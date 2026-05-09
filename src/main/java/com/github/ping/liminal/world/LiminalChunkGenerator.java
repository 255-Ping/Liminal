package com.github.ping.liminal.world;

import org.bukkit.generator.ChunkGenerator;

/**
 * Marker base class for all Liminal chunk generators. Used by listeners (e.g. mob
 * suppression) to recognise a world as Liminal-managed without hard-coding a list of
 * level ids: any `world.getGenerator() instanceof LiminalChunkGenerator` is one of ours.
 *
 * Concrete generators (Level1ChunkGenerator, future levels) extend this instead of
 * ChunkGenerator directly.
 */
public abstract class LiminalChunkGenerator extends ChunkGenerator {
}
