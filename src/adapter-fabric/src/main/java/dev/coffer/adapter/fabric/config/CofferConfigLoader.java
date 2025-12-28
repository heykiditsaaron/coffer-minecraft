package dev.coffer.adapter.fabric.config;

import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Loads the consolidated config/coffer/config.yaml when present.
 * Sections are optional; individual loaders still fall back to their legacy files.
 */
public final class CofferConfigLoader {

    private static final String CONFIG_SUBDIR = "coffer";
    private static final String CONFIG_FILE = "config.yaml";
    private static final Yaml YAML = new Yaml(new SafeConstructor(new LoaderOptions()));

    private CofferConfigLoader() {
        // utility
    }

    public static Map<String, Object> loadRoot() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_SUBDIR);
        Path path = configDir.resolve(CONFIG_FILE);
        if (!Files.exists(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            Object obj = YAML.load(reader);
            if (obj instanceof Map<?, ?> map) {
                //noinspection unchecked
                return (Map<String, Object>) map;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static Object section(Map<String, Object> root, String key) {
        if (root == null || key == null) return null;
        return root.get(key);
    }
}
