package com.github.ping.liminal.world;

import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;

/**
 * One Liminal area / "level". Each level is its own world — they are not connected
 * to each other in the same world. A LevelGenerator bundles the ChunkGenerator and
 * BiomeProvider used to replace vanilla generation for that level.
 *
 * Levels are looked up by id (see LevelGeneratorRegistry) so that bukkit.yml entries
 * like `generator: Liminal:level1` resolve to the right impl.
 */
public interface LevelGenerator {

    /** Stable id used in bukkit.yml `generator: Liminal:&lt;id&gt;` and WorldCreator.generator(...). */
    String id();

    ChunkGenerator chunkGenerator();

    BiomeProvider biomeProvider();
}
