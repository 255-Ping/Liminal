package com.github.ping.liminal.item.items;

import com.github.ping.liminal.item.CustomItem;
import com.github.ping.liminal.item.CustomItems;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public final class AlmondWaterItem implements CustomItem {

    public static final String ID = "almond_water";
    public static final int THIRST_RESTORE = 12;

    @Override public String id() { return ID; }

    @Override
    public ItemStack create() {
        ItemStack stack = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        meta.displayName(Component.text("Almond Water", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Restores thirst.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Almonds.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setColor(Color.fromRGB(0xF0, 0xEA, 0xD6)); // pale almond
        stack.setItemMeta(meta);
        return CustomItems.mark(stack, ID);
    }
}
