package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
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
        java.util.UUID uuid = event.getPlayer().getUniqueId();

        // 从队列中移除
        plugin.getQueueManager().removeFromQueue(uuid);

        // 清除通知标记
        plugin.getQueueManager().clearNotification(uuid);

        // 清除访客模式标记
        plugin.getQueueManager().setGuestMode(uuid, false);

        // 清除目标位置记录
        plugin.getQueueManager().removeTargetLocation(uuid);

        plugin.getLogger().info(event.getPlayer().getName() + " 退出服务器，清理所有状态");
    }
}
