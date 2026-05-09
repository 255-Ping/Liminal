package com.github.ping.liminal.world.level1;

import java.util.List;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public final class Level1BiomeProvider extends BiomeProvider {

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        return Biome.PLAINS;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return List.of(Biome.PLAINS);
    }
}
