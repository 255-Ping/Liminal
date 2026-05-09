package com.github.ping.liminal.item;

import org.bukkit.inventory.ItemStack;

/**
 * One Liminal custom item. Implementations are stateless factories — every call to
 * {@link #create()} returns a fresh ItemStack with the marker NBT applied so the
 * runtime can recognise it later via {@link CustomItems#idOf(ItemStack)}.
 *
 * Behaviour (consume effects, hold ticks, equip rules) is handled by listeners or
 * scheduled tasks elsewhere — keep the item class itself a builder.
 */
public interface CustomItem {

    /** Stable id used in PDC marker, registry lookups, and loot tables. */
    String id();

    /** Build a fresh ItemStack of this item, marked with the id NBT. */
    ItemStack create();
}
