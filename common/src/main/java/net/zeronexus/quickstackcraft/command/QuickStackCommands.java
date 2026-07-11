package net.zeronexus.quickstackcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.zeronexus.quickstackcraft.logic.ContainerScanner;
import net.zeronexus.quickstackcraft.logic.StorageBlockLists;
import net.zeronexus.quickstackcraft.network.ModNetworking;
import net.zeronexus.quickstackcraft.util.BlockSelection;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public final class QuickStackCommands {

    private enum StorageList {
        WHITELIST(
                StorageBlockLists::whitelist,
                StorageBlockLists::whitelist,
                StorageBlockLists::removeFromWhitelist,
                StorageBlockLists::clearWhitelist,
                "quickstackcraft.message.whitelist_added",
                "quickstackcraft.message.whitelist_removed",
                "quickstackcraft.message.whitelist_empty",
                "quickstackcraft.message.whitelist_cleared"),
        BLACKLIST(
                StorageBlockLists::blacklist,
                StorageBlockLists::blacklist,
                StorageBlockLists::removeFromBlacklist,
                StorageBlockLists::clearBlacklist,
                "quickstackcraft.message.blacklist_added",
                "quickstackcraft.message.blacklist_removed",
                "quickstackcraft.message.blacklist_empty",
                "quickstackcraft.message.blacklist_cleared");

        private final Supplier<Set<ResourceLocation>> entries;
        private final Function<ResourceLocation, Boolean> add;
        private final Function<ResourceLocation, Boolean> remove;
        private final Runnable clear;
        private final String addedMessage;
        private final String removedMessage;
        private final String emptyMessage;
        private final String clearedMessage;

        StorageList(
                Supplier<Set<ResourceLocation>> entries,
                Function<ResourceLocation, Boolean> add,
                Function<ResourceLocation, Boolean> remove,
                Runnable clear,
                String addedMessage,
                String removedMessage,
                String emptyMessage,
                String clearedMessage) {
            this.entries = entries;
            this.add = add;
            this.remove = remove;
            this.clear = clear;
            this.addedMessage = addedMessage;
            this.removedMessage = removedMessage;
            this.emptyMessage = emptyMessage;
            this.clearedMessage = clearedMessage;
        }
    }

    private QuickStackCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("quickstackcraft")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("storage")
                        .then(listCommands("whitelist", StorageList.WHITELIST))
                        .then(listCommands("blacklist", StorageList.BLACKLIST)));
        dispatcher.register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> listCommands(
            String name, StorageList storageList) {
        return Commands.literal(name)
                .then(changeCommand("add", storageList, true))
                .then(changeCommand("remove", storageList, false))
                .then(Commands.literal("show")
                        .executes(context -> show(context.getSource(), storageList)))
                .then(Commands.literal("clear")
                        .executes(context -> clear(context.getSource(), storageList)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> changeCommand(
            String name, StorageList storageList, boolean adding) {
        return Commands.literal(name)
                .executes(context -> changeSelected(context.getSource(), storageList, adding))
                .then(Commands.argument("block", ResourceLocationArgument.id())
                        .executes(context -> change(
                                context.getSource(),
                                storageList,
                                adding,
                                ResourceLocationArgument.getId(context, "block"))));
    }

    private static int changeSelected(
            CommandSourceStack source, StorageList storageList, boolean adding) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("quickstackcraft.message.player_required"));
            return 0;
        }

        BlockSelection selection = BlockSelection.within(player, 6.0D).orElse(null);
        if (selection == null) {
            source.sendFailure(Component.translatable("quickstackcraft.message.no_selected_block"));
            return 0;
        }
        if (adding && !ContainerScanner.hasItemStorage(player.level(), selection.position())) {
            source.sendFailure(Component.translatable("quickstackcraft.message.not_item_storage"));
            return 0;
        }
        return change(source, storageList, adding, selection.blockId());
    }

    private static int change(
            CommandSourceStack source,
            StorageList storageList,
            boolean adding,
            ResourceLocation blockId) {
        boolean changed = adding ? storageList.add.apply(blockId) : storageList.remove.apply(blockId);
        if (!changed) {
            source.sendFailure(Component.translatable(
                    "quickstackcraft.message.storage_list_unchanged", blockId.toString()));
            return 0;
        }

        String message = adding ? storageList.addedMessage : storageList.removedMessage;
        source.sendSuccess(() -> Component.translatable(message, blockId.toString()), true);
        ModNetworking.markConfigChanged();
        return 1;
    }

    private static int show(CommandSourceStack source, StorageList storageList) {
        Set<ResourceLocation> entries = storageList.entries.get();
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.translatable(storageList.emptyMessage), false);
            return 0;
        }

        String sortedEntries = entries.stream()
                .map(ResourceLocation::toString)
                .sorted()
                .collect(java.util.stream.Collectors.joining(", "));
        source.sendSuccess(() -> Component.literal(sortedEntries), false);
        return entries.size();
    }

    private static int clear(CommandSourceStack source, StorageList storageList) {
        if (storageList.entries.get().isEmpty()) {
            source.sendSuccess(() -> Component.translatable(storageList.emptyMessage), false);
            return 0;
        }
        storageList.clear.run();
        ModNetworking.markConfigChanged();
        source.sendSuccess(() -> Component.translatable(storageList.clearedMessage), true);
        return 1;
    }
}
