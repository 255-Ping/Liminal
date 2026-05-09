package com.github.ping.liminal.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Central registry of LevelGenerators keyed by id (lowercased). Populated in
 * Liminal#onLoad so generators are resolvable when Bukkit is opening worlds for
 * STARTUP-load plugins.
 */
public final class LevelGeneratorRegistry {

    private final Map<String, LevelGenerator> byId = new HashMap<>();

    public void register(LevelGenerator gen) {
        byId.put(gen.id().toLowerCase(Locale.ROOT), gen);
    }

    public Optional<LevelGenerator> get(String id) {
        if (id == null || id.isEmpty()) return Optional.empty();
        return Optional.ofNullable(byId.get(id.toLowerCase(Locale.ROOT)));
    }

    public Set<String> ids() {
        return Collections.unmodifiableSet(byId.keySet());
    }
}
