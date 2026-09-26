package com.noobhub.queue.managers;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {

    private final NoobHubQueue plugin;

    // VIP通道（优先）
    private final LinkedList<UUID> vipQueue;

    // 普通通道
    private final LinkedList<UUID> normalQueue;

    // 记录加入时间
    private final Map<UUID, Long> joinTime;

    // 记录玩家的目标位置
    private final Map<UUID, Location> targetLocations;

    // 记录已经通知过"轮到你了"的玩家，避免重复通知
    private final Set<UUID> notifiedPlayers;

    // 记录处于访客模式的玩家（在等待大厅）
    private final Set<UUID> guestModePlayers;

    private BukkitTask checkTask;
    private LuckPerms luckPerms;

    public QueueManager(NoobHubQueue plugin) {
        this.plugin = plugin;
        this.vipQueue = new LinkedList<>();
        this.normalQueue = new LinkedList<>();
        this.joinTime = new ConcurrentHashMap<>();
        this.targetLocations = new ConcurrentHashMap<>();
        this.notifiedPlayers = ConcurrentHashMap.newKeySet();
        this.guestModePlayers = ConcurrentHashMap.newKeySet();

        // 尝试获取LuckPerms
        try {
            this.luckPerms = LuckPermsProvider.get();
            plugin.getLogger().info("已连接到 LuckPerms");
        } catch (IllegalStateException e) {
            plugin.getLogger().warning("未找到 LuckPerms，VIP功能将被禁用");
            this.luckPerms = null;
        }
    }

    /**
     * 记录玩家的目标位置
     */
    public void recordTargetLocation(UUID uuid, Location location) {
        targetLocations.put(uuid, location);
    }

    /**
     * 获取玩家的目标位置
     */
    public Location getTargetLocation(UUID uuid) {
        return targetLocations.get(uuid);
    }

    /**
     * 移除玩家的目标位置记录
     */
    public void removeTargetLocation(UUID uuid) {
        targetLocations.remove(uuid);
    }

    /**
     * 添加玩家到队列
     */
    public synchronized void addToQueue(Player player) {
        UUID uuid = player.getUniqueId();

        if (isInQueue(uuid)) {
            return;
        }

        joinTime.put(uuid, System.currentTimeMillis());

        if (isVIP(player)) {
            vipQueue.addLast(uuid);
            int vipPosition = vipQueue.indexOf(uuid) + 1;

            MessageUtil.send(player, "§6§l[VIP通道] §e你已进入VIP排队 §8| §6位置: §e" + vipPosition + "§6/§e" + vipQueue.size());
            plugin.getLogger().info("[VIP] " + player.getName() + " 加入VIP通道，位置: " + vipPosition);
        } else {
            normalQueue.addLast(uuid);
            int normalPosition = normalQueue.indexOf(uuid) + 1;
            int vipAhead = vipQueue.size();

            MessageUtil.send(player, "§7§l[普通通道] §7你已加入排队 §8| §e位置: §7" + normalPosition + "§e/§7" + normalQueue.size());
            if (vipAhead > 0) {
                MessageUtil.send(player, "  §8▸ §7VIP通道前方: §e" + vipAhead + " §7人（优先进入）");
            }
            plugin.getLogger().info("[普通] " + player.getName() + " 加入普通通道，位置: " + normalPosition);
        }

        // 立即更新所有排队玩家的ActionBar显示
        updateQueueDisplay();
    }

    /**
     * 从队列移除玩家
     */
    public synchronized void removeFromQueue(UUID uuid) {
        boolean removed = false;

        if (vipQueue.remove(uuid)) {
            plugin.getLogger().info("玩家 " + uuid + " 已从VIP通道移除");
            removed = true;
        }

        if (normalQueue.remove(uuid)) {
            plugin.getLogger().info("玩家 " + uuid + " 已从普通通道移除");
            removed = true;
        }

        if (removed) {
            joinTime.remove(uuid);
            notifiedPlayers.remove(uuid);  // 清除通知标记

            // 立即更新所有排队玩家的ActionBar显示
            updateQueueDisplay();
        }
    }

    public synchronized int getPosition(UUID uuid) {
        int vipIndex = vipQueue.indexOf(uuid);
        if (vipIndex != -1) {
            return vipIndex + 1;
        }

        int normalIndex = normalQueue.indexOf(uuid);
        if (normalIndex != -1) {
            return normalIndex + 1;
        }

        return -1;
    }

    public synchronized UUID getNextPlayer() {
        if (!vipQueue.isEmpty()) {
            return vipQueue.getFirst();
        }

        if (!normalQueue.isEmpty()) {
            return normalQueue.getFirst();
        }

        return null;
    }

    public int getQueueSize() {
        return vipQueue.size() + normalQueue.size();
    }

    public int getVipQueueSize() {
        return vipQueue.size();
    }

    public int getNormalQueueSize() {
        return normalQueue.size();
    }

    public boolean isInQueue(UUID uuid) {
        return vipQueue.contains(uuid) || normalQueue.contains(uuid);
    }

    public boolean isInVipQueue(UUID uuid) {
        return vipQueue.contains(uuid);
    }

    /**
     * 设置玩家的访客模式状态
     */
    public void setGuestMode(UUID uuid, boolean enabled) {
        if (enabled) {
            guestModePlayers.add(uuid);
        } else {
            guestModePlayers.remove(uuid);
        }
    }

    /**
     * 检查玩家是否处于访客模式
     */
    public boolean isGuestMode(UUID uuid) {
        return guestModePlayers.contains(uuid);
    }

    /**
     * 检查玩家是否已被通知（轮到你了）
     */
    public boolean isNotified(UUID uuid) {
        return notifiedPlayers.contains(uuid);
    }

    /**
     * 清除玩家的通知标记
     */
    public void clearNotification(UUID uuid) {
        notifiedPlayers.remove(uuid);
    }

    private boolean isVIP(Player player) {
        if (luckPerms == null) {
            return player.hasPermission("queue.vip");
        }

        return luckPerms.getUserManager()
            .getUser(player.getUniqueId())
            .getCachedData()
            .getPermissionData()
            .checkPermission("queue.vip")
            .asBoolean();
    }

    /**
     * 启动定时检查任务
     */
    public void startQueueCheck() {
        int interval = plugin.getConfigManager().getCheckInterval();

        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int onlinePlayers = getGamePlayers();
            int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

            // 如果有空位且有排队玩家
            if (onlinePlayers < maxPlayers && getQueueSize() > 0) {
                UUID nextUuid = getNextPlayer();
                if (nextUuid == null) return;

                Player nextPlayer = Bukkit.getPlayer(nextUuid);
                if (nextPlayer == null || !nextPlayer.isOnline()) {
                    // 玩家不在线，从队列移除
                    removeFromQueue(nextUuid);
                    return;
                }

                // 如果已经通知过，跳过（避免重复通知）
                if (notifiedPlayers.contains(nextUuid)) {
                    return;
                }

                // 第一次通知
                boolean isFromVip = vipQueue.contains(nextUuid);
                String channel = isFromVip ? "§6[VIP通道]" : "§7[普通通道]";

                MessageUtil.send(nextPlayer, channel + " §a§l轮到你了！§r§7输入 §e/play §7进入游戏");

                // 显示标题
                Component title = Component.text("§a§l轮到你了！");
                Component subtitle = Component.text("§7输入 §e/play §7进入游戏");
                nextPlayer.showTitle(Title.title(title, subtitle,
                    Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofMillis(3000),
                        Duration.ofMillis(500)
                    )));

                // 立即从队列移除，占用游戏区名额
                removeFromQueue(nextUuid);

                // 标记为已通知（等待传送）
                notifiedPlayers.add(nextUuid);
                plugin.getLogger().info((isFromVip ? "[VIP]" : "[普通]") + " " + nextPlayer.getName() + " 轮到了，已占用名额，等待传送");
            }

            // 更新所有排队玩家的显示
            updateQueueDisplay();

        }, 0L, interval);
    }

    public void stopQueueCheck() {
        if (checkTask != null) {
            checkTask.cancel();
        }
    }

    /**
     * 获取当前已占用的游戏区名额
     * 计算方式：在线总人数 - 排队人数
     *
     * 前100名玩家虽然在等待大厅，但已占用名额
     * 排队玩家未占用名额，需等待空位
     */
    private int getGamePlayers() {
        return Bukkit.getOnlinePlayers().size() - getQueueSize();
    }

    private void updateQueueDisplay() {
        // 更新VIP通道玩家
        int vipIndex = 1;
        for (UUID uuid : vipQueue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                String message = String.format("§6[VIP通道] §7排队: §e%d§7/§e%d",
                    vipIndex, vipQueue.size());
                player.sendActionBar(Component.text(message));
            }
            vipIndex++;
        }

        // 更新普通通道玩家
        int normalIndex = 1;
        int vipAhead = vipQueue.size();
        for (UUID uuid : normalQueue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                String message = String.format("§7[普通通道] §7排队: §e%d§7/§e%d §8| §7VIP通道: §e%d§7人",
                    normalIndex, normalQueue.size(), vipAhead);
                player.sendActionBar(Component.text(message));
            }
            normalIndex++;
        }
    }
}
