package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.config.PermissionConfig;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Method;

/**
 * Permission helper that optionally integrates with Fabric Permissions API (LuckPerms-compatible)
 * and falls back to vanilla op-level checks.
 */
public final class PermissionGate {
    private static final boolean HAS_PERMISSIONS_API;
    private static final Method PERMISSIONS_CHECK;

    static {
        Method check = null;
        boolean present = false;
        try {
            Class<?> cls = Class.forName("net.fabricmc.fabric.api.permission.v1.Permissions");
            check = cls.getMethod("check", ServerCommandSource.class, String.class, int.class);
            present = true;
        } catch (ClassNotFoundException ignored) {
            // permissions API not present
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to reflect Permissions.check", e);
        }
        PERMISSIONS_CHECK = check;
        HAS_PERMISSIONS_API = present;
    }

    private PermissionGate() {
        // utility
    }

    public static boolean hasPermission(ServerCommandSource source, String permission, int opLevelFallback) {
        PermissionConfig cfg = PermissionConfig.load();
        int effectiveOpLevel = cfg.resolveOpLevel(permission, opLevelFallback);

        if (HAS_PERMISSIONS_API && cfg.useFabricPermissionsApi()) {
            try {
                return (Boolean) PERMISSIONS_CHECK.invoke(null, source, permission, effectiveOpLevel);
            } catch (ReflectiveOperationException e) {
                // fallback to op level
            }
        }
        return source.hasPermissionLevel(effectiveOpLevel);
    }
}
