package com.bestinslot.client.query;

public enum ItemCategory {
    MELEE_WEAPONS("Melee Weapons"),
    HELMET("Helmets"),
    CHESTPLATE("Chestplates"),
    LEGGINGS("Leggings"),
    BOOTS("Boots"),
    PICKAXE("Pickaxes"),
    SHOVEL("Shovels"),
    HOE("Hoes"),
    RANGED_WEAPONS("Ranged Weapons");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SortCriteria[] getApplicableSorts() {
        return switch (this) {
            case MELEE_WEAPONS -> new SortCriteria[]{
                    SortCriteria.DPS, SortCriteria.ATTACK_DAMAGE, SortCriteria.ATTACK_SPEED, SortCriteria.DURABILITY
            };
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> new SortCriteria[]{
                    SortCriteria.ARMOR, SortCriteria.ARMOR_TOUGHNESS, SortCriteria.KNOCKBACK_RESISTANCE, SortCriteria.DURABILITY
            };
            case PICKAXE, SHOVEL, HOE -> new SortCriteria[]{
                    SortCriteria.MINING_SPEED, SortCriteria.ATTACK_DAMAGE, SortCriteria.DURABILITY
            };
            case RANGED_WEAPONS -> new SortCriteria[]{
                    SortCriteria.DURABILITY
            };
        };
    }

    public SortCriteria getDefaultSort() {
        return getApplicableSorts()[0];
    }
}
