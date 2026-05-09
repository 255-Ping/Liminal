package com.github.ping.liminal.world.level1;

import com.github.ping.liminal.world.LevelGenerator;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;

public final class Level1Generator implements LevelGenerator {
    @Override public String id() { return "level1"; }
    @Override public ChunkGenerator chunkGenerator() { return new Level1ChunkGenerator(); }
    @Override public BiomeProvider biomeProvider()   { return new Level1BiomeProvider(); }
}
