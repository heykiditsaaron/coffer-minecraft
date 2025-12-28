package dev.coffer.adapter.fabric.execution;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * SQLite-backed balance store (JDBC).
 *
 * Notes:
 * - Uses a single table with composite primary key (account, currency).
 * - Applies WAL and busy_timeout for basic concurrency safety.
 * - Throws BalanceStoreException if database is unreachable; caller must refuse READY.
 */
public final class SqliteBalanceStore implements BalanceStore {

    private final Path path;
    private final String defaultCurrencyId;
    private final String jdbcUrl;

    public SqliteBalanceStore(Path path, String defaultCurrencyId) throws BalanceStoreException {
        if (path == null) {
            throw new IllegalArgumentException("path must be non-null");
        }
        if (defaultCurrencyId == null || defaultCurrencyId.isBlank()) {
            throw new IllegalArgumentException("defaultCurrencyId must be non-empty");
        }
        this.path = path;
        this.defaultCurrencyId = defaultCurrencyId;
        this.jdbcUrl = "jdbc:sqlite:" + path.toString();
        initialize();
    }

    @Override
    public synchronized long getBalance(UUID account, String currencyId) throws BalanceStoreException {
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(currencyId, "currencyId");
        ensureExists();

        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT balance FROM balances WHERE account = ? AND currency = ?"
             )) {
            ps.setString(1, account.toString());
            ps.setString(2, currencyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new BalanceStoreException("Failed to read balance", e);
        }
    }

    @Override
    public synchronized void applyDelta(UUID account, String currencyId, long delta) throws BalanceStoreException {
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(currencyId, "currencyId");
        ensureExists();

        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement select = conn.prepareStatement(
                    "SELECT balance FROM balances WHERE account = ? AND currency = ?"
            )) {
                select.setString(1, account.toString());
                select.setString(2, currencyId);
                long current = 0L;
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        current = rs.getLong(1);
                    }
                }
                long updated = current + delta;
                try (PreparedStatement upsert = conn.prepareStatement(
                        """
                        INSERT INTO balances (account, currency, balance)
                        VALUES (?, ?, ?)
                        ON CONFLICT(account, currency) DO UPDATE SET balance=excluded.balance
                        """
                )) {
                    upsert.setString(1, account.toString());
                    upsert.setString(2, currencyId);
                    upsert.setLong(3, updated);
                    upsert.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BalanceStoreException("Failed to apply delta", e);
        }
    }

    private void initialize() throws BalanceStoreException {
        ensureParent();
        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout=5000;");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS balances (
                        account TEXT NOT NULL,
                        currency TEXT NOT NULL,
                        balance INTEGER NOT NULL,
                        PRIMARY KEY (account, currency)
                    )
                    """);
        } catch (SQLException e) {
            throw new BalanceStoreException("Failed to initialize SQLite store", e);
        }
    }

    private void ensureExists() throws BalanceStoreException {
        if (!Files.exists(path)) {
            initialize();
        }
    }

    private void ensureParent() throws BalanceStoreException {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (Exception e) {
            throw new BalanceStoreException("Failed to create storage directory", e);
        }
    }

    private Connection openConnection() throws BalanceStoreException {
        try {
            return DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            throw new BalanceStoreException("Failed to open SQLite connection", e);
        }
    }

    /**
     * Admin-only import from legacy JSON (UUID -> currency map or long).
     * Fully audited via provided audit sink; fails fast on parse/write issues.
     *
     * @return number of balance entries imported
     */
    public static int adminImportFromJson(Path jsonPath, SqliteBalanceStore target, String defaultCurrencyId, FabricAuditSink auditSink) throws BalanceStoreException {
        if (jsonPath == null || target == null || defaultCurrencyId == null || defaultCurrencyId.isBlank()) {
            throw new BalanceStoreException("Import missing required arguments");
        }
        if (!Files.exists(jsonPath)) {
            throw new BalanceStoreException("JSON file not found");
        }
        Objects.requireNonNull(auditSink, "auditSink");

        try (Reader reader = Files.newBufferedReader(jsonPath)) {
            com.google.gson.JsonElement element = new com.google.gson.Gson().fromJson(reader, com.google.gson.JsonElement.class);
            if (element == null || !element.isJsonObject()) {
                throw new BalanceStoreException("JSON is not an object");
            }
            com.google.gson.JsonObject obj = element.getAsJsonObject();
            int imported = 0;
            for (Map.Entry<String, com.google.gson.JsonElement> entry : obj.entrySet()) {
                UUID id;
                try {
                    id = UUID.fromString(entry.getKey());
                } catch (Exception ignored) {
                    continue;
                }
                Map<String, Long> currencyMap = new HashMap<>();
                com.google.gson.JsonElement valueElement = entry.getValue();
                if (valueElement.isJsonObject()) {
                    for (Map.Entry<String, com.google.gson.JsonElement> c : valueElement.getAsJsonObject().entrySet()) {
                        try {
                            currencyMap.put(c.getKey(), c.getValue().getAsLong());
                        } catch (Exception ignored) {
                        }
                    }
                } else if (valueElement.isJsonPrimitive()) {
                    try {
                        currencyMap.put(defaultCurrencyId, valueElement.getAsLong());
                    } catch (Exception ignored) {
                    }
                }
                for (Map.Entry<String, Long> c : currencyMap.entrySet()) {
                    target.applyDelta(id, c.getKey(), c.getValue());
                    imported++;
                    auditSink.emitAdmin("IMPORT_BALANCE", "imported " + c.getValue() + " to " + id + " currency=" + c.getKey());
                }
            }
            return imported;
        } catch (BalanceStoreException e) {
            throw e;
        } catch (Exception e) {
            throw new BalanceStoreException("Import failed", e);
        }
    }
}
