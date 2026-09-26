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
        // 从队列中移除
        plugin.getQueueManager().removeFromQueue(event.getPlayer().getUniqueId());
        plugin.getLogger().info(event.getPlayer().getName() + " 退出服务器并从队列移除");
    }
}
