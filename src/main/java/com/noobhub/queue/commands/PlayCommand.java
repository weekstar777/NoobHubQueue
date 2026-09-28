package com.noobhub.queue.commands;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

        // 不在队列中（前100名或已被通知），直接传送
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
        World gameWorld = Bukkit.getWorld("world");
        if (gameWorld == null) {
            plugin.getLogger().severe("游戏世界 world 不存在！");
            MessageUtil.send(player, "§c服务器配置错误：游戏世界不存在");
            return;
        }

        // 检查游戏世界的 playerdata 文件是否存在
        File worldFolder = gameWorld.getWorldFolder();
        File playerDataFile = new File(worldFolder, "playerdata/" + uuid.toString() + ".dat");
        boolean isFirstTime = !playerDataFile.exists();

        if (isFirstTime) {
            // 首次进入：异步随机传送并设置固定出生点
            plugin.getLogger().info(player.getName() + " 首次进入游戏（playerdata 不存在），异步生成随机出生点");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                long startTime = System.currentTimeMillis();
                Location randomLocation = findSafeRandomLocation(gameWorld);
                long duration = System.currentTimeMillis() - startTime;

                // 回到主线程传送玩家
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(randomLocation);

                    // 保存为玩家的固定出生点（MySQL）
                    plugin.getPlayerDataRepository().saveSpawnPoint(uuid, randomLocation);

                    plugin.getLogger().info(player.getName() + " 首次进入游戏，传送到随机位置: " +
                        String.format("%.1f, %.1f, %.1f (耗时: %dms)",
                            randomLocation.getX(), randomLocation.getY(), randomLocation.getZ(), duration));

                    MessageUtil.send(player, "§a已到达游戏区域！");
                    MessageUtil.send(player, "§7这是你的固定出生点，死亡后会在此重生");
                });
            });
        } else {
            // 非首次：直接传送到游戏世界，Minecraft 会自动把玩家放到上次退出位置
            player.teleport(gameWorld.getSpawnLocation()); // 触发世界切换
            plugin.getLogger().info(player.getName() + " 进入游戏世界，Minecraft 将自动恢复上次位置");
            MessageUtil.send(player, "§a已到达游戏区域！");
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
}
