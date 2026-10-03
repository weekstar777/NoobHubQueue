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
        String worldName = player.getWorld().getName();

        // 如果玩家在游戏世界且死亡，强制重生（处理死亡退出的情况）
        if ((worldName.equals(plugin.getConfigManager().getGameWorld()) ||
             worldName.equals(plugin.getConfigManager().getNetherWorld()) ||
             worldName.equals(plugin.getConfigManager().getEndWorld())) && player.isDead()) {
            player.spigot().respawn();
            plugin.getLogger().info("[自动重生] " + player.getName() + " 在游戏世界以死亡状态登录，已强制重生");
        }

        // 所有玩家登录时都在lobby
        World lobby = Bukkit.getWorld(plugin.getConfigManager().getLobbyWorld());
        if (lobby != null) {
            Location spawnLocation = new Location(lobby, 0.5, 70.0, 0.5, 0.0f, 90.0f);
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }

        // 发送欢迎消息
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");

        // 排队逻辑移至AuthMeLoginListener，只在AuthMe登录成功后触发
    }
}
