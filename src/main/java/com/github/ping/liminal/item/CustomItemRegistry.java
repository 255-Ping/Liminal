package com.github.ping.liminal.item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CustomItemRegistry {

    private final Map<String, CustomItem> byId = new HashMap<>();

    public void register(CustomItem item) {
        byId.put(item.id(), item);
    }

    public Optional<CustomItem> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }

    public Set<String> ids() {
        return Collections.unmodifiableSet(byId.keySet());
    }
}
