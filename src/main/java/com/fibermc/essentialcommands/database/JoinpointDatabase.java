package com.fibermc.essentialcommands.database;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.types.JoinpointLocation;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class JoinpointDatabase {
    private static final String DATABASE_NAME = "joinpoints.db";
    private Connection connection;
    private final File dataDirectory;

    public JoinpointDatabase(File dataDirectory) {
        this.dataDirectory = dataDirectory;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            if (!dataDirectory.exists()) {
                dataDirectory.mkdirs();
            }

            File dbFile = new File(dataDirectory, DATABASE_NAME);
            String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            connection = DriverManager.getConnection(jdbcUrl);
            connection.setAutoCommit(true);

            createTables();

            EssentialCommands.log(Level.INFO, "Joinpoint database initialized at: " + dbFile.getAbsolutePath());
        } catch (SQLException e) {
            EssentialCommands.log(Level.ERROR, "Failed to initialize joinpoint database", e);
            throw new RuntimeException("Failed to initialize joinpoint database", e);
        }
    }

    private void createTables() throws SQLException {
        String createJoinpointsTable = """
            CREATE TABLE IF NOT EXISTS joinpoints (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                owner_uuid TEXT NOT NULL,
                world_key TEXT NOT NULL,
                x REAL NOT NULL,
                y REAL NOT NULL,
                z REAL NOT NULL,
                head_yaw REAL NOT NULL DEFAULT 0.0,
                pitch REAL NOT NULL DEFAULT 0.0,
                is_global BOOLEAN NOT NULL DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(name, owner_uuid)
            )""";

        String createSharedWithTable = """
            CREATE TABLE IF NOT EXISTS joinpoint_shared_with (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                joinpoint_id INTEGER NOT NULL,
                shared_with_uuid TEXT NOT NULL,
                shared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (joinpoint_id) REFERENCES joinpoints(id) ON DELETE CASCADE,
                UNIQUE(joinpoint_id, shared_with_uuid)
            )""";

        String createPlayerCacheTable = """
            CREATE TABLE IF NOT EXISTS player_cache (
                uuid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                nickname TEXT,
                last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""";

        String createIndices = """
            CREATE INDEX IF NOT EXISTS idx_joinpoints_owner ON joinpoints(owner_uuid);
            CREATE INDEX IF NOT EXISTS idx_joinpoints_name ON joinpoints(name);
            CREATE INDEX IF NOT EXISTS idx_joinpoints_global ON joinpoints(is_global);
            CREATE INDEX IF NOT EXISTS idx_shared_with_uuid ON joinpoint_shared_with(shared_with_uuid);
            CREATE INDEX IF NOT EXISTS idx_player_cache_name ON player_cache(name);
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createJoinpointsTable);
            stmt.executeUpdate(createSharedWithTable);
            stmt.executeUpdate(createPlayerCacheTable);
            stmt.executeUpdate(createIndices);
        }
    }

    private void createJoinpoint(String name, UUID ownerUuid, JoinpointLocation location) throws SQLException {
        String sql = """
            INSERT INTO joinpoints (name, owner_uuid, world_key, x, y, z, head_yaw, pitch, is_global)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, ownerUuid.toString());
            stmt.setString(3, location.dim().getValue().toString());
            stmt.setDouble(4, location.x());
            stmt.setDouble(5, location.y());
            stmt.setDouble(6, location.z());
            stmt.setFloat(7, location.headYaw());
            stmt.setFloat(8, location.pitch());
            stmt.setBoolean(9, location.isGlobal());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long joinpointId = generatedKeys.getLong(1);
                    addSharedUsers(joinpointId, location.getSharedWith());
                }
            }
        }
    }

    private void updateJoinpoint(String name, UUID ownerUuid, JoinpointLocation location) throws SQLException {
        String updateSql = """
            UPDATE joinpoints
            SET world_key = ?, x = ?, y = ?, z = ?, head_yaw = ?, pitch = ?, is_global = ?
            WHERE name = ? AND owner_uuid = ?
            """;

        long joinpointId;
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, location.dim().getValue().toString());
            stmt.setDouble(2, location.x());
            stmt.setDouble(3, location.y());
            stmt.setDouble(4, location.z());
            stmt.setFloat(5, location.headYaw());
            stmt.setFloat(6, location.pitch());
            stmt.setBoolean(7, location.isGlobal());
            stmt.setString(8, name);
            stmt.setString(9, ownerUuid.toString());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No joinpoint found with name: " + name + " for owner: " + ownerUuid);
            }
        }

        joinpointId = getJoinpointId(name, ownerUuid);
        clearSharedUsers(joinpointId);
        addSharedUsers(joinpointId, location.getSharedWith());
    }

    private boolean deleteJoinpoint(String name, UUID ownerUuid) throws SQLException {
        String sql = "DELETE FROM joinpoints WHERE name = ? AND owner_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, ownerUuid.toString());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private JoinpointLocation getJoinpoint(String name, UUID ownerUuid) throws SQLException {
        String sql = """
            SELECT j.*, GROUP_CONCAT(s.shared_with_uuid) as shared_uuids
            FROM joinpoints j
            LEFT JOIN joinpoint_shared_with s ON j.id = s.joinpoint_id
            WHERE j.name = ? AND j.owner_uuid = ?
            GROUP BY j.id
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, ownerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createJoinpointLocationFromResultSet(rs);
                }
            }
        }

        return null;
    }

    private List<JoinpointLocation> getAccessibleJoinpoints(ServerPlayerEntity player) throws SQLException {
        return getAccessibleOwnedJoinpoints(player, player.getUuid());
    }

    private List<JoinpointLocation> getAccessibleOwnedJoinpoints(ServerPlayerEntity player, UUID ownedById) throws SQLException {
        UUID playerUuid = player.getUuid();
        String sql = """
            SELECT DISTINCT j.*, GROUP_CONCAT(s.shared_with_uuid) as shared_uuids
            FROM joinpoints j
            LEFT JOIN joinpoint_shared_with s ON j.id = s.joinpoint_id
            WHERE (
                    j.is_global = TRUE
                    OR j.owner_uuid = ?
                    OR j.id IN (SELECT joinpoint_id FROM joinpoint_shared_with WHERE shared_with_uuid = ?)
               ) AND (
                   j.owner_uuid = ?
               )
            GROUP BY j.id
            ORDER BY j.name
            """;

        List<JoinpointLocation> joinpoints = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, playerUuid.toString());
            stmt.setString(3, ownedById.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    joinpoints.add(createJoinpointLocationFromResultSet(rs));
                }
            }
        }

        return joinpoints;
    }

    private List<JoinpointLocation> getOwnedJoinpoints(UUID ownerUuid) throws SQLException {
        String sql = """
            SELECT j.*, GROUP_CONCAT(s.shared_with_uuid) as shared_uuids
            FROM joinpoints j
            LEFT JOIN joinpoint_shared_with s ON j.id = s.joinpoint_id
            WHERE j.owner_uuid = ?
            GROUP BY j.id
            ORDER BY j.name
            """;

        List<JoinpointLocation> joinpoints = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ownerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    joinpoints.add(createJoinpointLocationFromResultSet(rs));
                }
            }
        }

        return joinpoints;
    }

    private boolean joinpointExists(String name, UUID ownerUuid) throws SQLException {
        String sql = "SELECT 1 FROM joinpoints WHERE name = ? AND owner_uuid = ? LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, ownerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void updatePlayerCache(UUID uuid, String name, String nickname) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO player_cache (uuid, name, nickname, last_seen)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.setString(3, nickname);
            stmt.executeUpdate();
        }
    }

    private String getCachedPlayerName(UUID uuid) throws SQLException {
        String sql = "SELECT name FROM player_cache WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }

        return null;
    }

    private UUID getOwnerPlayerIdByName(String playerName, UUID requesterUuid) throws SQLException {
        String sql = """
            SELECT DISTINCT p.uuid
            FROM player_cache p
            INNER JOIN joinpoints joinp ON joinp.owner_uuid = p.uuid
            LEFT JOIN joinpoint_shared_with jsw ON jsw.joinpoint_id = joinp.id
            WHERE p.name = ?
                AND (
                    joinp.is_global
                    OR jsw.shared_with_uuid = ?
                )
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setString(2, requesterUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                }
            }
        }

        return null;
    }

    private String getCachedPlayerNickname(UUID uuid) throws SQLException {
        String sql = "SELECT nickname FROM player_cache WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nickname");
                }
            }
        }

        return null;
    }

    private List<JoinpointLocation> getAccessibleJoinpointsWithNames(ServerPlayerEntity player) throws SQLException {
        UUID playerUuid = player.getUuid();
        String sql = """
            SELECT DISTINCT j.*,
                   GROUP_CONCAT(s.shared_with_uuid) as shared_uuids,
                   pc_owner.name as owner_name,
                   pc_owner.nickname as owner_nickname
            FROM joinpoints j
            LEFT JOIN joinpoint_shared_with s ON j.id = s.joinpoint_id
            LEFT JOIN player_cache pc_owner ON j.owner_uuid = pc_owner.uuid
            WHERE j.is_global = TRUE
               OR j.owner_uuid = ?
               OR j.id IN (SELECT joinpoint_id FROM joinpoint_shared_with WHERE shared_with_uuid = ?)
            GROUP BY j.id
            ORDER BY j.name
            """;

        List<JoinpointLocation> joinpoints = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, playerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JoinpointLocation joinpoint = createJoinpointLocationFromResultSet(rs);

                    String ownerName = rs.getString("owner_name");
                    String ownerNickname = rs.getString("owner_nickname");

                    if (ownerName != null) {
                        joinpoint = new JoinpointLocationWithOwnerName(joinpoint, ownerName, ownerNickname);
                    }

                    joinpoints.add(joinpoint);
                }
            }
        }

        return joinpoints;
    }

    private List<String> getAccessibleJoinpointsOwnerNames(ServerPlayerEntity player) throws SQLException {
        UUID playerUuid = player.getUuid();
        String sql = """
            SELECT DISTINCT
                pc_owner.name as owner_name
            FROM joinpoints j
            LEFT JOIN joinpoint_shared_with s ON j.id = s.joinpoint_id
            LEFT JOIN player_cache pc_owner ON j.owner_uuid = pc_owner.uuid
            WHERE j.is_global = TRUE
               OR j.owner_uuid = ?
               OR j.id IN (SELECT joinpoint_id FROM joinpoint_shared_with WHERE shared_with_uuid = ?)
            GROUP BY j.id
            ORDER BY j.name
            """;

        List<String> ownerNames = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, playerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ownerNames.add(rs.getString("owner_name"));
                }
            }
        }

        return ownerNames;
    }

    private Map<UUID, String> getCachedNamesForUuids(Set<UUID> uuids) throws SQLException {
        if (uuids.isEmpty()) return new HashMap<>();

        Map<UUID, String> nameMap = new HashMap<>();
        StringBuilder sql = new StringBuilder("SELECT uuid, name FROM player_cache WHERE uuid IN (");

        for (int i = 0; i < uuids.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (UUID uuid : uuids) {
                stmt.setString(index++, uuid.toString());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    String name = rs.getString("name");
                    nameMap.put(uuid, name);
                }
            }
        }

        return nameMap;
    }

    // ASYNC VARIANTS
    public CompletableFuture<Void> createJoinpointAsync(String name, UUID ownerUuid, JoinpointLocation location) {
        return DatabaseHelper.async(() -> {
            createJoinpoint(name, ownerUuid, location);
            return null;
        });
    }

    public CompletableFuture<Void> updateJoinpointAsync(String name, UUID ownerUuid, JoinpointLocation location) {
        return DatabaseHelper.async(() -> {
            updateJoinpoint(name, ownerUuid, location);
            return null;
        });
    }

    public CompletableFuture<Boolean> deleteJoinpointAsync(String name, UUID ownerUuid) {
        return DatabaseHelper.async(() -> deleteJoinpoint(name, ownerUuid), false);
    }

    public CompletableFuture<JoinpointLocation> getJoinpointAsync(@NotNull String name, @NotNull UUID ownerUuid) {
        return DatabaseHelper.async(() -> getJoinpoint(name, ownerUuid));
    }

    public CompletableFuture<List<JoinpointLocation>> getAccessibleJoinpointsAsync(ServerPlayerEntity player) {
        return DatabaseHelper.async(() -> getAccessibleJoinpoints(player), new ArrayList<>());
    }

    public CompletableFuture<List<JoinpointLocation>> getAccessibleOwnedJoinpointsAsync(ServerPlayerEntity player, UUID ownedById) {
        return DatabaseHelper.async(() -> getAccessibleOwnedJoinpoints(player, ownedById), new ArrayList<>());
    }

    public CompletableFuture<List<JoinpointLocation>> getOwnedJoinpointsAsync(UUID ownerUuid) {
        return DatabaseHelper.async(() -> getOwnedJoinpoints(ownerUuid), new ArrayList<>());
    }

    public CompletableFuture<Boolean> joinpointExistsAsync(String name, UUID ownerUuid) {
        return DatabaseHelper.async(() -> joinpointExists(name, ownerUuid), false);
    }

    public CompletableFuture<Void> updatePlayerCacheAsync(UUID uuid, String name, String nickname) {
        return DatabaseHelper.async(() -> {
            updatePlayerCache(uuid, name, nickname);
            return null;
        });
    }

    public CompletableFuture<String> getCachedPlayerNameAsync(UUID uuid) {
        return DatabaseHelper.async(() -> getCachedPlayerName(uuid));
    }

    public CompletableFuture<UUID> getOwnerPlayerIdByNameAsync(String playerName, UUID requesterId) {
        return DatabaseHelper.async(() -> getOwnerPlayerIdByName(playerName, requesterId));
    }

    public CompletableFuture<String> getCachedPlayerNicknameAsync(UUID uuid) {
        return DatabaseHelper.async(() -> getCachedPlayerNickname(uuid));
    }

    public CompletableFuture<List<JoinpointLocation>> getAccessibleJoinpointsWithNamesAsync(ServerPlayerEntity player) {
        return DatabaseHelper.async(() -> getAccessibleJoinpointsWithNames(player), new ArrayList<>());
    }

    public CompletableFuture<List<String>> getAccessibleJoinpointsOwnerNamesAsync(ServerPlayerEntity player) {
        return DatabaseHelper.async(() -> getAccessibleJoinpointsOwnerNames(player), new ArrayList<>());
    }

    public CompletableFuture<Map<UUID, String>> getCachedNamesForUuidsAsync(Set<UUID> uuids) {
        return DatabaseHelper.async(() -> getCachedNamesForUuids(uuids), new HashMap<>());
    }

    // HELPER METHODS (existing)
    private JoinpointLocation createJoinpointLocationFromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
        String worldKeyStr = rs.getString("world_key");
        double x = rs.getDouble("x");
        double y = rs.getDouble("y");
        double z = rs.getDouble("z");
        float headYaw = rs.getFloat("head_yaw");
        float pitch = rs.getFloat("pitch");
        boolean isGlobal = rs.getBoolean("is_global");

        Set<UUID> sharedWith = new HashSet<>();
        String sharedUuidsStr = rs.getString("shared_uuids");
        if (sharedUuidsStr != null && !sharedUuidsStr.trim().isEmpty()) {
            for (String uuidStr : sharedUuidsStr.split(",")) {
                if (!uuidStr.trim().isEmpty()) {
                    try {
                        sharedWith.add(UUID.fromString(uuidStr.trim()));
                    } catch (IllegalArgumentException e) {
                        EssentialCommands.log(Level.WARN, "Invalid UUID in shared_with: " + uuidStr);
                    }
                }
            }
        }

        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldKeyStr));
        MinecraftLocation mcLocation = new MinecraftLocation(worldKey, x, y, z, headYaw, pitch);

        return new JoinpointLocation(mcLocation, name, ownerUuid, isGlobal, sharedWith);
    }

    private long getJoinpointId(String name, UUID ownerUuid) throws SQLException {
        String sql = "SELECT id FROM joinpoints WHERE name = ? AND owner_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, ownerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                } else {
                    throw new SQLException("Joinpoint not found: " + name + " for owner: " + ownerUuid);
                }
            }
        }
    }

    private void addSharedUsers(long joinpointId, Set<UUID> sharedWith) throws SQLException {
        if (sharedWith.isEmpty()) return;

        String sql = "INSERT INTO joinpoint_shared_with (joinpoint_id, shared_with_uuid) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (UUID uuid : sharedWith) {
                stmt.setLong(1, joinpointId);
                stmt.setString(2, uuid.toString());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void clearSharedUsers(long joinpointId) throws SQLException {
        String sql = "DELETE FROM joinpoint_shared_with WHERE joinpoint_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, joinpointId);
            stmt.executeUpdate();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            EssentialCommands.log(Level.ERROR, "Error closing joinpoint database connection", e);
        }
    }

    public static class JoinpointLocationWithOwnerName extends JoinpointLocation {
        private final String ownerName;
        private final String ownerNickname;

        public JoinpointLocationWithOwnerName(JoinpointLocation original, String ownerName, String ownerNickname) {
            super(original, original.getName(), original.getOwner(), original.isGlobal(), original.getSharedWith());
            this.ownerName = ownerName;
            this.ownerNickname = ownerNickname;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getOwnerNickname() {
            return ownerNickname;
        }

        public String getDisplayName() {
            return ownerNickname != null ? ownerNickname : ownerName;
        }
    }
}
