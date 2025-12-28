package dev.coffer.adapter.fabric.config;

import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Currency configuration.
 *
 * Structure (config/coffer/currency.yaml):
 * default: "coins"
 * currencies:
 *   - id: "coins"
 *     name: "Coin"
 *     plural: "Coins"
 *     symbol: ""
 *     decimals: 0
 *
 * JSON is still accepted for compatibility.
 */
public final class CurrencyConfig {

    private static final String CONFIG_SUBDIR = "coffer";
    private static final String CONFIG_FILE_YAML = "currency.yaml";
    private static final String CONFIG_FILE_YML = "currency.yml";
    private static final String CONFIG_FILE_JSON = "currency.json";
    private static final Yaml YAML = new Yaml(new SafeConstructor(new LoaderOptions()));

    private final String defaultCurrencyId;
    private final Map<String, CurrencyDefinition> currencies;

    private CurrencyConfig(String defaultCurrencyId, Map<String, CurrencyDefinition> currencies) {
        this.defaultCurrencyId = defaultCurrencyId;
        this.currencies = Collections.unmodifiableMap(new HashMap<>(currencies));
    }

    public CurrencyDefinition defaultCurrency() {
        CurrencyDefinition def = currencies.get(defaultCurrencyId);
        if (def != null) return def;
        return currencies.values().stream().findFirst().orElse(defaultCurrencyDefinition());
    }

    public Optional<CurrencyDefinition> find(String currencyId) {
        if (currencyId == null || currencyId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(currencies.get(currencyId));
    }

    public int currencyCount() {
        return currencies.size();
    }

    public Set<String> currencyIds() {
        return Collections.unmodifiableSet(currencies.keySet());
    }

    public static CurrencyConfig load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_SUBDIR);

        Map<String, Object> root = CofferConfigLoader.loadRoot();
        if (root != null) {
            CurrencyConfig fromRoot = fromObject(CofferConfigLoader.section(root, "currency")).orElse(null);
            if (fromRoot != null) {
                return fromRoot;
            }
        }

        CurrencyConfig fromYaml = loadYaml(configDir.resolve(CONFIG_FILE_YAML));
        if (fromYaml != null) {
            return fromYaml;
        }

        CurrencyConfig fromYml = loadYaml(configDir.resolve(CONFIG_FILE_YML));
        if (fromYml != null) {
            return fromYml;
        }

        CurrencyConfig fromJson = loadJson(configDir.resolve(CONFIG_FILE_JSON));
        if (fromJson != null) {
            return fromJson;
        }

        return defaultConfig();
    }

    public static Optional<CurrencyConfig> fromObject(Object loaded) {
        CurrencyConfig cfg = parseMap(loaded);
        return Optional.ofNullable(cfg);
    }

    private static CurrencyConfig loadYaml(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Object loaded = YAML.load(reader);
            return parseMap(loaded);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static CurrencyConfig loadJson(Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            Object loaded = new com.google.gson.Gson().fromJson(reader, Object.class);
            return parseMap(loaded);
        } catch (IOException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static CurrencyConfig parseMap(Object loaded) {
        if (!(loaded instanceof Map<?, ?> obj)) {
            return null;
        }
        String defaultId = asString(obj.get("default"), "coins");
        Map<String, CurrencyDefinition> defs = new HashMap<>();
        Object currenciesNode = obj.get("currencies");
        if (currenciesNode instanceof Collection<?> coll) {
            for (Object el : coll) {
                parseCurrency(el).ifPresent(def -> defs.put(def.id(), def));
            }
        }

        if (defs.isEmpty()) {
            return defaultConfig();
        }

        return new CurrencyConfig(defaultId, defs);
    }

    @SuppressWarnings("unchecked")
    private static Optional<CurrencyDefinition> parseCurrency(Object el) {
        if (!(el instanceof Map<?, ?> obj)) return Optional.empty();
        String id = asString(obj.get("id"), "");
        if (id.isBlank()) return Optional.empty();
        String name = asString(obj.get("name"), id);
        String plural = asString(obj.get("plural"), name + "s");
        String symbol = asString(obj.get("symbol"), "");
        int decimals = 0;
        Object decObj = obj.get("decimals");
        if (decObj instanceof Number n) {
            decimals = Math.max(0, n.intValue());
        }
        return Optional.of(new CurrencyDefinition(id, name, plural, symbol, decimals));
    }

    private static String asString(Object o, String defaultVal) {
        if (o == null) return defaultVal;
        String s = Objects.toString(o);
        return s == null ? defaultVal : s;
    }

    private static CurrencyDefinition defaultCurrencyDefinition() {
        return new CurrencyDefinition("coins", "Coin", "Coins", "", 0);
    }

    public static CurrencyConfig defaultConfig() {
        CurrencyDefinition def = defaultCurrencyDefinition();
        return new CurrencyConfig(def.id(), Map.of(def.id(), def));
    }

    public record CurrencyDefinition(
            String id,
            String name,
            String pluralName,
            String symbol,
            int decimals
    ) {
        public CurrencyDefinition {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(pluralName, "pluralName");
            Objects.requireNonNull(symbol, "symbol");
            decimals = Math.max(0, decimals);
        }
    }
}
