package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Random;
import java.util.Set;

public class PlayerRespawnListener implements Listener {

    private final NoobHubQueue plugin;
    private final Random random = new Random();

    // 生物群系黑名单（与PlayCommand保持一致）
    private static final Set<Biome> BLACKLISTED_BIOMES = Set.of(
        Biome.OCEAN,
        Biome.DEEP_OCEAN,
        Biome.COLD_OCEAN,
        Biome.WARM_OCEAN,
        Biome.LUKEWARM_OCEAN,
        Biome.FROZEN_OCEAN,
        Biome.DEEP_COLD_OCEAN,
        Biome.DEEP_LUKEWARM_OCEAN,
        Biome.DEEP_FROZEN_OCEAN,
        Biome.RIVER,
        Biome.FROZEN_RIVER,
        Biome.THE_VOID
    );

    public PlayerRespawnListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 如果玩家有床/重生锚，使用那个位置
        if (event.isBedSpawn() || event.isAnchorSpawn()) {
            plugin.getLogger().info(player.getName() + " 在床/重生锚重生");
            return;
        }

        // 没有床/重生锚，使用玩家的出生点（第一次随机传送的位置）
        Location spawnPoint = plugin.getQueueManager().getPlayerSpawnPoint(player.getUniqueId());

        if (spawnPoint != null) {
            // 有保存的出生点，使用它
            event.setRespawnLocation(spawnPoint);
            plugin.getLogger().info(player.getName() + " 在出生点重生: " +
                String.format("%.1f, %.1f, %.1f",
                    spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ()));
        } else {
            // 没有出生点（理论上不应该发生），生成新的随机位置
            plugin.getLogger().warning(player.getName() + " 没有出生点记录，生成新的随机位置");

            World world = player.getWorld();
            Location randomLocation = findSafeRandomLocation(world);

            event.setRespawnLocation(randomLocation);

            // 保存为出生点
            plugin.getQueueManager().setPlayerSpawnPoint(player.getUniqueId(), randomLocation);

            plugin.getLogger().info(player.getName() + " 随机重生到: " +
                String.format("%.1f, %.1f, %.1f",
                    randomLocation.getX(), randomLocation.getY(), randomLocation.getZ()));
        }
    }

    /**
     * 寻找安全的随机位置（与PlayCommand相同的算法）
     */
    private Location findSafeRandomLocation(World world) {
        int maxTries = 50;
        int minRadius = 500;
        int maxRadius = 2000;

        for (int i = 0; i < maxTries; i++) {
            // 圆形区域内随机
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);

            int x = (int) (radius * Math.cos(angle));
            int z = (int) (radius * Math.sin(angle));

            // 检查生物群系
            Biome biome = world.getBiome(x, 64, z);
            if (BLACKLISTED_BIOMES.contains(biome)) {
                continue;
            }

            // 获取地表高度
            int y = world.getHighestBlockYAt(x, z);

            // 检查高度范围
            if (y < 60 || y > 200) {
                continue;
            }

            Location location = new Location(world, x + 0.5, y, z + 0.5);

            // 安全检查
            if (isLocationSafe(location)) {
                return location;
            }
        }

        // 失败后返回世界出生点
        plugin.getLogger().warning("无法找到安全的随机重生位置，使用世界出生点");
        return world.getSpawnLocation();
    }

    /**
     * 完整的位置安全检查
     */
    private boolean isLocationSafe(Location location) {
        // 检查脚下
        Material ground = location.getBlock().getType();
        if (isDangerousBlock(ground) || !ground.isSolid()) {
            return false;
        }

        // 检查头部空间
        Location head1 = location.clone().add(0, 1, 0);
        Location head2 = location.clone().add(0, 2, 0);

        Material headBlock1 = head1.getBlock().getType();
        Material headBlock2 = head2.getBlock().getType();

        if (headBlock1.isSolid() || headBlock2.isSolid()) {
            return false;
        }

        // 检查周围危险
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Location check = location.clone().add(dx, 0, dz);
                Material blockType = check.getBlock().getType();

                if (isDangerousBlock(blockType)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 判断方块是否危险
     */
    private boolean isDangerousBlock(Material material) {
        return material == Material.LAVA
            || material == Material.FIRE
            || material == Material.SOUL_FIRE
            || material == Material.CACTUS
            || material == Material.MAGMA_BLOCK
            || material == Material.CAMPFIRE
            || material == Material.SOUL_CAMPFIRE
            || material == Material.WITHER_ROSE
            || material == Material.SWEET_BERRY_BUSH
            || material == Material.POWDER_SNOW;
    }
}
