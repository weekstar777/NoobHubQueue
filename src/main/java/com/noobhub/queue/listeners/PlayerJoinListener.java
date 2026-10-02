package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerJoinListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerJoinListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String loginWorld = player.getWorld().getName();

        // 如果玩家登录时在游戏世界（Minecraft从world/playerdata恢复的）
        // 立即保存这个位置到PDC，然后再传送到lobby
        if ("world".equals(loginWorld) ||
            "world_nether".equals(loginWorld) ||
            "world_the_end".equals(loginWorld)) {

            Location worldLocation = player.getLocation();
            saveLastLocationToPDC(player, worldLocation);

            plugin.getLogger().info("[位置保存] " + player.getName() +
                " 从游戏世界登录，已保存位置: " +
                String.format("%.1f, %.1f, %.1f (%s)",
                    worldLocation.getX(), worldLocation.getY(), worldLocation.getZ(), loginWorld));
        }

        // 强制传送到lobby
        World lobby = Bukkit.getWorld("world_lobby");
        if (lobby != null) {
            Location spawnLocation = new Location(lobby, 0.5, 70.0, 0.5, 0.0f, 90.0f);
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }

        // 发送欢迎消息
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");
        MessageUtil.send(player, "§7输入 §6/play §7进入游戏");
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
