package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.TeleportUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerJoinListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)  // 最后执行，让RandomSpawn等插件先处理
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 延迟1 tick，确保RandomSpawn等插件已经处理完
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            // 获取当前游戏中的玩家数
            int onlinePlayers = getGamePlayers();
            int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

            plugin.getLogger().info(player.getName() + " 加入服务器，当前游戏人数: " + onlinePlayers + "/" + maxPlayers);

            if (onlinePlayers >= maxPlayers) {
                // 需要排队
                // 记录玩家当前位置（RandomSpawn已经处理过的位置）
                Location targetLocation = player.getLocation().clone();
                plugin.getQueueManager().recordTargetLocation(player.getUniqueId(), targetLocation);

                plugin.getLogger().info("记录 " + player.getName() + " 的目标位置: " +
                    String.format("%.1f, %.1f, %.1f", targetLocation.getX(), targetLocation.getY(), targetLocation.getZ()));

                // 传送到等待平台
                TeleportUtil.teleportToLobby(player, plugin);

                // 添加到队列
                plugin.getQueueManager().addToQueue(player);

                // 设置访客模式
                setGuestMode(player);
            } else {
                // 直接进入游戏，不需要访客状态
                plugin.getLogger().info(player.getName() + " 直接进入游戏");
            }
        }, 1L);
    }

    /**
     * 设置访客模式
     */
    private void setGuestMode(Player player) {
        // 清空物品栏
        player.getInventory().clear();

        // 清空装备
        player.getInventory().setArmorContents(null);

        // 设置饱食度
        player.setFoodLevel(20);
        player.setSaturation(20);

        // 设置生命值
        player.setHealth(player.getMaxHealth());

        plugin.getLogger().info(player.getName() + " 已设置为访客模式");
    }

    /**
     * 获取当前在游戏中的玩家数（不包括排队的）
     */
    private int getGamePlayers() {
        return Bukkit.getOnlinePlayers().size() - plugin.getQueueManager().getQueueSize();
    }
}
