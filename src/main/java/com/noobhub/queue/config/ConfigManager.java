package com.noobhub.queue.config;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final NoobHubQueue plugin;
    private final FileConfiguration config;

    public ConfigManager(NoobHubQueue plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public int getMaxGamePlayers() {
        return config.getInt("settings.max-game-players", 100);
    }

    public int getCheckInterval() {
        return config.getInt("settings.check-interval", 20);
    }

    public Location getLobbyLocation() {
        String world = config.getString("settings.lobby-location.world", "world");
        double x = config.getDouble("settings.lobby-location.x", 0.5);
        double y = config.getDouble("settings.lobby-location.y", 100.0);
        double z = config.getDouble("settings.lobby-location.z", 0.5);
        float yaw = (float) config.getDouble("settings.lobby-location.yaw", 0.0);
        float pitch = (float) config.getDouble("settings.lobby-location.pitch", 0.0);

        return new Location(plugin.getServer().getWorld(world), x, y, z, yaw, pitch);
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "§cMessage not found: " + key)
            .replace("&", "§");
    }

    public String getVIPPermission() {
        return config.getString("permissions.vip", "queue.vip");
    }
}
