# AGENTS — BestInSlot

## Identity
- **Type**: Client-side Minecraft mod
- **Mod ID**: `bestinslot`
- **Package**: `com.bestinslot`
- **Loader**: NeoForge 21.1.219+ (Minecraft 1.21.1)
- **Java**: 21 (compiled), 24 (runtime compatible)
- **Build**: Gradle 8.14, NeoGradle 7.1.25

## Build Commands
- `.\gradlew.bat build` — outputs `build/libs/bestinslot-{version}.jar`
- `.\gradlew.bat runClient` — launches Minecraft with mod loaded
- Version is set in `gradle.properties` → `mod_version`
- Changing `gradle.properties` invalidates config cache (~9min rebuild); Java-only changes rebuild in ~5-15s

## Source Map
```
src/main/java/com/bestinslot/
├── BestInSlot.java                          — @Mod entrypoint, registers client events via FMLEnvironment dist check
└── client/
    ├── ClientEvents.java                    — Keybind (GLFW_KEY_B), client tick handler opens BestInSlotScreen
    ├── gui/
    │   └── BestInSlotScreen.java            — Screen subclass: category tabs, sort buttons, scrollable item list, tooltips
    └── query/
        ├── ItemQueryProvider.java           — Interface: query(ItemCategory, SortCriteria, boolean ascending) → List<CategorizedItem>
        ├── AttributeQueryProvider.java      — Scans BuiltInRegistries.ITEM, classifies by attribute modifiers
        ├── ItemCategory.java                — Enum: MELEE_WEAPONS, HELMET, CHESTPLATE, LEGGINGS, BOOTS, PICKAXE, SHOVEL, HOE, RANGED_WEAPONS
        ├── SortCriteria.java                — Enum: DPS, ATTACK_DAMAGE, ATTACK_SPEED, ARMOR, ARMOR_TOUGHNESS, KNOCKBACK_RESISTANCE, DURABILITY, MINING_SPEED
        └── CategorizedItem.java             — Record: (ItemStack stack, Map<SortCriteria, Double> stats)

src/main/resources/
├── META-INF/neoforge.mods.toml             — Mod metadata, dependencies (CLIENT side only)
└── assets/bestinslot/lang/en_us.json        — Translation keys: screen.bestinslot.title, key.bestinslot.open, key.categories.bestinslot
```

## Architecture

### Data Flow
1. User presses keybind → `ClientEvents.onClientTick` → opens `BestInSlotScreen`
2. Screen calls `ItemQueryProvider.query(category, sort, ascending)`
3. `AttributeQueryProvider` iterates `BuiltInRegistries.ITEM`, creates default `ItemStack` per item, reads `ItemAttributeModifiers`
4. Items classified by category, stats extracted into `CategorizedItem` records, sorted, returned
5. Screen renders: category tabs (Button widgets), sort buttons, scrollable item list with icons/names/stats, vanilla tooltips on hover

### Classification Logic
| Category | Detection | Stats Extracted |
|---|---|---|
| MELEE_WEAPONS | Has ATTACK_DAMAGE attribute modifier in MAINHAND slot | DPS=(baseDmg+1)*speed, ATTACK_DAMAGE, ATTACK_SPEED, DURABILITY |
| HELMET/CHESTPLATE/LEGGINGS/BOOTS | instanceof ArmorItem + matching EquipmentSlot | ARMOR, ARMOR_TOUGHNESS, KNOCKBACK_RESISTANCE, DURABILITY |
| PICKAXE/SHOVEL/HOE | instanceof specific DiggerItem subclass, fallback: getDestroySpeed on representative block | MINING_SPEED, ATTACK_DAMAGE, DURABILITY |
| RANGED_WEAPONS | instanceof BowItem/CrossbowItem/TridentItem/ProjectileWeaponItem | DURABILITY |

### Key Constants
- `BASE_ATTACK_DAMAGE = 1.0` (player base)
- `BASE_ATTACK_SPEED = 4.0` (player base)
- Items with `getMaxDamage() == 0` (energy/mana-based) → durability stored as `Double.MAX_VALUE`, displayed as `∞`

### Extensibility
`ItemQueryProvider` is an interface designed for future alternative backends. Planned: `AiQueryProvider` for natural-language item queries via external LLM API. The interface accepts category+sort, returns `List<CategorizedItem>` — no GUI coupling.

## Constraints
- Pure client-side: no custom items, no packets, no server dependency
- GUI is a raw `Screen` subclass, not a container — no inventory interaction
- `buildWidgets()` is a private method (not an override of `Screen.rebuildWidgets`) so the framework's resize flow (`rebuildWidgets` → `init`) works correctly
- `@EventBusSubscriber` on `ClientEvents` handles NeoForge game bus events; mod bus events registered via `IEventBus.addListener` in constructor to avoid deprecated `bus=MOD` annotation