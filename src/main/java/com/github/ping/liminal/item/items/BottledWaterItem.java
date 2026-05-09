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

public final class BottledWaterItem implements CustomItem {

    public static final String ID = "bottled_water";
    public static final int THIRST_RESTORE = 8;

    @Override public String id() { return ID; }

    @Override
    public ItemStack create() {
        ItemStack stack = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        meta.displayName(Component.text("Bottled Water", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Restores thirst.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setColor(Color.fromRGB(0x6E, 0xA8, 0xD8)); // light blue
        stack.setItemMeta(meta);
        return CustomItems.mark(stack, ID);
    }
}
