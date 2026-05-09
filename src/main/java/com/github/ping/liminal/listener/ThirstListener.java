package com.github.ping.liminal.listener;

import com.github.ping.liminal.item.CustomItems;
import com.github.ping.liminal.item.items.AlmondWaterItem;
import com.github.ping.liminal.item.items.BottledWaterItem;
import com.github.ping.liminal.stat.StatService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Routes Liminal consumable items to the stat service. The drink animation +
 * 32-tick consume window is provided by Material.POTION; we just intercept the
 * consume event, apply the thirst delta, and replace the resulting empty bottle
 * with AIR so it isn't returned to the inventory.
 */
public final class ThirstListener implements Listener {

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        String id = CustomItems.idOf(item);
        if (id == null) return;
        int delta = switch (id) {
            case AlmondWaterItem.ID  -> AlmondWaterItem.THIRST_RESTORE;
            case BottledWaterItem.ID -> BottledWaterItem.THIRST_RESTORE;
            default -> 0;
        };
        if (delta == 0) return;
        StatService.modifyThirst(e.getPlayer(), delta);
        e.setReplacement(new ItemStack(Material.AIR));
    }
}
