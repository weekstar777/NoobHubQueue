package com.noobhub.queue.commands;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class PlayCommand implements CommandExecutor {

    private final NoobHubQueue plugin;
    private final Random random = new Random();

    // 生物群系黑名单（借鉴FastRTP）
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

    public PlayCommand(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此指令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // 检查玩家是否已经在游戏世界
        World currentWorld = player.getWorld();
        if (currentWorld.getName().equals(plugin.getConfigManager().getGameWorld()) ||
            currentWorld.getName().equals(plugin.getConfigManager().getNetherWorld()) ||
            currentWorld.getName().equals(plugin.getConfigManager().getEndWorld())) {
            MessageUtil.send(player, "§c你已经在游戏世界了！");
            return true;
        }

        // 检查是否在队列中
        if (plugin.getQueueManager().isInQueue(uuid)) {
            // 还在队列中，说明还没轮到
            boolean isVip = plugin.getQueueManager().isInVipQueue(uuid);
            String channel = isVip ? "§6[VIP通道]" : "§7[普通通道]";
            int position = plugin.getQueueManager().getPosition(uuid);

            MessageUtil.send(player, channel + " §c还没轮到你！");
            MessageUtil.send(player, "  §8▸ §7你的位置: §e第 " + position + " 位");

            if (!isVip) {
                int vipAhead = plugin.getQueueManager().getVipQueueSize();
                if (vipAhead > 0) {
                    MessageUtil.send(player, "  §8▸ §7VIP通道前方: §e" + vipAhead + " §7人");
                }
            }
            return true;
        }

        // 检查是否已被通知（轮到了，等待传送）
        if (plugin.getQueueManager().isNotified(uuid)) {
            // 已经轮到了，直接传送，无需再检查人数
            enterGame(player);
            return true;
        }

        // 检查游戏世界是否已满
        int currentPlayers = plugin.getQueueManager().getGamePlayers();
        int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

        if (currentPlayers >= maxPlayers) {
            // 游戏世界已满，加入排队
            plugin.getQueueManager().addToQueue(player);
            return true;
        }

        // 游戏世界未满，直接传送
        enterGame(player);

        return true;
    }

    /**
     * 处理玩家进入游戏
     */
    private void enterGame(Player player) {
        UUID uuid = player.getUniqueId();

        // 清除通知标记（如果有）
        plugin.getQueueManager().clearNotification(uuid);

        MessageUtil.send(player, "§e正在传送中~");

        // 获取游戏世界
        World gameWorld = Bukkit.getWorld(plugin.getConfigManager().getGameWorld());
        if (gameWorld == null) {
            plugin.getLogger().severe("游戏世界 " + plugin.getConfigManager().getGameWorld() + " 不存在！");
            MessageUtil.send(player, "§c服务器配置错误：游戏世界不存在");
            return;
        }

        // 从PDC读取上次位置
        Location lastLocation = getLastLocationFromPDC(player);

        if (lastLocation != null) {
            // 有上次位置记录（老玩家），传送到上次位置
            player.teleport(lastLocation);
            plugin.getLogger().info(player.getName() + " 恢复到上次位置: " +
                String.format("%.1f, %.1f, %.1f (%s)",
                    lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(),
                    lastLocation.getWorld().getName()));
            MessageUtil.send(player, "§a已到达游戏区域！");
        } else {
            // 无上次位置记录（首次进入），异步随机传送
            plugin.getLogger().info(player.getName() + " 首次进入游戏，异步生成随机出生点");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                long startTime = System.currentTimeMillis();
                Location randomLocation = findSafeRandomLocation(gameWorld);
                long duration = System.currentTimeMillis() - startTime;

                // 回到主线程传送玩家并设置出生点
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(randomLocation);

                    // 保存出生点到PDC（用于死亡重生）
                    saveSpawnPointToPDC(player, randomLocation);

                    // 保存到PDC作为上次位置
                    saveLastLocationToPDC(player, randomLocation);

                    plugin.getLogger().info(player.getName() + " 首次进入游戏，出生点设置为: " +
                        String.format("%.1f, %.1f, %.1f (耗时: %dms)",
                            randomLocation.getX(), randomLocation.getY(), randomLocation.getZ(), duration));

                    MessageUtil.send(player, "§a已到达游戏区域！");
                    MessageUtil.send(player, "§7这是你的出生点，死亡后会在此重生");
                });
            });
        }
    }

    /**
     * 寻找安全的随机位置（FastRTP范围 + 主城保护圈）
     */
    private Location findSafeRandomLocation(World world) {
        int maxTries = 50;
        int minRadius = 1000;   // 最小半径（保护主城周围 1000格）
        int maxRadius = 25000;  // 最大半径（FastRTP默认）

        for (int i = 0; i < maxTries; i++) {
            // 在圆形区域内随机选择位置（而不是正方形）
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);

            int x = (int) (radius * Math.cos(angle));
            int z = (int) (radius * Math.sin(angle));

            // 1️⃣ 先检查生物群系（FastRTP核心优化）
            // 这一步不需要加载区块，非常快
            Biome biome = world.getBiome(x, 64, z);
            if (BLACKLISTED_BIOMES.contains(biome)) {
                continue;  // 跳过海洋等生物群系
            }

            // 2️⃣ 获取地表高度（需要加载区块）
            int y = world.getHighestBlockYAt(x, z);

            // 检查高度是否合理（避免虚空或过高）
            if (y < 60 || y > 200) {
                continue;
            }

            Location location = new Location(world, x + 0.5, y, z + 0.5);

            // 3️⃣ 完整的安全检查
            if (isLocationSafe(location)) {
                return location;
            }
        }

        // 如果找不到安全位置，返回世界出生点
        plugin.getLogger().warning("无法找到安全的随机位置，使用世界出生点");
        return world.getSpawnLocation();
    }

    /**
     * 完整的位置安全检查（借鉴FastRTP）
     */
    private boolean isLocationSafe(Location location) {
        // 检查脚下方块
        Material ground = location.getBlock().getType();
        if (isDangerousBlock(ground) || !ground.isSolid()) {
            return false;
        }

        // 检查头部空间（防止窒息）
        Location head1 = location.clone().add(0, 1, 0);
        Location head2 = location.clone().add(0, 2, 0);

        Material headBlock1 = head1.getBlock().getType();
        Material headBlock2 = head2.getBlock().getType();

        if (headBlock1.isSolid() || headBlock2.isSolid()) {
            return false;  // 会窒息
        }

        // 检查周围是否有危险（4格范围）
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Location check = location.clone().add(dx, 0, dz);
                Material blockType = check.getBlock().getType();

                if (isDangerousBlock(blockType)) {
                    return false;  // 周围有危险
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

    /**
     * 从PDC读取上次位置
     */
    private Location getLastLocationFromPDC(Player player) {
        NamespacedKey keyX = new NamespacedKey(plugin, "last_world_x");
        NamespacedKey keyY = new NamespacedKey(plugin, "last_world_y");
        NamespacedKey keyZ = new NamespacedKey(plugin, "last_world_z");
        NamespacedKey keyWorld = new NamespacedKey(plugin, "last_world_name");
        NamespacedKey keyYaw = new NamespacedKey(plugin, "last_world_yaw");
        NamespacedKey keyPitch = new NamespacedKey(plugin, "last_world_pitch");

        Double x = player.getPersistentDataContainer().get(keyX, PersistentDataType.DOUBLE);
        Double y = player.getPersistentDataContainer().get(keyY, PersistentDataType.DOUBLE);
        Double z = player.getPersistentDataContainer().get(keyZ, PersistentDataType.DOUBLE);
        String worldName = player.getPersistentDataContainer().get(keyWorld, PersistentDataType.STRING);
        Float yaw = player.getPersistentDataContainer().get(keyYaw, PersistentDataType.FLOAT);
        Float pitch = player.getPersistentDataContainer().get(keyPitch, PersistentDataType.FLOAT);

        if (x != null && y != null && z != null && worldName != null && yaw != null && pitch != null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                return new Location(world, x, y, z, yaw, pitch);
            }
        }

        return null;
    }

    /**
     * 保存位置到PDC
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

    /**
     * 保存出生点到PDC（用于PlayerRespawnListener）
     */
    private void saveSpawnPointToPDC(Player player, Location location) {
        NamespacedKey keyX = new NamespacedKey(plugin, "spawn_point_x");
        NamespacedKey keyY = new NamespacedKey(plugin, "spawn_point_y");
        NamespacedKey keyZ = new NamespacedKey(plugin, "spawn_point_z");
        NamespacedKey keyWorld = new NamespacedKey(plugin, "spawn_point_world");

        player.getPersistentDataContainer().set(keyX, PersistentDataType.DOUBLE, location.getX());
        player.getPersistentDataContainer().set(keyY, PersistentDataType.DOUBLE, location.getY());
        player.getPersistentDataContainer().set(keyZ, PersistentDataType.DOUBLE, location.getZ());
        player.getPersistentDataContainer().set(keyWorld, PersistentDataType.STRING, location.getWorld().getName());
    }
}
