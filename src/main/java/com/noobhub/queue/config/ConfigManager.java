package com.noobhub.queue.config;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final FileConfiguration config;

    public ConfigManager(NoobHubQueue plugin) {
        this.config = plugin.getConfig();
    }

    public int getMaxGamePlayers() {
        return config.getInt("settings.max-game-players", 100);
    }

    public int getCheckInterval() {
        return config.getInt("settings.check-interval", 20);
    }

    public String getLobbyWorld() {
        return config.getString("settings.worlds.lobby", "world_lobby");
    }

    public String getGameWorld() {
        return config.getString("settings.worlds.game", "world");
    }

    public String getNetherWorld() {
        return getGameWorld() + "_nether";
    }

    public String getEndWorld() {
        return getGameWorld() + "_the_end";
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "§cMessage not found: " + key)
            .replace("&", "§");
    }

    public String getVIPPermission() {
        return config.getString("permissions.vip", "queue.vip");
    }
}
