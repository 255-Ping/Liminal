package com.github.ping.liminal.stat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Decay hunger and thirst by 1 each per minute. ~20 minutes from full to empty for
 * each independently. Below 0 is no-op — the damage task takes over from there.
 */
public final class StatDecayTask {

    private StatDecayTask() {}

    public static void start(Plugin plugin) {
        long oneMinute = 20L * 60L;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (StatService.getHunger(p) > 0) StatService.modifyHunger(p, -1);
                if (StatService.getThirst(p) > 0) StatService.modifyThirst(p, -1);
            }
        }, oneMinute, oneMinute);
    }
}
