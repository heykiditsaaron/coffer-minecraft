package dev.coffer.adapter.fabric.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ShopDefinition {
    private final String id;
    private final String name;
    private final List<ShopEntry> entries;

    public ShopDefinition(String id, String name, List<ShopEntry> entries) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must be non-empty");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.entries = Collections.unmodifiableList(entries);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<ShopEntry> entries() {
        return entries;
    }
}
