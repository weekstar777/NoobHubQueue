package com.noobhub.queue.commands;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayCommand implements CommandExecutor {

    private final NoobHubQueue plugin;

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

        // 检查是否在等待大厅（访客模式）
        if (!plugin.getQueueManager().isGuestMode(uuid)) {
            MessageUtil.send(player, "§c你不在等待大厅！");
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

        // 取消访客模式
        plugin.getQueueManager().setGuestMode(uuid, false);

        MessageUtil.send(player, "§a§l正在进入游戏...");

        // 恢复玩家物品和装备
        plugin.getQueueManager().restoreInventory(player);

        // 获取目标位置
        Location targetLocation = plugin.getQueueManager().getTargetLocation(uuid);

        if (targetLocation != null) {
            // 有记录的位置，传送过去
            player.teleport(targetLocation);
            plugin.getLogger().info(player.getName() + " 执行/play进入游戏，传送到: " +
                String.format("%.1f, %.1f, %.1f", targetLocation.getX(), targetLocation.getY(), targetLocation.getZ()));

            // 移除位置记录
            plugin.getQueueManager().removeTargetLocation(uuid);
        } else {
            // 没有记录位置（新玩家首次/play）
            // 生成随机出生位置
            plugin.getLogger().info(player.getName() + " 没有记录位置，生成随机出生点");

            Location randomLocation = generateRandomSpawnLocation(player.getWorld());
            player.teleport(randomLocation);

            plugin.getLogger().info(player.getName() + " 首次进入游戏，传送到随机位置: " +
                String.format("%.1f, %.1f, %.1f", randomLocation.getX(), randomLocation.getY(), randomLocation.getZ()));
        }

        MessageUtil.send(player, "§a欢迎来到游戏！");
    }

    /**
     * 生成随机出生位置（模拟RandomSpawn的逻辑）
     */
    private Location generateRandomSpawnLocation(org.bukkit.World world) {
        java.util.Random random = new java.util.Random();
        int maxTries = 50;

        for (int i = 0; i < maxTries; i++) {
            // 在 -1000 到 1000 范围内随机选择 X 和 Z
            int x = random.nextInt(2000) - 1000;
            int z = random.nextInt(2000) - 1000;

            // 获取该位置的最高方块（地表）
            int y = world.getHighestBlockYAt(x, z);

            Location location = new Location(world, x + 0.5, y, z + 0.5);

            // 检查是否是安全位置（不在危险方块上）
            if (isSafeLocation(location)) {
                return location;
            }
        }

        // 如果找不到安全位置，返回世界出生点
        return world.getSpawnLocation();
    }

    /**
     * 检查位置是否安全
     */
    private boolean isSafeLocation(Location location) {
        org.bukkit.Material blockType = location.getBlock().getType();

        // 危险方块列表
        return blockType != org.bukkit.Material.LAVA
            && blockType != org.bukkit.Material.FIRE
            && blockType != org.bukkit.Material.CACTUS
            && blockType != org.bukkit.Material.MAGMA_BLOCK
            && blockType != org.bukkit.Material.CAMPFIRE
            && blockType != org.bukkit.Material.SOUL_CAMPFIRE
            && blockType != org.bukkit.Material.WITHER_ROSE
            && blockType != org.bukkit.Material.SWEET_BERRY_BUSH;
    }
}
