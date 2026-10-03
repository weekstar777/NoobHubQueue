package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerQuitListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String worldName = player.getWorld().getName();

        // 检查玩家是否处于死亡状态
        if (player.isDead()) {
            // 保存死亡位置（在重生前）
            Location deathLocation = player.getLocation();

            // 自动重生玩家
            player.spigot().respawn();

            // 保存死亡位置到PDC（作为下次/play的位置）
            saveLastLocationToPDC(player, deathLocation);

            plugin.getLogger().info("[自动重生] " + player.getName() +
                " 死亡状态退出，已自动重生并保存位置: " +
                String.format("%.1f, %.1f, %.1f (%s)",
                    deathLocation.getX(), deathLocation.getY(), deathLocation.getZ(), worldName));
        } else {
            // 正常退出，如果在游戏世界则保存位置
            if (worldName.equals(plugin.getConfigManager().getGameWorld()) ||
                worldName.equals(plugin.getConfigManager().getNetherWorld()) ||
                worldName.equals(plugin.getConfigManager().getEndWorld())) {

                Location location = player.getLocation();
                saveLastLocationToPDC(player, location);

                plugin.getLogger().info("[位置保存] " + player.getName() +
                    " 在游戏世界退出，已保存位置: " +
                    String.format("%.1f, %.1f, %.1f (%s)",
                        location.getX(), location.getY(), location.getZ(), worldName));
            }
        }

        // 清理队列数据和占用的名额
        plugin.getQueueManager().removeFromQueue(uuid);
        plugin.getQueueManager().clearNotification(uuid);
    }

    /**
     * 保存位置到PersistentDataContainer（存储在playerdata中）
     */
    private void saveLastLocationToPDC(Player player, Location location) {
        NamespacedKey keyX = new NamespacedKey(plugin, "last_world_x");
        NamespacedKey keyY = new NamespacedKey(plugin, "last_world_y");
        NamespacedKey keyZ = new NamespacedKey(plugin, "last_world_z");
        NamespacedKey keyWorld = new NamespacedKey(plugin, "last_world_name");
        NamespacedKey keyYaw = new NamespacedKey(plugin, "last_world_yaw");
        NamespacedKey keyPitch = new NamespacedKey(plugin, "last_world_pitch");

        player.getPersistentDataContainer().set(keyX, PersistentDataType.DOUBLE, location.getX());
        player.getPersistentDataContainer().set(keyY, PersistentDataType.DOUBLE, location.getY());
        player.getPersistentDataContainer().set(keyZ, PersistentDataType.DOUBLE, location.getZ());
        player.getPersistentDataContainer().set(keyWorld, PersistentDataType.STRING, location.getWorld().getName());
        player.getPersistentDataContainer().set(keyYaw, PersistentDataType.FLOAT, location.getYaw());
        player.getPersistentDataContainer().set(keyPitch, PersistentDataType.FLOAT, location.getPitch());
    }
}
