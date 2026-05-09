package com.github.ping.liminal.world.level1;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

public final class Level1ChunkGenerator extends ChunkGenerator {

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        Level1Layout.generate(worldInfo.getSeed(), chunkX, chunkZ, chunkData);
    }

    @Override public boolean shouldGenerateNoise()        { return true;  }
    @Override public boolean shouldGenerateSurface()      { return false; }
    @Override public boolean shouldGenerateBedrock()      { return false; }
    @Override public boolean shouldGenerateCaves()        { return false; }
    @Override public boolean shouldGenerateDecorations()  { return false; }
    @Override public boolean shouldGenerateMobs()         { return false; }
    @Override public boolean shouldGenerateStructures()   { return false; }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        // Cell (0, 0) interior: lx=1, lz=1. Stand on floor 1, look east into the maze.
        return new Location(world, 1.5, Level1Layout.FLOOR_BASE + 1, 1.5, -90f, 0f);
    }
}
