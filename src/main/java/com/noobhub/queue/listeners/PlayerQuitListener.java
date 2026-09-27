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

        // 1. 记录玩家位置和物品（仅在游戏区时记录）
        Location currentLocation = player.getLocation();
        Location lobbyLocation = plugin.getConfigManager().getLobbyLocation();

        // 判断是否在主城（20格范围内）
        boolean isInLobby = currentLocation.getWorld().equals(lobbyLocation.getWorld())
            && Math.abs(currentLocation.getX() - lobbyLocation.getX()) < 20
            && Math.abs(currentLocation.getY() - lobbyLocation.getY()) < 20
            && Math.abs(currentLocation.getZ() - lobbyLocation.getZ()) < 20;

        if (!isInLobby) {
            // 在游戏区退出，记录位置和物品
            plugin.getQueueManager().recordTargetLocation(uuid, currentLocation);
            plugin.getQueueManager().saveInventory(player);
            plugin.getLogger().info("记录 " + player.getName() + " 的退出位置和物品: " +
                String.format("%.1f, %.1f, %.1f", currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()));
        } else {
            // 在主城退出，不更新记录（保留上次游戏区的数据）
            plugin.getLogger().info(player.getName() + " 在主城退出，保留游戏区数据");
        }

        // 2. 清理临时状态
        // 从队列中移除
        plugin.getQueueManager().removeFromQueue(uuid);

        // 清除通知标记
        plugin.getQueueManager().clearNotification(uuid);

        // 清除访客模式标记
        plugin.getQueueManager().setGuestMode(uuid, false);

        // 注意：不清除 targetLocation 和 savedInventories（已在上面处理）

        plugin.getLogger().info(event.getPlayer().getName() + " 退出服务器");
    }
}
