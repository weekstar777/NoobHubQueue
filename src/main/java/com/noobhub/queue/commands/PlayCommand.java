package com.noobhub.queue.commands;

import com.noobhub.queue.NoobHubQueue;
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

        // 如果玩家在队列中
        if (plugin.getQueueManager().isInQueue(uuid)) {
            // 检查是否轮到了
            UUID nextPlayer = plugin.getQueueManager().getNextPlayer();
            int gamePlayers = getGamePlayers();
            int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

            if (uuid.equals(nextPlayer) && gamePlayers < maxPlayers) {
                // 轮到了！处理进入游戏
                handlePlayCommand(player);
            } else {
                // 还没轮到
                boolean isVip = plugin.getQueueManager().isInVipQueue(uuid);
                String channel = isVip ? "§6[VIP通道]" : "§7[普通通道]";
                int position = plugin.getQueueManager().getPosition(uuid);

                player.sendMessage(channel + " §c还没轮到你！");
                player.sendMessage("  §8▸ §7你的位置: §e第 " + position + " 位");

                if (!isVip) {
                    int vipAhead = plugin.getQueueManager().getVipQueueSize();
                    if (vipAhead > 0) {
                        player.sendMessage("  §8▸ §7VIP通道前方: §e" + vipAhead + " §7人");
                    }
                }
            }
        } else {
            // 不在队列中
            player.sendMessage("§e你已经在游戏中了！");
        }

        return true;
    }

    /**
     * 处理 /play 指令 - 玩家进入游戏
     */
    private void handlePlayCommand(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isVip = plugin.getQueueManager().isInVipQueue(uuid);
        String channel = isVip ? "§6[VIP通道]" : "§7[普通通道]";

        // 从队列移除
        plugin.getQueueManager().removeFromQueue(uuid);

        player.sendMessage(channel + " §a正在进入游戏...");

        // 获取目标位置
        Location targetLocation = plugin.getQueueManager().getTargetLocation(uuid);

        if (targetLocation != null) {
            // 传送到记录的位置
            player.teleport(targetLocation);
            plugin.getLogger().info((isVip ? "[VIP]" : "[普通]") + " " + player.getName() +
                " 执行/play进入游戏，传送到: " + String.format("%.1f, %.1f, %.1f",
                targetLocation.getX(), targetLocation.getY(), targetLocation.getZ()));

            // 移除位置记录
            plugin.getQueueManager().removeTargetLocation(uuid);
        } else {
            // 如果没有记录位置，传送到世界出生点
            Location spawn = player.getWorld().getSpawnLocation();
            player.teleport(spawn);
            plugin.getLogger().warning(player.getName() + " 没有记录的目标位置，传送到世界出生点");
        }

        // 恢复玩家状态
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setHealth(player.getMaxHealth());
    }

    /**
     * 获取当前在游戏中的玩家数（不包括排队的）
     */
    private int getGamePlayers() {
        return plugin.getServer().getOnlinePlayers().size() - plugin.getQueueManager().getQueueSize();
    }
}
