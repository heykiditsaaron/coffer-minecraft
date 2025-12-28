package dev.coffer.adapter.fabric.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * VALUATION CONFIG LOADER
 *
 * Responsibility:
 * - Load valuation configuration from JSON at config/coffer-valuation.json.
 * - Deny-by-default when missing or malformed.
 *
 * Invariants:
 * - Returns ValuationConfig.empty() on any load/parse issue.
 * - Only accepts positive long values.
 */
public final class ValuationConfigLoader {

    private static final String CONFIG_SUBDIR = "coffer";
    private static final String CONFIG_FILE = "valuation.json";
    private static final Gson GSON = new Gson();

    private ValuationConfigLoader() {
        // utility
    }

    public static ValuationConfig load(String defaultCurrencyId) {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_SUBDIR);
        Path path = configDir.resolve(CONFIG_FILE);

        if (!Files.exists(path)) {
            return ValuationConfig.empty();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement element = GSON.fromJson(reader, JsonElement.class);
            if (element == null || !element.isJsonObject()) {
                return ValuationConfig.empty();
            }

            JsonObject obj = element.getAsJsonObject();
            if (obj.has("rules") && obj.get("rules").isJsonArray()) {
                List<ValuationConfig.Rule> rules = new ArrayList<>();
                for (JsonElement el : obj.get("rules").getAsJsonArray()) {
                    if (!el.isJsonObject()) continue;
                    ValuationConfig.Rule rule = parseRule(el.getAsJsonObject(), defaultCurrencyId);
                    if (rule != null) {
                        rules.add(rule);
                    }
                }
                return rules.isEmpty() ? ValuationConfig.empty() : new ValuationConfig(rules);
            }

            // legacy flat map
            Map<String, Long> values = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String itemId = entry.getKey();
                JsonElement valueElem = entry.getValue();
                if (!valueElem.isJsonPrimitive() || !valueElem.getAsJsonPrimitive().isNumber()) {
                    continue;
                }
                long val = valueElem.getAsLong();
                if (val > 0) {
                    values.put(itemId, val);
                }
            }
            if (values.isEmpty()) {
                return ValuationConfig.empty();
            }
            List<ValuationConfig.Rule> legacyRules = new ArrayList<>();
            for (Map.Entry<String, Long> e : values.entrySet()) {
                legacyRules.add(new ValuationConfig.Rule(
                        ValuationConfig.Type.ITEM,
                        e.getKey(),
                        defaultCurrencyId,
                        e.getValue(),
                        0
                ));
            }
            return new ValuationConfig(legacyRules);
        } catch (IOException e) {
            return ValuationConfig.empty();
        }
    }

    private static ValuationConfig.Rule parseRule(JsonObject obj, String defaultCurrencyId) {
        String typeStr = obj.has("type") ? obj.get("type").getAsString() : "item";
        ValuationConfig.Type type = switch (typeStr.toLowerCase()) {
            case "item" -> ValuationConfig.Type.ITEM;
            case "tag" -> ValuationConfig.Type.TAG;
            case "default" -> ValuationConfig.Type.DEFAULT;
            default -> null;
        };
        if (type == null) return null;
        String id = null;
        if (type != ValuationConfig.Type.DEFAULT) {
            if (!obj.has("id") || obj.get("id").getAsString().isBlank()) return null;
            id = obj.get("id").getAsString();
        }
        long value = obj.has("value") ? obj.get("value").getAsLong() : -1;
        if (value <= 0) return null;
        String currencyId = obj.has("currency") ? obj.get("currency").getAsString() : defaultCurrencyId;
        if (currencyId == null || currencyId.isBlank()) return null;
        int priority = obj.has("priority") ? obj.get("priority").getAsInt() : 0;
        return new ValuationConfig.Rule(type, id, currencyId, value, priority);
    }
}
