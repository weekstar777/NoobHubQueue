package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public class PlayerRespawnListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerRespawnListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Location spawnPoint = plugin.getPlayerDataRepository().getSpawnPoint(uuid);

        if (spawnPoint != null && !event.isBedSpawn()) {
            event.setRespawnLocation(spawnPoint);
            plugin.getLogger().info(player.getName() + " 在固定出生点重生: " +
                String.format("%.1f, %.1f, %.1f", spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ()));
        } else if (event.isBedSpawn()) {
            plugin.getLogger().info(player.getName() + " 在床重生");
        } else {
            // 没有固定出生点，重生到lobby
            org.bukkit.World lobby = org.bukkit.Bukkit.getWorld("world_lobby");
            if (lobby != null) {
                Location lobbySpawn = lobby.getSpawnLocation();
                event.setRespawnLocation(lobbySpawn);
                plugin.getLogger().info(player.getName() + " 没有固定出生点，重生到lobby");
            } else {
                plugin.getLogger().warning(player.getName() + " lobby世界不存在，使用默认重生点");
            }
        }
    }
}
