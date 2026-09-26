package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import com.noobhub.queue.utils.TeleportUtil;
import org.bukkit.Bukkit;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 延迟1 tick，让RandomSpawn等插件先处理
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            // 所有玩家都先传送到等待大厅

            // 1. 记录玩家当前位置（RandomSpawn处理后的位置/老玩家的退出位置）
            Location targetLocation = player.getLocation().clone();
            plugin.getQueueManager().recordTargetLocation(player.getUniqueId(), targetLocation);

            plugin.getLogger().info("记录 " + player.getName() + " 的目标位置: " +
                String.format("%.1f, %.1f, %.1f", targetLocation.getX(), targetLocation.getY(), targetLocation.getZ()));

            // 2. 传送到等待大厅
            TeleportUtil.teleportToLobby(player, plugin);

            // 3. 设置访客模式
            setGuestMode(player);

            // 标记为访客模式
            plugin.getQueueManager().setGuestMode(player.getUniqueId(), true);

            // 4. 检查游戏区域人数，决定是否需要排队
            int gamePlayersCount = getGamePlayers();
            int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

            plugin.getLogger().info(player.getName() + " 加入服务器，当前游戏人数: " + gamePlayersCount + "/" + maxPlayers);

            if (gamePlayersCount < maxPlayers) {
                // 还有空位，可以随时 /play
                MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");
                MessageUtil.send(player, "§7游戏区域还有空位 §8| §e输入 §6/play §e即可进入游戏");
                plugin.getLogger().info(player.getName() + " 可以随时进入游戏（有空位）");
            } else {
                // 已满，加入排队系统
                plugin.getQueueManager().addToQueue(player);
                plugin.getLogger().info(player.getName() + " 加入排队系统（游戏区域已满）");
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
     * 获取当前在游戏区域的玩家数（不包括等待大厅的）
     */
    private int getGamePlayers() {
        // 总在线人数 - 排队人数 - 在等待大厅但未排队的人数
        int totalOnline = Bukkit.getOnlinePlayers().size();
        int queueSize = plugin.getQueueManager().getQueueSize();
        int guestNotInQueue = 0;

        // 统计在等待大厅但未排队的玩家数
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getQueueManager().isGuestMode(p.getUniqueId())
                && !plugin.getQueueManager().isInQueue(p.getUniqueId())) {
                guestNotInQueue++;
            }
        }

        return totalOnline - queueSize - guestNotInQueue;
    }
}

