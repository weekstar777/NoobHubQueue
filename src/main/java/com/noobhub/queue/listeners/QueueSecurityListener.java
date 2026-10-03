package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class QueueSecurityListener implements Listener {

    private final NoobHubQueue plugin;

    public QueueSecurityListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    /**
     * 拦截排队玩家尝试传送到游戏世界
     * 实时防护，防止通过传送命令/插件绕过排队
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        UUID uuid = player.getUniqueId();

        // 检查玩家是否在排队中
        if (!plugin.getQueueManager().isInQueue(uuid)) {
            return; // 不在排队，放行
        }

        // 检查目标世界是否是游戏世界
        String worldName = to.getWorld().getName();
        if (worldName.equals(plugin.getConfigManager().getGameWorld()) ||
            worldName.equals(plugin.getConfigManager().getNetherWorld()) ||
            worldName.equals(plugin.getConfigManager().getEndWorld())) {

            // 取消传送并踢出服务器
            event.setCancelled(true);
            player.kickPlayer("§c你还在排队中！\n\n§7请等待轮到后输入 §e/play §7进入游戏\n§8不要尝试使用其他方式进入游戏世界");

            plugin.getLogger().warning("[安全] " + player.getName() + " 在排队时尝试传送到游戏世界，已踢出");
        }
    }
}
