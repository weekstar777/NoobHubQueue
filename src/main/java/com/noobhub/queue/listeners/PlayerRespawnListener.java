package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
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
            // 调整到最高固体顶面（防止地形变化导致从天上掉下来）
            Location safeSpawnPoint = findHighestSolidSurface(spawnPoint);

            // 有出生点记录，重生在个人出生点
            event.setRespawnLocation(safeSpawnPoint);
            plugin.getLogger().info(player.getName() + " 在个人出生点重生: " +
                String.format("%.1f, %.1f, %.1f",
                    safeSpawnPoint.getX(), safeSpawnPoint.getY(), safeSpawnPoint.getZ()));
        } else {
            // 没有出生点记录（没有/play过），强制重生在lobby
            org.bukkit.World lobby = Bukkit.getWorld(plugin.getConfigManager().getLobbyWorld());
            if (lobby != null) {
                Location lobbySpawn = new Location(lobby, 0.5, 70.0, 0.5);
                event.setRespawnLocation(lobbySpawn);
                plugin.getLogger().info(player.getName() + " 无出生点记录，重生在lobby: 0.5, 70.0, 0.5");
            } else {
                plugin.getLogger().warning(player.getName() + " lobby世界不存在，使用原版重生机制");
            }
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

    /**
     * 找到指定位置最高的固体方块顶面
     * 从世界顶部向下扫描，找到第一个固体方块，并确保上方有足够空间
     */
    private Location findHighestSolidSurface(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        // 从世界最大高度向下扫描
        int maxHeight = world.getMaxHeight() - 1;
        int minHeight = world.getMinHeight();

        for (int y = maxHeight; y >= minHeight; y--) {
            Block block = world.getBlockAt(x, y, z);

            // 找到固体方块
            if (block.getType().isSolid()) {
                // 检查上方两格是否为空气（确保玩家有足够空间）
                Block above1 = world.getBlockAt(x, y + 1, z);
                Block above2 = world.getBlockAt(x, y + 2, z);

                if (above1.getType().isAir() && above2.getType().isAir()) {
                    // 返回固体方块上方的位置（y+1），保留原有的yaw和pitch
                    Location result = new Location(world, x + 0.5, y + 1, z + 0.5,
                        location.getYaw(), location.getPitch());

                    if (Math.abs(result.getY() - location.getY()) > 0.1) {
                        plugin.getLogger().info("[重生位置调整] 原Y=" + String.format("%.1f", location.getY()) +
                            " → 最高固体顶面Y=" + (y + 1));
                    }

                    return result;
                }
            }
        }

        // 如果没找到合适位置（极端情况），返回原位置
        plugin.getLogger().warning("[重生位置调整] 未找到合适的固体顶面，使用原位置");
        return location;
    }
}
