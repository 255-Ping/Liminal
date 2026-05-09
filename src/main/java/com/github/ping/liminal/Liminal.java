package com.github.ping.liminal;

import com.github.ping.liminal.command.LiminalWorldCommand;
import com.github.ping.liminal.listener.MobSuppressListener;
import com.github.ping.liminal.listener.PlayerRouterListener;
import com.github.ping.liminal.world.LevelGenerator;
import com.github.ping.liminal.world.LevelGeneratorRegistry;
import com.github.ping.liminal.world.level1.Level1Generator;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.PluginCommand;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class Liminal extends JavaPlugin {

    /** Hardcoded for v1; promote to config when there's more than one default level. */
    private static final String DEFAULT_LEVEL1_WORLD = "liminal_level1";

    private final LevelGeneratorRegistry levels = new LevelGeneratorRegistry();
    private World defaultLevel1;

    @Override
    public void onLoad() {
        // Registry must be populated before bukkit.yml-defined worlds open. STARTUP-load
        // means Bukkit calls getDefaultWorldGenerator between onLoad and onEnable.
        levels.register(new Level1Generator());
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MobSuppressListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerRouterListener(() -> defaultLevel1), this);

        PluginCommand cmd = getCommand("liminalworld");
        if (cmd != null) {
            LiminalWorldCommand exec = new LiminalWorldCommand(levels);
            cmd.setExecutor(exec);
            cmd.setTabCompleter(exec);
        }

        // World creation has to wait until the server finishes initializing — STARTUP-load
        // plugins are enabled before Bukkit's main worlds are ready, and CraftServer rejects
        // createWorld before then with IllegalStateException. Scheduling on the next tick
        // defers it past server load. PlayerRouterListener is null-tolerant in the gap.
        getServer().getScheduler().runTask(this, () -> defaultLevel1 = ensureDefaultLevel1World());
    }

    @Override
    public void onDisable() {
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (id == null || id.isEmpty()) return null;
        return levels.get(id).map(LevelGenerator::chunkGenerator).orElseGet(() -> {
            getLogger().warning("Unknown level generator id: " + id + " (world " + worldName + ")");
            return null;
        });
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(String worldName, String id) {
        if (id == null || id.isEmpty()) return null;
        return levels.get(id).map(LevelGenerator::biomeProvider).orElse(null);
    }

    public LevelGeneratorRegistry levels() {
        return levels;
    }

    public World defaultLevel1() {
        return defaultLevel1;
    }

    private World ensureDefaultLevel1World() {
        World existing = getServer().getWorld(DEFAULT_LEVEL1_WORLD);
        if (existing != null) return existing;
        LevelGenerator gen = levels.get("level1").orElseThrow();
        getLogger().info("Creating default Level 1 world: " + DEFAULT_LEVEL1_WORLD);
        return new WorldCreator(DEFAULT_LEVEL1_WORLD)
                .generator(gen.chunkGenerator())
                .biomeProvider(gen.biomeProvider())
                .createWorld();
    }
}
