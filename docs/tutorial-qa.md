# Tutorial and inventory gameplay

Tested September 10, 2026 in the CurseForge launcher: Minecraft 1.21.1, NeoForge 21.1.250, Java 21, 4 GB heap, Architectury 13.0.8, EMI 1.1.20 and JEI 19.21.0.247. Tests used a separate survival world with cheats for fixture setup.

## Inventory actions

| Action | Observed result |
| --- | --- |
| Hovered-stack shortcut | Moved the selected 32 cobblestone into a chest. The separate stack of 16 stayed in the inventory. |
| Native lock | Alt-click protected three diamonds; the hovered-stack action reported that the stack was locked. |
| Occupied cursor | The shortcut asked for the carried stack to be put down and moved nothing. |
| Restock | Took 48 cobblestone from the chest, increasing the existing stack from 16 to 64. The empty inventory slot stayed empty. |
| Quick Stack | Returned the 64 cobblestone to storage. The chest contained 72 total, matching the original inventory and chest counts. |
| Dump | Deposited six unlocked iron ingots while leaving the locked diamonds in place. |
| EMI nearby crafting | With no ingredients in the player inventory, placed three cobblestone and two sticks from the chest into the crafting grid. Taking the result produced one stone pickaxe; the chest retained 69 cobblestone and no sticks. |
| Button preferences | Applied offsets of -28 and -14, hid the inventory toolbar, reopened Settings with its bound key, restored visibility and reset the position. |

## Guides

Watched the original Inventory, Crafting, and Storage chapters before the final visual pass. The updated guide retains the captured forest backdrop, animated cursor, item movement, and vanilla menu presentation.

- Hovering a toolbar button and holding W opened the corresponding lesson.
- The book on Settings → Buttons opened Controls directly.
- The new Controls scenes demonstrated the selected-stack action, locks, offsets, hiding, and reset.
- Clicking the timeline paused at the selected step. Arrow-key seeking moved five seconds while remaining paused.
- The guide pauses singleplayer; multiplayer servers continue running normally.
- The rebuilt lock tooltip displayed the item name and native lock information on separate lines.
- GUI scales 2 and 4 kept chapter buttons, captions, and transport controls readable.

The final callout revision replaces the generic transfer symbol with an outline around the selected slot and the labels “Move this stack” and “The other stack stays here.” It has build and translation coverage; its final appearance has not been rechecked in Minecraft.

Fabric gameplay, Minecraft 1.21.4 gameplay, multiplayer, and optional backpack integrations are not covered by this session. Automated transfer tests cover additional item-conservation and stale-menu cases.

## 1.1.0 release verification — September 11, 2026

Both 1.21 branches passed `build :common:test`, producing the Fabric and NeoForge release jars. Uploaded GitHub and CurseForge files match the local SHA-256 hashes; Modrinth file hashes and required dependencies match. Release receipts are in `docs/releases/`. Existing gameplay evidence above remains the scope of runtime coverage; no additional Minecraft client was launched for this release. The final guide callout and recipe-book camera changes were also exercised in the 1.20.1 backport gameplay session.
