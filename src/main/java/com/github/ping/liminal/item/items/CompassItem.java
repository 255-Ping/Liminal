package com.github.ping.liminal.item.items;

import com.github.ping.liminal.item.CustomItem;
import com.github.ping.liminal.item.CustomItems;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public final class CompassItem implements CustomItem {

    public static final String ID = "compass_erratic";

    @Override public String id() { return ID; }

    @Override
    public ItemStack create() {
        ItemStack stack = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) stack.getItemMeta();
        meta.displayName(Component.text("Compass", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Behaving erratically.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setLodestoneTracked(false); // setLodestone targets become valid even without a real lodestone
        stack.setItemMeta(meta);
        return CustomItems.mark(stack, ID);
    }
}
