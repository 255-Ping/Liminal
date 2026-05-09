package com.github.ping.liminal.task;

import com.github.ping.liminal.item.CustomItems;
import com.github.ping.liminal.item.items.CompassItem;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.Plugin;

/**
 * Every second, scan online players for a Compass-Erratic and re-target its lodestone
 * to a random nearby location. Cheap loop — no per-player state, no allocation per
 * tick beyond the ItemMeta clone Bukkit forces.
 */
public final class CompassErraticTask {

    private static final long PERIOD_TICKS = 20L;
    private static final double JITTER_RADIUS = 50.0;

    private CompassErraticTask() {}

    public static void start(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                ItemStack[] contents = p.getInventory().getContents();
                for (ItemStack item : contents) {
                    if (CustomItems.is(item, CompassItem.ID)) {
                        randomize(item, p.getLocation());
                    }
                }
            }
        }, PERIOD_TICKS, PERIOD_TICKS);
    }

    private static void randomize(ItemStack item, Location playerLoc) {
        if (!(item.getItemMeta() instanceof CompassMeta meta)) return;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double dx = (r.nextDouble() - 0.5) * 2 * JITTER_RADIUS;
        double dz = (r.nextDouble() - 0.5) * 2 * JITTER_RADIUS;
        Location target = playerLoc.clone().add(dx, 0, dz);
        meta.setLodestone(target);
        meta.setLodestoneTracked(false);
        item.setItemMeta(meta);
    }
}
