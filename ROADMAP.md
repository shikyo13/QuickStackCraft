# Roadmap

## Next update: 1.1

Target: Minecraft 1.21.1 and 1.21.4, on Fabric and NeoForge.

The features below are implemented in the 1.1 development branches. Both versions build for both loaders. Minecraft 1.21.1 NeoForge gameplay covers transfers, restocking, locks, nearby crafting through EMI, button preferences, and the animated guide. See [gameplay notes](docs/tutorial-qa.md) for the tested environment and remaining coverage.

- **Restock partial stacks.** Fill existing unlocked stacks from nearby storage without occupying empty slots. Show which containers supplied the items.
- **Safer transfers from open storage.** Keep the open container out of the destination list and ignore requests for a screen that has already closed.
- **Choose where buttons appear.** Hide inventory and storage buttons independently, or move either toolbar by an exact horizontal and vertical pixel offset. Keep keybinds available when buttons are hidden. Requested in [#6: QOL improvements](https://github.com/shikyo13/QuickStackCraft/issues/6).
- **Quick Stack the hovered stack.** Add an optional keybind that moves only the player-inventory stack under the cursor, including the hotbar. Respect native and ItemLocks protection, keep the cursor stack untouched, and report when no matching destination has room. Requested in [#6](https://github.com/shikyo13/QuickStackCraft/issues/6).
- **Clear feedback and readable controls.** Keep button positions attached to the inventory when the recipe book opens, make settings usable at small GUI sizes, and explain unchanged transfers without implying they succeeded.
- **Animated help beside the controls.** Open a relevant lesson by hovering a toolbar button and holding the forward key. Add a Controls chapter for the new shortcuts and button preferences, with a timeline that can be paused, replayed, or scrubbed.

Before release, complete the gameplay coverage noted below, with particular attention to multiplayer and optional storage integrations. Keep transfer counts, item components, and locked slots intact on every supported loader.

## Item-transfer animation

[#5: Private Build request](https://github.com/shikyo13/QuickStackCraft/issues/5) proposes Terraria-style flying items between the player and the containers that received them. A contribution to the official mod is welcome, as confirmed in the issue reply.

- Drive the animation from completed transfers, showing the actual item and destination.
- Render cosmetic items on clients, without creating collectible items or changing storage contents.
- Provide an off switch and bound the number of animated items during large transfers.
- Keep outlines and text feedback available when animation is disabled.
- Support multiplayer and both loaders; verify that animation has no effect on transfer counts or completion.

Status: planned. Contributions can be coordinated in the linked issue.
