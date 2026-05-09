package com.github.ping.liminal.stat;

import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Slow death from depletion. Every 4 seconds, each online player whose hunger OR
 * thirst is 0 takes 1 HP — matches vanilla starvation cadence, single tick regardless
 * of whether one or both stats are empty (keep it slow, not punishing).
 *
 * Combined with the hunger gate that holds MC food level at 10 (no auto-regen) and
 * the depleted gate that drops it to 1, players can't passively heal out — recovering
 * from a near-death state has to come from Liminal items, once those exist.
 */
public final class StatDamageTask {

    private static final long PERIOD_TICKS = 20L * 4L;
    private static final double DAMAGE_PER_TICK = 1.0;

    private StatDamageTask() {}

    public static void start(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isDead() || p.getHealth() <= 0.0) continue;
                if (StatService.getHunger(p) > 0 && StatService.getThirst(p) > 0) continue;
                p.damage(DAMAGE_PER_TICK, DamageSource.builder(DamageType.STARVE).build());
            }
        }, PERIOD_TICKS, PERIOD_TICKS);
    }
}
