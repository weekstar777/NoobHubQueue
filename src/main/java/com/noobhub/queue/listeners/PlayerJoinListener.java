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

        // 强制传送到主城spawn点（所有玩家登录都传送到固定位置）
        World lobby = Bukkit.getWorld("world_lobby");
        if (lobby != null) {
            Location spawnLocation = lobby.getSpawnLocation();
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.ADVENTURE);
            plugin.getLogger().info(player.getName() + " 传送到主城spawn");
        }

        plugin.getLogger().info(player.getName() + " 进入服务器");

        // 发送欢迎消息和操作提示
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");
        MessageUtil.send(player, "§7输入 §6/play §7进入游戏");
    }
}
