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

        // 所有玩家登录时都在lobby（level-name=world_lobby + Multiverse first-spawn）
        // 直接传送到lobby固定坐标即可
        World lobby = Bukkit.getWorld(plugin.getConfigManager().getLobbyWorld());
        if (lobby != null) {
            Location spawnLocation = new Location(lobby, 0.5, 70.0, 0.5, 0.0f, 90.0f);
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }

        // 发送欢迎消息
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");
        MessageUtil.send(player, "§7输入 §6/play §7进入游戏");
    }
}
