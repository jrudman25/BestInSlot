package com.bestinslot.client.query;

import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record CategorizedItem(ItemStack stack, Map<SortCriteria, Double> stats) {

    public double getStat(SortCriteria criteria) {
        return stats.getOrDefault(criteria, 0.0);
    }

    public String getFormattedStat(SortCriteria criteria) {
        double value = getStat(criteria);
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }
}
