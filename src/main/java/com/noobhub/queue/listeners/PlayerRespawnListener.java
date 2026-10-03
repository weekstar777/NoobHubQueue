package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
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
        Location respawnLocation = event.getRespawnLocation();

        // 完全遵循Minecraft原版重生机制，不做任何修改
        // 记录详细的重生信息用于调试

        String locInfo = String.format("%.1f, %.1f, %.1f (%s)",
            respawnLocation.getX(),
            respawnLocation.getY(),
            respawnLocation.getZ(),
            respawnLocation.getWorld().getName());

        if (event.isBedSpawn()) {
            plugin.getLogger().info(player.getName() + " 在床重生: " + locInfo);
        } else if (event.isAnchorSpawn()) {
            plugin.getLogger().info(player.getName() + " 在重生锚重生: " + locInfo);
        } else {
            // 可能是setBedSpawnLocation设置的出生点，也可能是世界spawn
            Location bedSpawn = player.getBedSpawnLocation();
            if (bedSpawn != null) {
                plugin.getLogger().info(player.getName() + " 在个人出生点重生: " + locInfo);
            } else {
                plugin.getLogger().info(player.getName() + " 在世界spawn重生: " + locInfo);
            }
        }

        // 不修改event.setRespawnLocation()，让Minecraft自己处理
    }
}
