package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerRespawnListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerRespawnListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 如果有床或重生锚，使用原版机制
        if (event.isBedSpawn() || event.isAnchorSpawn()) {
            Location respawnLocation = event.getRespawnLocation();
            String locInfo = String.format("%.1f, %.1f, %.1f",
                respawnLocation.getX(), respawnLocation.getY(), respawnLocation.getZ());

            if (event.isBedSpawn()) {
                plugin.getLogger().info(player.getName() + " 在床重生: " + locInfo);
            } else {
                plugin.getLogger().info(player.getName() + " 在重生锚重生: " + locInfo);
            }
            return; // 使用原版机制
        }

        // 没有床/锚，从PDC读取出生点
        Location spawnPoint = getSpawnPointFromPDC(player);

        if (spawnPoint != null) {
            // 手动设置重生位置到出生点
            event.setRespawnLocation(spawnPoint);
            plugin.getLogger().info(player.getName() + " 在个人出生点重生: " +
                String.format("%.1f, %.1f, %.1f",
                    spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ()));
        } else {
            // 没有出生点记录，使用世界spawn（首次进入但还未/play的玩家）
            Location respawnLocation = event.getRespawnLocation();
            plugin.getLogger().info(player.getName() + " 无出生点记录，在世界spawn重生: " +
                String.format("%.1f, %.1f, %.1f",
                    respawnLocation.getX(), respawnLocation.getY(), respawnLocation.getZ()));
        }
    }

    /**
     * 从PDC读取出生点
     */
    private Location getSpawnPointFromPDC(Player player) {
        NamespacedKey keyX = new NamespacedKey(plugin, "spawn_point_x");
        NamespacedKey keyY = new NamespacedKey(plugin, "spawn_point_y");
        NamespacedKey keyZ = new NamespacedKey(plugin, "spawn_point_z");
        NamespacedKey keyWorld = new NamespacedKey(plugin, "spawn_point_world");

        Double x = player.getPersistentDataContainer().get(keyX, PersistentDataType.DOUBLE);
        Double y = player.getPersistentDataContainer().get(keyY, PersistentDataType.DOUBLE);
        Double z = player.getPersistentDataContainer().get(keyZ, PersistentDataType.DOUBLE);
        String worldName = player.getPersistentDataContainer().get(keyWorld, PersistentDataType.STRING);

        if (x != null && y != null && z != null && worldName != null) {
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world != null) {
                return new Location(world, x, y, z);
            }
        }

        return null;
    }
}
