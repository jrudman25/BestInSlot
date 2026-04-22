# Best In Slot

A client-side NeoForge mod for Minecraft 1.21.1 that helps you find the best item in any category.

Large modpacks ship hundreds of weapons, armor pieces, and tools. Paging through JEI/EMI to compare them is tedious. **Best In Slot** scans every registered item, classifies it by category, and lets you sort by the stats that matter.

## Features

- **Melee Weapons** — sorted by DPS, attack damage, attack speed, durability
- **Armor** (per slot) — sorted by armor, toughness, knockback resistance, durability
- **Tools** (pickaxe, shovel, hoe) — sorted by mining speed, attack damage, durability
- **Ranged Weapons** — bows, crossbows, tridents

## Usage

1. Press **B** (configurable) to open the Best In Slot screen.
2. Click a **category tab** to switch between item types.
3. Click a **sort column** to sort — click again to toggle ascending/descending.
4. Hover any item icon to see its full vanilla tooltip.

## Installation

Drop the mod JAR into your `mods/` folder. **Client-side only** — works on any server without server-side install.

## Building

```
./gradlew build
```

The output JAR will be in `build/libs/`.

## License

MIT
