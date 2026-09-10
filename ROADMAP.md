# Roadmap

## Next update: 1.1

Target: Minecraft 1.21.1 and 1.21.4, on Fabric and NeoForge.

- **Restock partial stacks.** Fill existing unlocked stacks from nearby storage without occupying empty slots. Show which containers supplied the items.
- **Safer transfers from open storage.** Keep the open container out of the destination list and ignore requests for a screen that has already closed.
- **Choose where buttons appear.** Hide inventory and storage buttons independently, or move either toolbar by an exact horizontal and vertical pixel offset. Keep keybinds available when buttons are hidden. Requested in [#6: QOL improvements](https://github.com/shikyo13/QuickStackCraft/issues/6).
- **Quick Stack the hovered stack.** Add an optional keybind that moves only the player-inventory stack under the cursor, including the hotbar. Respect native and ItemLocks protection, keep the cursor stack untouched, and report when no matching destination has room. Requested in [#6](https://github.com/shikyo13/QuickStackCraft/issues/6).
- **Clear feedback and readable controls.** Keep button positions attached to the inventory when the recipe book opens, make settings usable at small GUI sizes, and explain unchanged transfers without implying they succeeded.

Before release, verify Quick Stack, Dump, Restock and Craft Nearby with item counts and components preserved; locked slots, full storage, empty scans and stale screens should leave items intact. Build both loaders for both supported Minecraft versions, then exercise the inventory controls and recipe-viewer integration in Minecraft.

## Item-transfer animation

[#5: Private Build request](https://github.com/shikyo13/QuickStackCraft/issues/5) proposes Terraria-style flying items between the player and the containers that received them. A contribution to the official mod is welcome, as confirmed in the issue reply.

- Drive the animation from completed transfers, showing the actual item and destination.
- Render cosmetic items on clients, without creating collectible items or changing storage contents.
- Provide an off switch and bound the number of animated items during large transfers.
- Keep outlines and text feedback available when animation is disabled.
- Support multiplayer and both loaders; verify that animation has no effect on transfer counts or completion.

Status: planned. Contributions can be coordinated in the linked issue.
