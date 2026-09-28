package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerQuitListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 记录玩家在游戏世界的退出位置（MySQL）
        Location currentLocation = player.getLocation();
        String worldName = currentLocation.getWorld().getName();

        if (worldName.equals("world")) {
            // 在游戏世界退出，保存位置
            plugin.getPlayerDataRepository().saveLastLocation(uuid, currentLocation);
            plugin.getLogger().info("记录 " + player.getName() + " 的退出位置: " +
                String.format("%.1f, %.1f, %.1f", currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()));
        }

        // 清理临时状态
        plugin.getQueueManager().removeFromQueue(uuid);
        plugin.getQueueManager().clearNotification(uuid);
        plugin.getQueueManager().setGuestMode(uuid, false);

        plugin.getLogger().info(player.getName() + " 退出服务器");
    }
}
