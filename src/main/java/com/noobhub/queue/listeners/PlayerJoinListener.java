package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 强制传送到主城（确保所有玩家登录都在主城）
        if (!player.getWorld().getName().equals("world_lobby")) {
            World lobby = Bukkit.getWorld("world_lobby");
            if (lobby != null) {
                Location spawnLocation = lobby.getSpawnLocation();
                player.teleport(spawnLocation);
                plugin.getLogger().info(player.getName() + " 从 " + player.getWorld().getName() + " 传送到主城");
            }
        }

        plugin.getLogger().info(player.getName() + " 进入服务器");

        // 发送欢迎消息
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");

        // 检查名额分配
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
            MessageUtil.send(player, "§7输入 §6/play §7即可进入游戏");
            plugin.getLogger().info(player.getName() + " 占用名额 " + occupiedSlots + "/" + maxPlayers + "，可随时传送");
        } else {
            // 已满100人，加入排队系统等待空位
            plugin.getQueueManager().addToQueue(player);
            plugin.getLogger().info(player.getName() + " 加入排队系统（名额已满）");
        }
    }
}
