package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class QueueSecurityListener implements Listener {

    private final NoobHubQueue plugin;
    private final AuthMeApi authMeApi;

    public QueueSecurityListener(NoobHubQueue plugin) {
        this.plugin = plugin;
        this.authMeApi = AuthMeApi.getInstance();
    }

    /**
     * 拦截未登录玩家尝试传送到游戏世界
     * 实时防护，防止未登录玩家通过传送命令/插件进入游戏世界
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        // 检查玩家是否已通过AuthMe登录
        if (authMeApi.isAuthenticated(player)) {
            return; // 已登录，放行
        }

        // 检查目标世界是否是游戏世界
        String worldName = to.getWorld().getName();
        if (worldName.equals(plugin.getConfigManager().getGameWorld()) ||
            worldName.equals(plugin.getConfigManager().getNetherWorld()) ||
            worldName.equals(plugin.getConfigManager().getEndWorld())) {

            // 取消传送并踢出服务器
            event.setCancelled(true);
            player.kickPlayer("§c未登录玩家不能进入游戏世界！\n\n§7请先完成登录验证\n§8检测到异常传送行为");

            plugin.getLogger().warning("[安全] " + player.getName() + " 未登录时尝试传送到游戏世界，已踢出");
        }
    }
}
