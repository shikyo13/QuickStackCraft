# Changelog

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
