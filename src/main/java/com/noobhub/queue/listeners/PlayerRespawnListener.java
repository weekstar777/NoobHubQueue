package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerRespawnListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 完全遵循Minecraft原版重生机制，不做任何修改
        // 原版逻辑：
        // 1. 如果有床/重生锚 → 在床/锚重生
        // 2. 如果没有 → 在世界spawn点或玩家个人spawn点重生

        if (event.isBedSpawn()) {
            plugin.getLogger().info(player.getName() + " 在床/重生锚重生（原版机制）");
        } else if (event.isAnchorSpawn()) {
            plugin.getLogger().info(player.getName() + " 在重生锚重生（原版机制）");
        } else {
            plugin.getLogger().info(player.getName() + " 在spawn点重生（原版机制）");
        }

        // 不修改event.setRespawnLocation()，让Minecraft自己处理
    }
}
