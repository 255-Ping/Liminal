package com.github.ping.liminal.listener;

import com.github.ping.liminal.world.LiminalChunkGenerator;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * Suppress vanilla mob spawning in any Liminal-managed world.
 *
 * The whitelist below covers admin and system spawning paths so debug commands and
 * future Liminal-summoned entities still work:
 *  - CUSTOM        — programmatic spawn from our own code
 *  - COMMAND       — /summon
 *  - SPAWNER_EGG   — player using a spawn egg
 *  - SPAWNER       — mob spawner block (we'll place these for backrooms entities)
 *  - BUILD_*       — player-built iron golem / snow golem / wither
 *  - BREEDING      — animal breeding
 *
 * Everything else (NATURAL, CHUNK_GEN, JOCKEY, RAID, PATROL, etc.) is cancelled.
 */
public final class MobSuppressListener implements Listener {

    private static final Set<SpawnReason> ALLOWED = Set.of(
            SpawnReason.CUSTOM,
            SpawnReason.COMMAND,
            SpawnReason.SPAWNER_EGG,
            SpawnReason.SPAWNER,
            SpawnReason.BUILD_IRONGOLEM,
            SpawnReason.BUILD_SNOWMAN,
            SpawnReason.BUILD_WITHER,
            SpawnReason.BREEDING
    );

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!(e.getEntity().getWorld().getGenerator() instanceof LiminalChunkGenerator)) return;
        if (ALLOWED.contains(e.getSpawnReason())) return;
        e.setCancelled(true);
    }
}
