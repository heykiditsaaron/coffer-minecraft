package dev.coffer.adapter.fabric.execution;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * JSON-backed balance store (persistent file under config/coffer/ by default).
 *
 * Not for performance; intended as a simple, auditable default.
 */
public final class JsonBalanceStore implements BalanceStore {

    private static final Gson GSON = new Gson();

    private final Path path;
    private final String defaultCurrencyId;
    private final Map<UUID, Map<String, Long>> balances = new HashMap<>();

    public JsonBalanceStore(Path path, String defaultCurrencyId) throws BalanceStoreException {
        this.path = path;
        if (defaultCurrencyId == null || defaultCurrencyId.isBlank()) {
            throw new IllegalArgumentException("defaultCurrencyId must be non-empty");
        }
        this.defaultCurrencyId = defaultCurrencyId;
        load();
    }

    @Override
    public synchronized long getBalance(UUID account, String currencyId) throws BalanceStoreException {
        load(); // ensure freshness
        Map<String, Long> byCurrency = balances.getOrDefault(account, Map.of());
        return byCurrency.getOrDefault(currencyId, 0L);
    }

    @Override
    public synchronized void applyDelta(UUID account, String currencyId, long delta) throws BalanceStoreException {
        load(); // ensure freshness
        Map<String, Long> byCurrency = balances.computeIfAbsent(account, k -> new HashMap<>());
        long current = byCurrency.getOrDefault(currencyId, 0L);
        long updated = current + delta;
        byCurrency.put(currencyId, updated);
        save();
    }

    private void load() throws BalanceStoreException {
        if (!Files.exists(path)) {
            ensureParent();
            save(); // create empty file
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement element = GSON.fromJson(reader, JsonElement.class);
            balances.clear();
            if (element != null && element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    try {
                        UUID id = UUID.fromString(entry.getKey());
                        JsonElement valueElement = entry.getValue();
                        Map<String, Long> currencyMap = new HashMap<>();
                        if (valueElement.isJsonObject()) {
                            JsonObject currencyObj = valueElement.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> c : currencyObj.entrySet()) {
                                try {
                                    long v = c.getValue().getAsLong();
                                    currencyMap.put(c.getKey(), v);
                                } catch (Exception ignored) {
                                }
                            }
                        } else if (valueElement.isJsonPrimitive()) {
                            // legacy format: single long applies to default currency
                            try {
                                long v = valueElement.getAsLong();
                                currencyMap.put(defaultCurrencyId, v);
                            } catch (Exception ignored) {
                            }
                        }
                        balances.put(id, currencyMap);
                    } catch (Exception ignored) {
                        // skip malformed entries
                    }
                }
            }
        } catch (IOException e) {
            throw new BalanceStoreException("Failed to read balances", e);
        }
    }

    private void save() throws BalanceStoreException {
        ensureParent();
        JsonObject obj = new JsonObject();
        for (Map.Entry<UUID, Map<String, Long>> entry : balances.entrySet()) {
            JsonObject currencyObj = new JsonObject();
            for (Map.Entry<String, Long> c : entry.getValue().entrySet()) {
                currencyObj.addProperty(c.getKey(), c.getValue());
            }
            obj.add(entry.getKey().toString(), currencyObj);
        }
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(obj, writer);
        } catch (IOException e) {
            throw new BalanceStoreException("Failed to write balances", e);
        }
    }

    private void ensureParent() throws BalanceStoreException {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new BalanceStoreException("Failed to create storage directory", e);
        }
    }
}
