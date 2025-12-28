package dev.coffer.adapter.fabric.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Permissions configuration sourced from config/coffer/config.yaml (permissions section).
 * Falls back to hardcoded defaults when absent.
 */
public final class PermissionConfig {

    private static final Map<String, Integer> DEFAULT_OP_LEVELS;
    static {
        Map<String, Integer> defaults = new HashMap<>();
        defaults.put("coffer.command.ready", 0);
        defaults.put("coffer.command.audits", 2);
        defaults.put("coffer.command.reload", 2);
        defaults.put("coffer.command.sell.execute", 0);
        defaults.put("coffer.command.shop.open", 0);
        defaults.put("coffer.command.shop.price", 0);
        defaults.put("coffer.command.shop.buy", 0);
        defaults.put("coffer.command.balance.self", 0);
        defaults.put("coffer.command.balance.others", 2);
        defaults.put("coffer.command.credit", 2);
        defaults.put("coffer.command.debit", 2);
        DEFAULT_OP_LEVELS = Collections.unmodifiableMap(defaults);
    }

    private final boolean useFabricPermissionsApi;
    private final Map<String, Integer> opLevels;

    private PermissionConfig(boolean useFabricPermissionsApi, Map<String, Integer> opLevels) {
        this.useFabricPermissionsApi = useFabricPermissionsApi;
        this.opLevels = opLevels;
    }

    public static PermissionConfig load() {
        Map<String, Object> root = CofferConfigLoader.loadRoot();
        if (root != null) {
            Object permissionsSection = CofferConfigLoader.section(root, "permissions");
            if (permissionsSection instanceof Map<?, ?> map) {
                boolean useApi = asBoolean(map.get("useFabricPermissionsApi"));
                Map<String, Integer> levels = readOpLevels(map.get("defaults"));
                return new PermissionConfig(useApi, levels);
            }
        }
        return new PermissionConfig(true, DEFAULT_OP_LEVELS);
    }

    public boolean useFabricPermissionsApi() {
        return useFabricPermissionsApi;
    }

    public int resolveOpLevel(String permissionNode, int fallback) {
        Objects.requireNonNull(permissionNode, "permissionNode");
        return opLevels.getOrDefault(permissionNode, fallback);
    }

    private static Map<String, Integer> readOpLevels(Object obj) {
        if (!(obj instanceof Map<?, ?> map)) {
            return DEFAULT_OP_LEVELS;
        }
        Map<String, Integer> resolved = new HashMap<>(DEFAULT_OP_LEVELS);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object val = entry.getValue();
            if (key == null || !(key instanceof String node)) continue;
            if (val instanceof Number n) {
                resolved.put(node, n.intValue());
            } else if (val instanceof String s) {
                try {
                    resolved.put(node, Integer.parseInt(s));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return Collections.unmodifiableMap(resolved);
    }

    private static boolean asBoolean(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return true;
    }
}
