# QuickStack & Craft

[![CurseForge](https://img.shields.io/curseforge/dt/1505147?logo=curseforge&label=CurseForge)](https://www.curseforge.com/minecraft/mc-mods/quickstack-craft)
[![Modrinth](https://img.shields.io/modrinth/dt/quickstack-craft?logo=modrinth&label=Modrinth)](https://modrinth.com/mod/quickstack-craft)
[![GitHub](https://img.shields.io/github/v/release/shikyo13/QuickStackCraft?logo=github)](https://github.com/shikyo13/QuickStackCraft/releases)
[![License](https://img.shields.io/badge/license-source--available-orange)](LICENSE)

Terraria-style inventory management for Minecraft. Quick Stack matching items into nearby storage, dump unlocked items, protect important slots, and craft with ingredients stored around you.

QuickStack & Craft 1.0 supports **Minecraft 1.21.1 and 1.21.4** on **Fabric and NeoForge** through [Architectury API](https://modrinth.com/mod/architectury-api).

[Watch the QuickStack & Craft 1.0 showcase](https://www.youtube.com/watch?v=l_6XsxF90Pw)

## Features

**Inventory and Open-Storage Controls** - Compact Q, D, and settings buttons sit beside the vanilla recipe-book control. Supported open-storage screens also expose Quick Stack and Dump controls for their contents.

**Quick Stack to Nearby** - Deposit items into nearby storage that already holds matching items. Prioritizes fullest containers first.

**Dump All** - Deposit all unlocked inventory items to nearby storage with space.

**Storage-Safe Detection** - Quick stack and dump use real storage by default, avoiding machines and trash-style inventories that can consume items.

**Native Slot Locks** - Alt+click any player inventory slot in any container screen to protect it from quick stack and dump. Locks are server-persisted and survive death.

**ItemLocks Compatibility** - ItemLocks remains optional. Its client-side locks are combined with native locks during quick stack, dump, and recipe transfer operations.

**Full In-Game Settings** - Configure scan range and safe-storage detection from the inventory gear button or an optional keybind. Block items can be dragged from JEI or EMI directly into the Storage Block Whitelist or Blacklist. Both lists apply by block type, so every matching storage block is affected and coordinates are not stored.

**Customizable Visual Feedback** - Choose the destination outline color, opacity, and duration with preset swatches, advanced hex input, and a live preview. Appearance preferences remain client-local.

**Animated Tutorial** - Three replayable chapters cover Quick Stack / Dump / Locks, Craft Nearby, and Whitelist / Blacklist / Preview using runtime Minecraft terrain models and the vanilla recipe book. It opens automatically on the first inventory opening in each save or server and remains replayable from Settings.

**Storage Whitelist / Blacklist Cycle** - Aim at item storage and use the Cycle Storage Block Whitelist / Blacklist key to move its block type through Default, Whitelisted, and Blacklisted states with color and text confirmation.

**Sophisticated Backpacks Support** - When a Sophisticated Backpacks storage screen is open, Q/D buttons and keybinds transfer the backpack's contents into nearby storage.

**Craft from Nearby** - The vanilla recipe book, JEI, and EMI check your inventory and nearby containers for ingredients. Recipe variants are allocated from the items you actually own, locked slots remain protected, and the server resolves the recipe before moving anything into the crafting grid. Hold Shift during recipe transfer to fill as many crafts as the available ingredients allow.

**Storage Feedback** - Successful destinations receive a configurable outline and particle trail. Whitelist, blacklist, and default changes use distinct colors plus action-bar text so state is never communicated by color alone.

**Modded Storage Support** - Works with vanilla storage, common storage tags, Fabric Transfer API storage, NeoForge item-handler storage, and known storage namespaces such as Sophisticated Storage, Iron Chests, Storage Drawers, and Functional Storage.

## Supported Versions

| Minecraft | Fabric | NeoForge |
|-|-|-|
| 1.21.1 | Yes | Yes |
| 1.21.4 | Yes | Yes |

## Installation

Install on the server and participating clients for full functionality. Item movement and storage-list configuration are server-authoritative, and multiplayer list changes are restricted to operators.

### Dependencies
- [Architectury API](https://modrinth.com/mod/architectury-api) (required)
- [Fabric API](https://modrinth.com/mod/fabric-api) (Fabric only)
- [JEI](https://modrinth.com/mod/jei) (optional - recipe viewer crafting and whitelist/blacklist drag-and-drop)
- [EMI](https://modrinth.com/mod/emi) (optional on Minecraft 1.21.1 - recipe viewer crafting and whitelist/blacklist drag-and-drop)
- [ItemLocks](https://www.curseforge.com/minecraft/mc-mods/itemlocks) (optional - external slot-lock compatibility)
- [Sophisticated Backpacks](https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks) (optional - direct open-backpack transfers)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional on Fabric - opens the built-in settings screen from the mod list)

### Multiplayer
Install on both the server and all clients. The server handles all item movement logic. Clients without the mod can still connect but won't have access to the features.

## Keybinds

The inventory Q, D, and settings buttons work immediately. All five keyboard actions are unbound by default under **Options -> Controls -> Key Binds -> QuickStack & Craft**.

| Action | Default |
|-|-|
| Quick Stack Nearby | Unbound (configure in Controls) |
| Dump Unlocked Items | Unbound (configure in Controls) |
| Preview Nearby Storage | Unbound (configure in Controls) |
| Cycle Storage Block Whitelist / Blacklist | Unbound (configure in Controls) |
| Open QuickStack & Craft Settings | Unbound (configure in Controls) |
| Native Slot Lock Toggle | Alt + Left Click |

## Building

```bash
git clone https://github.com/shikyo13/QuickStackCraft.git
cd QuickStackCraft
git checkout mc/1.21.1  # or mc/1.21.4
./gradlew build
```

Output jars are in `fabric/build/libs/` and `neoforge/build/libs/`.

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/quickstack-craft)
- [Modrinth](https://modrinth.com/mod/quickstack-craft)
- [Video Showcase](https://www.youtube.com/watch?v=l_6XsxF90Pw)
- [Source](https://github.com/shikyo13/QuickStackCraft)
- [Website](https://zeronexus.net/mods.php)
- [Issues](https://github.com/shikyo13/QuickStackCraft/issues)
- [Buy Me a Coffee](https://buymeacoffee.com/zerotheabsolute)

## License

[QuickStackCraft Source Available License](LICENSE). This is a proprietary source-available license, not an open-source license. Forks, modified builds, competing project pages, mirrors, reuploads, and other redistribution require explicit written authorization identifying the authorized project and recipient. Modpacks may reference official CurseForge or Modrinth releases but may not bundle or rehost the mod. GitHub's mandatory public-repository terms may separately permit viewing and service-level forking only within their stated scope. Earlier releases remain governed by the license distributed with them.
