package dev.coffer.adapter.fabric.config;

import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Collection;
import java.util.Optional;

/**
 * Item/tag blacklist configuration for adapter policy.
 *
 * Structure (config/coffer/blacklist.yaml):
 * denyTags: ["minecraft:planks"]
 * denyItems: ["minecraft:bedrock"]
 *
 * JSON is still accepted for compatibility.
 *
 * Rules:
 * - If file is absent/empty, nothing is blacklisted.
 * - Blacklist denial is absolute regardless of valuation.
 */
public final class ItemBlacklistConfig {

    private static final String CONFIG_SUBDIR = "coffer";
    private static final String CONFIG_FILE_YAML = "blacklist.yaml";
    private static final String CONFIG_FILE_YML = "blacklist.yml";
    private static final String CONFIG_FILE_JSON = "blacklist.json";
    private static final Yaml YAML = new Yaml(new SafeConstructor(new LoaderOptions()));

    private final Set<String> denyTags;
    private final Set<String> denyItems;

    private ItemBlacklistConfig(Set<String> denyTags, Set<String> denyItems) {
        this.denyTags = denyTags;
        this.denyItems = denyItems;
    }

    public static ItemBlacklistConfig load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_SUBDIR);

        Map<String, Object> root = CofferConfigLoader.loadRoot();
        if (root != null) {
            ItemBlacklistConfig fromRoot = fromObject(CofferConfigLoader.section(root, "blacklist")).orElse(null);
            if (fromRoot != null) {
                return fromRoot;
            }
        }

        ItemBlacklistConfig fromYaml = loadYaml(configDir.resolve(CONFIG_FILE_YAML));
        if (fromYaml != null) {
            return fromYaml;
        }

        ItemBlacklistConfig fromYml = loadYaml(configDir.resolve(CONFIG_FILE_YML));
        if (fromYml != null) {
            return fromYml;
        }

        ItemBlacklistConfig fromJson = loadJson(configDir.resolve(CONFIG_FILE_JSON));
        if (fromJson != null) {
            return fromJson;
        }

        return empty();
    }

    public static Optional<ItemBlacklistConfig> fromObject(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Optional.empty();
        }
        Set<String> denyTags = readStringSet(map.get("denyTags"));
        Set<String> denyItems = readStringSet(map.get("denyItems"));
        return Optional.of(new ItemBlacklistConfig(denyTags, denyItems));
    }

    private static ItemBlacklistConfig loadYaml(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Object loaded = YAML.load(reader);
            if (!(loaded instanceof java.util.Map<?, ?> map)) {
                return null;
            }
            Set<String> denyTags = readStringSet(map.get("denyTags"));
            Set<String> denyItems = readStringSet(map.get("denyItems"));
            return new ItemBlacklistConfig(denyTags, denyItems);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static ItemBlacklistConfig loadJson(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Object loaded = new com.google.gson.Gson().fromJson(reader, Object.class);
            if (!(loaded instanceof java.util.Map<?, ?> map)) {
                return null;
            }
            Set<String> denyTags = readStringSet(map.get("denyTags"));
            Set<String> denyItems = readStringSet(map.get("denyItems"));
            return new ItemBlacklistConfig(denyTags, denyItems);
        } catch (IOException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<String> readStringSet(Object value) {
        if (value == null) return Collections.emptySet();
        if (value instanceof Collection<?> collection) {
            Set<String> out = new HashSet<>();
            for (Object o : collection) {
                if (o instanceof String s && !s.isBlank()) {
                    out.add(s);
                }
            }
            return Collections.unmodifiableSet(out);
        }
        return Collections.emptySet();
    }

    public static ItemBlacklistConfig empty() {
        return new ItemBlacklistConfig(Collections.emptySet(), Collections.emptySet());
    }

    public int denyItemCount() {
        return denyItems.size();
    }

    public int denyTagCount() {
        return denyTags.size();
    }

    public boolean isDenied(String itemId, Set<String> itemTags) {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(itemTags, "itemTags");
        if (denyItems.contains(itemId)) return true;
        for (String tag : itemTags) {
            if (denyTags.contains(tag)) return true;
        }
        return false;
    }
}
