package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import com.noobhub.queue.utils.TeleportUtil;
import org.bukkit.Bukkit;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 所有玩家统一处理：立即传送到主城
        // 位置和物品的记录在退出时已完成，不需要在进服时处理

        plugin.getLogger().info(player.getName() + " 进入服务器，立即传送到主城");

        // 1. 立即传送到主城
        TeleportUtil.teleportToLobby(player, plugin);

        // 2. 设置访客模式
        setGuestMode(player);

        // 3. 标记为访客模式
        plugin.getQueueManager().setGuestMode(player.getUniqueId(), true);

        // 4. 检查名额分配
        handleQueueAssignment(player);
    }

    /**
     * 处理排队分配逻辑
     */
    private void handleQueueAssignment(Player player) {
        // 检查已占用名额，决定是否需要排队
        // 已占用名额 = 当前在线人数（包括自己）- 排队人数
        int occupiedSlots = Bukkit.getOnlinePlayers().size() - plugin.getQueueManager().getQueueSize();
        int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

        plugin.getLogger().info(player.getName() + " 加入服务器，当前占用名额: " + occupiedSlots + "/" + maxPlayers);

        if (occupiedSlots <= maxPlayers) {
            // 有空位，玩家占用一个名额，可以随时 /play 传送
            MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");
            MessageUtil.send(player, "§7输入 §6/play §7即可进入游戏");
            plugin.getLogger().info(player.getName() + " 占用名额 " + occupiedSlots + "/" + maxPlayers + "，可随时传送");
        } else {
            // 已满100人，加入排队系统等待空位
            plugin.getQueueManager().addToQueue(player);
            plugin.getLogger().info(player.getName() + " 加入排队系统（名额已满）");
        }
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
}
