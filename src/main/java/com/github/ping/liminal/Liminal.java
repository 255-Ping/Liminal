package com.github.ping.liminal;

import com.github.ping.liminal.command.LiminalWorldCommand;
import com.github.ping.liminal.world.LevelGenerator;
import com.github.ping.liminal.world.LevelGeneratorRegistry;
import com.github.ping.liminal.world.level1.Level1Generator;
import org.bukkit.command.PluginCommand;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class Liminal extends JavaPlugin {

    private final LevelGeneratorRegistry levels = new LevelGeneratorRegistry();

    @Override
    public void onLoad() {
        // Register in onLoad — STARTUP-load means Bukkit asks getDefaultWorldGenerator
        // for bukkit.yml-defined worlds before onEnable fires.
        levels.register(new Level1Generator());
    }

    @Override
    public void onEnable() {
        PluginCommand cmd = getCommand("liminalworld");
        if (cmd != null) {
            LiminalWorldCommand exec = new LiminalWorldCommand(levels);
            cmd.setExecutor(exec);
            cmd.setTabCompleter(exec);
        }
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
}
