package com.github.ping.liminal.item.items;

import com.github.ping.liminal.item.CustomItem;
import com.github.ping.liminal.item.CustomItems;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public final class JacketItem implements CustomItem {

    public static final String ID = "jacket";

    /** Brown→gray palette. Each Jacket rolls one entry. */
    private static final int[] COLORS = {
            0x6B4423, // dark brown
            0x8B5A2B, // saddle brown
            0xA0826D, // taupe
            0x8B7355, // beige brown
            0x736F6E, // dim gray
            0x6B6B6B, // medium gray
            0x595959, // dark gray
            0x808080, // gray
    };

    @Override public String id() { return ID; }

    @Override
    public ItemStack create() {
        ItemStack stack = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        meta.displayName(Component.text("Jacket", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Smells faintly of damp.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        int rgb = COLORS[ThreadLocalRandom.current().nextInt(COLORS.length)];
        meta.setColor(Color.fromRGB(rgb));
        stack.setItemMeta(meta);
        return CustomItems.mark(stack, ID);
    }
}
