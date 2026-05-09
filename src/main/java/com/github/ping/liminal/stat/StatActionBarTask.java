package com.github.ping.liminal.stat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Once-per-second loop. Renders HP / Hunger / Thirst on every online player's action
 * bar and re-asserts MC food level so vanilla activity drain (sprinting, jumping)
 * doesn't pull the hunger bar away from our gate value.
 */
public final class StatActionBarTask {

    private static final long PERIOD_TICKS = 20L;

    private StatActionBarTask() {}

    public static void start(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                int hp     = StatService.getHealth(p);
                int hpMax  = StatService.maxHealth(p);
                int hunger = StatService.getHunger(p);
                int thirst = StatService.getThirst(p);
                p.sendActionBar(render(hp, hpMax, hunger, thirst));
                StatService.syncMcHunger(p, hunger);
            }
        }, PERIOD_TICKS, PERIOD_TICKS);
    }

    private static Component render(int hp, int hpMax, int hunger, int thirst) {
        Component sep = Component.text("  ·  ", NamedTextColor.DARK_GRAY);
        return Component.text("HP ", NamedTextColor.GRAY)
                .append(Component.text(hp + "/" + hpMax, NamedTextColor.RED))
                .append(sep)
                .append(Component.text("Hunger ", NamedTextColor.GRAY))
                .append(Component.text(hunger + "/" + StatService.MAX, NamedTextColor.GOLD))
                .append(sep)
                .append(Component.text("Thirst ", NamedTextColor.GRAY))
                .append(Component.text(thirst + "/" + StatService.MAX, NamedTextColor.AQUA));
    }
}
