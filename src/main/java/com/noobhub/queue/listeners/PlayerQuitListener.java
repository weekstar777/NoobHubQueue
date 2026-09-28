package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerQuitListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        java.util.UUID uuid = player.getUniqueId();

        // 使用双世界方案后，只需记录玩家在游戏世界的退出位置
        // 物品栏由 Minecraft 自动保存到各世界的 playerdata

        Location currentLocation = player.getLocation();
        String worldName = currentLocation.getWorld().getName();

        if (worldName.equals("world")) {
            // 在游戏世界退出，记录位置（用于下次 /play 传送）
            plugin.getQueueManager().recordTargetLocation(uuid, currentLocation);
            plugin.getLogger().info("记录 " + player.getName() + " 的退出位置: " +
                String.format("%.1f, %.1f, %.1f", currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()));
        } else if (worldName.equals("world_lobby")) {
            // 在主城世界退出，不记录（保留上次游戏区的位置）
            plugin.getLogger().info(player.getName() + " 在主城退出，保留游戏区位置记录");
        }

        // Minecraft 自动保存 playerdata（物品栏、血量等）
        // 插件不需要手动保存物品栏

        // 清理临时状态
        plugin.getQueueManager().removeFromQueue(uuid);
        plugin.getQueueManager().clearNotification(uuid);
        plugin.getQueueManager().setGuestMode(uuid, false);

        plugin.getLogger().info(event.getPlayer().getName() + " 退出服务器");
    }
}
