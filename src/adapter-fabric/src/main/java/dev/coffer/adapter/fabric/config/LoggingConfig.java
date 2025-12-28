package dev.coffer.adapter.fabric.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Map;

/**
 * Logging configuration for audits and diagnostics.
 */
public final class LoggingConfig {
    private final boolean consoleEnabled;
    private final String filePath; // relative to config/coffer unless absolute
    private final int maxRecords;

    private LoggingConfig(boolean consoleEnabled, String filePath, int maxRecords) {
        this.consoleEnabled = consoleEnabled;
        this.filePath = filePath;
        this.maxRecords = maxRecords;
    }

    public boolean consoleEnabled() {
        return consoleEnabled;
    }

    public String filePath() {
        return filePath;
    }

    public Path resolveFilePath() {
        if (filePath == null || filePath.isBlank()) return null;
        Path p = Path.of(filePath);
        if (p.isAbsolute()) return p;
        return FabricLoader.getInstance().getConfigDir().resolve("coffer").resolve(p);
    }

    public int maxRecords() {
        return maxRecords;
    }

    public static LoggingConfig load() {
        Map<String, Object> root = CofferConfigLoader.loadRoot();
        if (root != null) {
            Object section = CofferConfigLoader.section(root, "logging");
            if (section instanceof Map<?, ?> map) {
                boolean console = asBoolean(map.get("console"));
                String path = asString(map.get("file"), null);
                int max = asInt(map.get("maxRecords"), 50);
                return new LoggingConfig(console, path, max);
            }
        }
        return defaultConfig();
    }

    public static LoggingConfig defaultConfig() {
        return new LoggingConfig(true, null, 50);
    }

    private static boolean asBoolean(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof String s) return Boolean.parseBoolean(s);
        return true;
    }

    private static String asString(Object o, String defaultVal) {
        if (o == null) return defaultVal;
        String s = o.toString();
        return s.isBlank() ? defaultVal : s;
    }

    private static int asInt(Object o, int defaultVal) {
        if (o instanceof Number n) return n.intValue();
        if (o instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }
}
