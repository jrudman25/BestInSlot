package com.bestinslot.client.query;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.stream.Collectors;

public class AttributeQueryProvider implements ItemQueryProvider {

    private static final double BASE_ATTACK_DAMAGE = 1.0;
    private static final double BASE_ATTACK_SPEED = 4.0;

    @Override
    public List<CategorizedItem> query(ItemCategory category, SortCriteria sort, boolean ascending) {
        List<CategorizedItem> results = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (stack.isEmpty()) continue;

            CategorizedItem categorized = classify(stack, category);
            if (categorized != null) {
                results.add(categorized);
            }
        }

        results.sort((a, b) -> {
            int cmp = Double.compare(a.getStat(sort), b.getStat(sort));
            return ascending ? cmp : -cmp;
        });

        return results;
    }

    private CategorizedItem classify(ItemStack stack, ItemCategory category) {
        return switch (category) {
            case MELEE_WEAPONS -> classifyMelee(stack);
            case HELMET -> classifyArmor(stack, EquipmentSlot.HEAD);
            case CHESTPLATE -> classifyArmor(stack, EquipmentSlot.CHEST);
            case LEGGINGS -> classifyArmor(stack, EquipmentSlot.LEGS);
            case BOOTS -> classifyArmor(stack, EquipmentSlot.FEET);
            case PICKAXE -> classifyTool(stack, ToolType.PICKAXE);
            case SHOVEL -> classifyTool(stack, ToolType.SHOVEL);
            case HOE -> classifyTool(stack, ToolType.HOE);
            case RANGED_WEAPONS -> classifyRanged(stack);
        };
    }

    private CategorizedItem classifyMelee(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.getAttributeModifiers();
        if (modifiers.modifiers().isEmpty()) return null;

        double attackDamage = 0;
        double attackSpeed = 0;
        boolean hasAttackDamage = false;

        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (!matchesSlotGroup(entry.slot(), EquipmentSlotGroup.MAINHAND)) continue;

            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                attackDamage += resolveModifierValue(entry.modifier());
                hasAttackDamage = true;
            } else if (entry.attribute().is(Attributes.ATTACK_SPEED)) {
                attackSpeed += resolveModifierValue(entry.modifier());
            }
        }

        if (!hasAttackDamage) return null;

        double effectiveDamage = BASE_ATTACK_DAMAGE + attackDamage;
        double effectiveSpeed = BASE_ATTACK_SPEED + attackSpeed;
        double dps = effectiveDamage * effectiveSpeed;

        Map<SortCriteria, Double> stats = new EnumMap<>(SortCriteria.class);
        stats.put(SortCriteria.ATTACK_DAMAGE, effectiveDamage);
        stats.put(SortCriteria.ATTACK_SPEED, effectiveSpeed);
        stats.put(SortCriteria.DPS, dps);
        stats.put(SortCriteria.DURABILITY, (double) stack.getMaxDamage());

        return new CategorizedItem(stack, stats);
    }

    private CategorizedItem classifyArmor(ItemStack stack, EquipmentSlot slot) {
        Item item = stack.getItem();
        if (!(item instanceof ArmorItem armorItem)) return null;
        if (armorItem.getEquipmentSlot() != slot) return null;

        ItemAttributeModifiers modifiers = stack.getAttributeModifiers();
        EquipmentSlotGroup slotGroup = slot.isArmor()
                ? EquipmentSlotGroup.bySlot(slot)
                : EquipmentSlotGroup.ANY;

        double armor = 0;
        double toughness = 0;
        double kbResist = 0;

        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (!matchesSlotGroup(entry.slot(), slotGroup)) continue;

            if (entry.attribute().is(Attributes.ARMOR)) {
                armor += resolveModifierValue(entry.modifier());
            } else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS)) {
                toughness += resolveModifierValue(entry.modifier());
            } else if (entry.attribute().is(Attributes.KNOCKBACK_RESISTANCE)) {
                kbResist += resolveModifierValue(entry.modifier());
            }
        }

        Map<SortCriteria, Double> stats = new EnumMap<>(SortCriteria.class);
        stats.put(SortCriteria.ARMOR, armor);
        stats.put(SortCriteria.ARMOR_TOUGHNESS, toughness);
        stats.put(SortCriteria.KNOCKBACK_RESISTANCE, kbResist);
        stats.put(SortCriteria.DURABILITY, (double) stack.getMaxDamage());

        return new CategorizedItem(stack, stats);
    }

    private CategorizedItem classifyTool(ItemStack stack, ToolType toolType) {
        Item item = stack.getItem();
        boolean matches = switch (toolType) {
            case PICKAXE -> item instanceof PickaxeItem;
            case SHOVEL -> item instanceof ShovelItem;
            case HOE -> item instanceof HoeItem;
        };

        if (!matches) {
            if (!(item instanceof DiggerItem)) return null;
            // Fallback: check if the item can mine the representative block faster than bare hand
            float speed = stack.getDestroySpeed(toolType.getRepresentativeBlockState());
            if (speed <= 1.0f) return null;
        }

        float miningSpeed = stack.getDestroySpeed(toolType.getRepresentativeBlockState());

        // Also extract attack damage if present
        double attackDamage = 0;
        ItemAttributeModifiers modifiers = stack.getAttributeModifiers();
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (!matchesSlotGroup(entry.slot(), EquipmentSlotGroup.MAINHAND)) continue;
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                attackDamage += resolveModifierValue(entry.modifier());
            }
        }

        Map<SortCriteria, Double> stats = new EnumMap<>(SortCriteria.class);
        stats.put(SortCriteria.MINING_SPEED, (double) miningSpeed);
        stats.put(SortCriteria.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE + attackDamage);
        stats.put(SortCriteria.DURABILITY, (double) stack.getMaxDamage());

        return new CategorizedItem(stack, stats);
    }

    private CategorizedItem classifyRanged(ItemStack stack) {
        Item item = stack.getItem();
        boolean isRanged = item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem
                || item instanceof ProjectileWeaponItem;

        if (!isRanged) return null;

        Map<SortCriteria, Double> stats = new EnumMap<>(SortCriteria.class);
        stats.put(SortCriteria.DURABILITY, (double) stack.getMaxDamage());

        return new CategorizedItem(stack, stats);
    }

    private static boolean matchesSlotGroup(EquipmentSlotGroup group, EquipmentSlotGroup target) {
        if (group == EquipmentSlotGroup.ANY || target == EquipmentSlotGroup.ANY) return true;
        return group == target;
    }

    private static double resolveModifierValue(AttributeModifier modifier) {
        // For ADD_VALUE operation (the standard for weapon/armor stats), just return the amount.
        // MULTIPLY_BASE and MULTIPLY_TOTAL are uncommon for base item stats.
        return modifier.amount();
    }

    private enum ToolType {
        PICKAXE,
        SHOVEL,
        HOE;

        public net.minecraft.world.level.block.state.BlockState getRepresentativeBlockState() {
            return switch (this) {
                case PICKAXE -> Blocks.STONE.defaultBlockState();
                case SHOVEL -> Blocks.DIRT.defaultBlockState();
                case HOE -> Blocks.HAY_BLOCK.defaultBlockState();
            };
        }
    }
}
