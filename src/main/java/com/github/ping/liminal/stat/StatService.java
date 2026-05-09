package com.github.ping.liminal.stat;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Per-player stats: health (read live from MC), hunger and thirst (custom, PDC-stored).
 *
 * Hunger is decoupled from vanilla MC hunger:
 *  - While our hunger > 0, MC food level is held at {@link #MC_HUNGER_ACTIVE} (10) so the
 *    vanilla bar reads as half-full and acts as a visual cue that it isn't load-bearing.
 *    Sprint still works (≥7 internal); HP regen does not (needs 18+), which is intended —
 *    healing has to come from Liminal items, not idle ticks.
 *  - When our hunger reaches 0, MC food level drops to {@link #MC_HUNGER_DEPLETED} (1) so
 *    vanilla starvation damage kicks in.
 *
 * The {@link StatActionBarTask} re-asserts MC hunger every second; that's the fence that
 * keeps vanilla activity-driven food drain from skewing the bar between explicit sets.
 */
public final class StatService {

    public static final int MAX = 20;
    public static final int DEFAULT = 20;

    static final int MC_HUNGER_ACTIVE   = 10;
    static final int MC_HUNGER_DEPLETED = 1;

    private static NamespacedKey hungerKey;
    private static NamespacedKey thirstKey;

    private StatService() {}

    public static void init(Plugin plugin) {
        hungerKey = new NamespacedKey(plugin, "hunger");
        thirstKey = new NamespacedKey(plugin, "thirst");
    }

    // --- Health: read live from MC, no custom storage ---

    public static int getHealth(Player p) { return (int) Math.round(p.getHealth()); }
    public static int maxHealth(Player p) { return (int) Math.round(p.getMaxHealth()); }

    // --- Hunger ---

    public static int getHunger(Player p) {
        Integer v = p.getPersistentDataContainer().get(hungerKey, PersistentDataType.INTEGER);
        return v != null ? v : DEFAULT;
    }

    public static void setHunger(Player p, int value) {
        int v = clamp(value);
        p.getPersistentDataContainer().set(hungerKey, PersistentDataType.INTEGER, v);
        syncMcHunger(p, v);
    }

    public static void modifyHunger(Player p, int delta) {
        setHunger(p, getHunger(p) + delta);
    }

    /** Force MC food level to match our hunger gate (active vs. depleted). */
    public static void syncMcHunger(Player p, int ourHunger) {
        p.setFoodLevel(ourHunger == 0 ? MC_HUNGER_DEPLETED : MC_HUNGER_ACTIVE);
    }

    // --- Thirst ---

    public static int getThirst(Player p) {
        Integer v = p.getPersistentDataContainer().get(thirstKey, PersistentDataType.INTEGER);
        return v != null ? v : DEFAULT;
    }

    public static void setThirst(Player p, int value) {
        p.getPersistentDataContainer().set(thirstKey, PersistentDataType.INTEGER, clamp(value));
    }

    public static void modifyThirst(Player p, int delta) {
        setThirst(p, getThirst(p) + delta);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(MAX, v)); }
}
