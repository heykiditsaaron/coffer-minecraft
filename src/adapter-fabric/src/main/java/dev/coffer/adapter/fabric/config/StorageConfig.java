package dev.coffer.adapter.fabric.config;

import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;

/**
 * Storage configuration loader.
 *
 * Structure (config/coffer/storage.yaml):
 * type: "json"
 * path: "balances.json"
 *
 * JSON is still accepted for compatibility.
 */
public final class StorageConfig {

    public enum Type {
        JSON,
        SQLITE
    }

    private static final String CONFIG_SUBDIR = "coffer";
    private static final String CONFIG_FILE_YAML = "storage.yaml";
    private static final String CONFIG_FILE_YML = "storage.yml";
    private static final String CONFIG_FILE_JSON = "storage.json";
    private static final Yaml YAML = new Yaml(new SafeConstructor(new LoaderOptions()));

    public final Type type;
    public final String path;

    private StorageConfig(Type type, String path) {
        this.type = type;
        this.path = path;
    }

    public static StorageConfig load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_SUBDIR);

        Map<String, Object> root = CofferConfigLoader.loadRoot();
        if (root != null) {
            StorageConfig fromRoot = fromObject(CofferConfigLoader.section(root, "storage")).orElse(null);
            if (fromRoot != null) {
                return fromRoot;
            }
        }

        StorageConfig fromYaml = loadYaml(configDir.resolve(CONFIG_FILE_YAML));
        if (fromYaml != null) {
            return fromYaml;
        }

        StorageConfig fromYml = loadYaml(configDir.resolve(CONFIG_FILE_YML));
        if (fromYml != null) {
            return fromYml;
        }

        StorageConfig fromJson = loadJson(configDir.resolve(CONFIG_FILE_JSON));
        if (fromJson != null) {
            return fromJson;
        }

        return defaultConfig();
    }

    public static Optional<StorageConfig> fromObject(Object value) {
        StorageConfig cfg = parseMap(value);
        return Optional.ofNullable(cfg);
    }

    private static StorageConfig loadYaml(Path path) {
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

    private static StorageConfig loadJson(Path path) {
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
    private static StorageConfig parseMap(Object loaded) {
        if (!(loaded instanceof java.util.Map<?, ?> map)) {
            return null;
        }
        String typeStr = asString(map.get("type"), "json");
        String filePath = asString(map.get("path"), "balances.json");
        Type type = parseType(typeStr);
        if (filePath.isBlank()) {
            filePath = type == Type.SQLITE ? "balances.db" : "balances.json";
        }
        return new StorageConfig(type, filePath);
    }

    private static Type parseType(String typeStr) {
        if ("sqlite".equalsIgnoreCase(typeStr)) {
            return Type.SQLITE;
        }
        return Type.JSON;
    }

    private static String asString(Object o, String defaultVal) {
        if (o == null) return defaultVal;
        String s = Objects.toString(o);
        return s == null ? defaultVal : s;
    }

    public static StorageConfig defaultConfig() {
        return new StorageConfig(Type.SQLITE, "balances.db");
    }
}
