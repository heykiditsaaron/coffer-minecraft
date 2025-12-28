package dev.coffer.adapter.fabric.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Ensures default config files exist with safe, honest defaults.
 */
public final class ConfigBootstrap {

    private static final String COFFER_DIR = "coffer";

    private ConfigBootstrap() {
        // utility
    }

    public static void ensureDefaults() throws IOException {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(COFFER_DIR);
        Files.createDirectories(configDir);

        Path valuation = configDir.resolve("valuation.json");
        Path configYaml = configDir.resolve("config.yaml");
        Path shopsDir = configDir.resolve("shops");
        Path exampleShop = shopsDir.resolve("example_shop.json");

        if (!Files.exists(valuation)) {
            Files.writeString(valuation,
                    "{\n" +
                            "  \"rules\": [\n" +
                            "    { \"type\": \"item\", \"id\": \"minecraft:dirt\", \"currency\": \"coins\", \"value\": 5, \"priority\": 10 },\n" +
                            "    { \"type\": \"tag\",  \"id\": \"minecraft:logs\", \"currency\": \"coins\", \"value\": 50, \"priority\": 5 },\n" +
                            "    { \"type\": \"default\", \"currency\": \"coins\", \"value\": 1, \"priority\": 0 }\n" +
                            "  ]\n" +
                            "}\n", StandardCharsets.UTF_8);
        }

        if (!Files.exists(configYaml)) {
            Files.writeString(configYaml, """
# Coffer consolidated configuration
# This file is optional; per-feature files are still supported.
# Paths are resolved under config/coffer/ unless absolute.

storage:
  # Storage backend: json (file) or sqlite (bundled JDBC).
  type: sqlite
  # Relative path for the chosen backend (e.g., balances.json or balances.db).
  path: balances.db

currency:
  # Default currency id, must match one from the list below.
  default: coins
  # All values are minor units; decimals describes the minor-unit scale.
  currencies:
    - id: coins
      name: Coin
      plural: Coins
      symbol: ""        # optional prefix symbol
      decimals: 0       # 0 = whole units only

blacklist:
  # Items or tags listed here are denied before evaluation.
  denyTags: []
  denyItems: []

permissions:
  # If true and Fabric Permissions API is present, use it; otherwise fallback to op levels.
  useFabricPermissionsApi: true

logging:
  # Emit audits to console
  console: true
  # Also emit audits to this file (relative to config/coffer/ or absolute). Leave blank to disable file logging.
  file: ""
  # How many recent audits to keep in memory for /coffer audits
  maxRecords: 50
""", StandardCharsets.UTF_8);
        }

        Files.createDirectories(shopsDir);
        if (!Files.exists(exampleShop)) {
            Files.writeString(exampleShop, "{\n" +
                    "  \"name\": \"Example Shop\",\n" +
                    "  \"entries\": [\n" +
                    "    {\n" +
                    "      \"target\": \"minecraft:dirt\",\n" +
                    "      \"kind\": \"item\",\n" +
                    "      \"multiplier\": 1.0,\n" +
                    "      \"additive\": 0\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}", StandardCharsets.UTF_8);
        }
    }
}
