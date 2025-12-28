package dev.coffer.adapter.fabric.config;

import java.util.Collections;
import java.util.List;

public final class ShopCatalog {
    private final List<ShopDefinition> shops;

    public ShopCatalog(List<ShopDefinition> shops) {
        this.shops = Collections.unmodifiableList(shops);
    }

    public List<ShopDefinition> shops() {
        return shops;
    }
}
