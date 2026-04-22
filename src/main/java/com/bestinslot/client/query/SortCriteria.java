package com.bestinslot.client.query;

public enum SortCriteria {
    DPS("DPS"),
    ATTACK_DAMAGE("Damage"),
    ATTACK_SPEED("Speed"),
    ARMOR("Armor"),
    ARMOR_TOUGHNESS("Toughness"),
    KNOCKBACK_RESISTANCE("KB Resist"),
    DURABILITY("Durability"),
    MINING_SPEED("Mine Speed");

    private final String displayName;

    SortCriteria(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
