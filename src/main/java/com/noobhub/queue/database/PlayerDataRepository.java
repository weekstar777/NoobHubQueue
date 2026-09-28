package com.noobhub.queue.database;

import org.bukkit.Location;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDataRepository {

    private final DatabaseManager databaseManager;

    public PlayerDataRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void saveSpawnPoint(UUID uuid, Location location) {
        String sql = "INSERT INTO player_data (uuid, spawn_x, spawn_y, spawn_z, spawn_world, first_time) " +
                "VALUES (?, ?, ?, ?, ?, FALSE) " +
                "ON DUPLICATE KEY UPDATE spawn_x=?, spawn_y=?, spawn_z=?, spawn_world=?, first_time=FALSE";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setDouble(2, location.getX());
            stmt.setDouble(3, location.getY());
            stmt.setDouble(4, location.getZ());
            stmt.setString(5, location.getWorld().getName());
            stmt.setDouble(6, location.getX());
            stmt.setDouble(7, location.getY());
            stmt.setDouble(8, location.getZ());
            stmt.setString(9, location.getWorld().getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getSpawnPoint(UUID uuid) {
        String sql = "SELECT spawn_x, spawn_y, spawn_z, spawn_world FROM player_data WHERE uuid=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String worldName = rs.getString("spawn_world");
                double x = rs.getDouble("spawn_x");
                double y = rs.getDouble("spawn_y");
                double z = rs.getDouble("spawn_z");

                if (worldName != null && Bukkit.getWorld(worldName) != null) {
                    return new Location(Bukkit.getWorld(worldName), x, y, z);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveLastLocation(UUID uuid, Location location) {
        String sql = "INSERT INTO player_data (uuid, last_x, last_y, last_z, last_world) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE last_x=?, last_y=?, last_z=?, last_world=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setDouble(2, location.getX());
            stmt.setDouble(3, location.getY());
            stmt.setDouble(4, location.getZ());
            stmt.setString(5, location.getWorld().getName());
            stmt.setDouble(6, location.getX());
            stmt.setDouble(7, location.getY());
            stmt.setDouble(8, location.getZ());
            stmt.setString(9, location.getWorld().getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getLastLocation(UUID uuid) {
        String sql = "SELECT last_x, last_y, last_z, last_world FROM player_data WHERE uuid=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String worldName = rs.getString("last_world");
                double x = rs.getDouble("last_x");
                double y = rs.getDouble("last_y");
                double z = rs.getDouble("last_z");

                if (worldName != null && Bukkit.getWorld(worldName) != null) {
                    return new Location(Bukkit.getWorld(worldName), x, y, z);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isFirstTime(UUID uuid) {
        String sql = "SELECT first_time FROM player_data WHERE uuid=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("first_time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}
