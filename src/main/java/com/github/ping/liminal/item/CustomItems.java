package com.github.ping.liminal.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Static façade for the custom-item subsystem. Initialised in Liminal#onEnable so the
 * marker NamespacedKey is bound to the plugin instance, then used everywhere via
 * {@link #mark}, {@link #idOf}, and {@link #registry()}.
 */
public final class CustomItems {

    private static NamespacedKey idKey;
    private static final CustomItemRegistry REGISTRY = new CustomItemRegistry();

    private CustomItems() {}

    public static void init(Plugin plugin) {
        idKey = new NamespacedKey(plugin, "item_id");
    }

    public static CustomItemRegistry registry() {
        return REGISTRY;
    }

    /** Stamp the custom-item id into the stack's PDC. Returns the same stack for chaining. */
    public static ItemStack mark(ItemStack stack, String id) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
        stack.setItemMeta(meta);
        return stack;
    }

    /** @return the custom-item id stored in the stack's PDC, or null if it isn't ours. */
    public static String idOf(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    public static boolean is(ItemStack stack, String id) {
        return id != null && id.equals(idOf(stack));
    }
}
