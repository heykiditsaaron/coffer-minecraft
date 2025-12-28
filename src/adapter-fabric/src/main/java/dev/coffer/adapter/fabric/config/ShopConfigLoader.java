package dev.coffer.adapter.fabric.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ShopConfigLoader {

    private static final String CONFIG_SUBDIR = "coffer/shops";
    private static final Gson GSON = new Gson();

    private ShopConfigLoader() {
        // utility
    }

    public static ShopCatalog load() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_SUBDIR);
        if (!Files.exists(dir)) {
            return new ShopCatalog(List.of());
        }

        List<ShopDefinition> defs = new ArrayList<>();
        try {
            try (var files = Files.list(dir)) {
                files.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                    ShopDefinition def = loadSingle(path);
                    if (def != null) defs.add(def);
                });
            }
        } catch (IOException ignored) {
            // treat as no shops
        }

        return new ShopCatalog(defs);
    }

    private static ShopDefinition loadSingle(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement element = GSON.fromJson(reader, JsonElement.class);
            if (element == null || !element.isJsonObject()) return null;
            JsonObject obj = element.getAsJsonObject();
            String baseName = stripExtension(path.getFileName().toString());
            String id = obj.has("id") ? obj.get("id").getAsString() : baseName;
            if (id == null || id.isBlank()) {
                id = baseName;
            }
            String name = obj.has("name") ? obj.get("name").getAsString() : baseName;
            List<ShopEntry> entries = new ArrayList<>();
            if (obj.has("entries")) {
                JsonArray arr = obj.getAsJsonArray("entries");
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject eo = el.getAsJsonObject();
                    String target = eo.has("target") ? eo.get("target").getAsString() : null;
                    String kindStr = eo.has("kind") ? eo.get("kind").getAsString() : "item";
                    double mult = eo.has("multiplier") ? eo.get("multiplier").getAsDouble() : 1.0;
                    long add = eo.has("additive") ? eo.get("additive").getAsLong() : 0L;
                    long buyPrice = eo.has("buyPrice") ? eo.get("buyPrice").getAsLong() : 0L;
                    String buyCurrency = eo.has("buyCurrency") ? eo.get("buyCurrency").getAsString() : null;
                    int buyQty = eo.has("buyQuantity") ? eo.get("buyQuantity").getAsInt() : 1;
                    long sellPrice = eo.has("sellPrice") ? eo.get("sellPrice").getAsLong() : 0L;
                    String sellCurrency = eo.has("sellCurrency") ? eo.get("sellCurrency").getAsString() : null;
                    int sellQty = eo.has("sellQuantity") ? eo.get("sellQuantity").getAsInt() : 1;
                    Integer slot = eo.has("slot") ? eo.get("slot").getAsInt() : null;
                    Integer page = eo.has("page") ? eo.get("page").getAsInt() : null;
                    ShopEntry.Kind kind = "tag".equalsIgnoreCase(kindStr) ? ShopEntry.Kind.TAG : ShopEntry.Kind.ITEM;
                    try {
                        entries.add(new ShopEntry(target, kind, mult, add, buyPrice, buyCurrency, buyQty, sellPrice, sellCurrency, sellQty, slot, page));
                    } catch (Exception ignored) {
                        // skip malformed entry
                    }
                }
            }
            return new ShopDefinition(id, name, entries);
        } catch (IOException e) {
            return null;
        }
    }

    private static String stripExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(0, idx) : fileName;
    }
}
