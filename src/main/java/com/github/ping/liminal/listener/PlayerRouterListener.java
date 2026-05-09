package com.github.ping.liminal.listener;

import com.github.ping.liminal.world.LiminalChunkGenerator;
import java.util.function.Supplier;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Routes players into a Liminal world automatically.
 *
 *  - Join: any player whose current world is not Liminal-managed is teleported to the
 *    default Level 1 spawn. Returning players who were last in a Liminal world stay put.
 *  - Respawn: if the natural respawn target (vanilla bed / world spawn) is non-Liminal,
 *    redirect to the default Level 1 spawn — death drops players back into the backrooms,
 *    not the overworld.
 */
public final class PlayerRouterListener implements Listener {

    private final Supplier<World> defaultLevel;

    public PlayerRouterListener(Supplier<World> defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().getWorld().getGenerator() instanceof LiminalChunkGenerator) return;
        World w = defaultLevel.get();
        if (w == null) return;
        e.getPlayer().teleportAsync(w.getSpawnLocation());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent e) {
        if (e.getRespawnLocation().getWorld().getGenerator() instanceof LiminalChunkGenerator) return;
        World w = defaultLevel.get();
        if (w == null) return;
        e.setRespawnLocation(w.getSpawnLocation());
    }
}
