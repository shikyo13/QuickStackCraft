# Changelog

## 1.1.0 (unreleased)

- Added a Buttons settings tab with independent visibility and exact pixel offsets for inventory and open-storage controls.
- Added an optional Quick Stack Hovered Stack keybind. It moves only the selected player-inventory stack, respects slot locks and leaves the cursor stack untouched.
- Kept toolbars attached to the inventory when the recipe book shifts the screen, and kept moved buttons within the viewport.
- Added feedback for a locked stack, occupied cursor, changed inventory and matching storage without space.
- Fixed the executable permission on the Gradle wrapper so the documented build command works on macOS and Linux.
- Added Restock Partial Stacks, which fills existing unlocked inventory and hotbar stacks from nearby storage without occupying empty slots.
- Added a compact R inventory control and an unbound Restock key under the QuickStack & Craft controls category.
- Added cyan source-storage outlines and action-bar feedback for successful restocks.
- Expanded direct open-storage Q/D transfers to vanilla chest-style menus, shulker boxes, Traveler's Backpack, and Inmis while retaining Sophisticated support.
- Added active-menu validation and source exclusion so stale packets are ignored and an open storage block cannot transfer into itself.
- Added a Restock segment to the animated tutorial using the same toolbar layout, lock marker, and highlight palette as the live interface.
- Kept AE2 and Refined Storage network blocks outside automatic targeting so their native storage models remain authoritative.
- Preserved feature parity across Fabric and NeoForge for Minecraft 1.21.1 and 1.21.4.

## 1.0.1

- Fixed Fabric dedicated servers crashing during startup while registering clientbound packets.
- Added safe automatic recognition for direct Ars Nouveau Repository blocks without targeting aggregate storage networks.

## 1.0.0

- Rebuilt the in-game settings around General, Storage Whitelist / Blacklist, and Appearance tabs with responsive layouts, localized storage names, block icons, tooltips, color swatches, and save-on-close behavior.
- Made JEI and EMI sidebars available on the Whitelist / Blacklist settings page for universal block-type drag-and-drop.
- Added compact Q, D, and Settings controls to the inventory plus icon controls on supported storage screens.
- Added three deterministic animated tutorial chapters for Quick Stack / Dump / Locks, Craft Nearby, and Whitelist / Blacklist / Preview, using the live toolbar geometry and highlight palette.
- Clearly separates the Preview hotkey from the Whitelist / Blacklist hotkey inside the final chapter and increases every caption hold for comfortable reading.
- Anchored the animated chest by its complete projected model bounds, corrected its depth orientation, and added an explicit first-inventory notice with replay guidance.
- Removed tutorial player-model overdraw and corrected crafting-table layering and model-aligned storage outlines.
- Added an author-captured Minecraft forest backdrop and rebuilt the tutorial recipe lookup from runtime vanilla GUI sprites and item rendering.
- Added once-per-player, per-save tutorial progress with permanent replay access from Settings.
- Added the `Default -> Whitelisted -> Blacklisted -> Default` list cycle with green, red, and cyan world feedback.
- Added a native corner lock icon, source-aware tooltip, and conflict-free ItemLocks gesture handling.
- Moved highlight appearance preferences to client-local configuration.
- Added a focused About page and repaired live appearance color selection and preview.
- Added revisioned, permission-aware server settings synchronization so delayed packets cannot overwrite active edits.
- Preserved Fabric and NeoForge parity for Minecraft 1.21.1 and 1.21.4 without adding a required dependency.
