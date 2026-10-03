package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

        // 所有玩家登录时都在lobby
        World lobby = Bukkit.getWorld(plugin.getConfigManager().getLobbyWorld());
        if (lobby != null) {
            Location spawnLocation = new Location(lobby, 0.5, 70.0, 0.5, 0.0f, 90.0f);
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }

        // 发送欢迎消息
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");

        // 延迟1秒后检查排队（等待AuthMe验证完成）
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            // 检查游戏世界人数
            int currentPlayers = plugin.getQueueManager().getGamePlayers();
            int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

            if (currentPlayers >= maxPlayers) {
                // 游戏世界已满，自动加入排队
                plugin.getQueueManager().addToQueue(player);
            } else {
                // 游戏世界未满，占用名额（即使还没/play）
                plugin.getQueueManager().reserveSlot(player);
                MessageUtil.send(player, "§7输入 §6/play §7进入游戏");
            }
        }, 20L); // 1秒后执行
    }
}
