package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import com.noobhub.queue.utils.TeleportUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerJoinListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 判断是否是新玩家
        boolean isNewPlayer = !player.hasPlayedBefore();

        if (isNewPlayer) {
            // 新玩家：立即传送到等待平台，不延迟
            plugin.getLogger().info(player.getName() + " 是新玩家，立即传送到等待平台");

            // 立即传送（防止看到游戏区）
            TeleportUtil.teleportToLobby(player, plugin);

            // 设置访客模式
            setGuestMode(player);
            plugin.getQueueManager().setGuestMode(player.getUniqueId(), true);

            // 检查名额分配
            handleQueueAssignment(player);

        } else {
            // 老玩家：延迟1 tick，等待Paper恢复位置
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                handleOldPlayerJoin(player);

            }, 1L);
        }
    }

    /**
     * 处理老玩家进服
     */
    private void handleOldPlayerJoin(Player player) {
        // 1. 检查并记录玩家位置（仅记录游戏区位置）
        Location currentLocation = player.getLocation().clone();
        Location lobbyLocation = plugin.getConfigManager().getLobbyLocation();

        // 判断当前位置是否在等待平台附近（20格范围内）
        boolean isInLobby = currentLocation.getWorld().equals(lobbyLocation.getWorld())
            && Math.abs(currentLocation.getX() - lobbyLocation.getX()) < 20
            && Math.abs(currentLocation.getY() - lobbyLocation.getY()) < 20
            && Math.abs(currentLocation.getZ() - lobbyLocation.getZ()) < 20;

        if (!isInLobby) {
            // 不在等待平台，记录游戏区位置并保存物品
            plugin.getQueueManager().recordTargetLocation(player.getUniqueId(), currentLocation);
            plugin.getQueueManager().saveInventory(player);
            plugin.getLogger().info("记录 " + player.getName() + " 的游戏区位置和物品: " +
                String.format("%.1f, %.1f, %.1f", currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()));
        } else {
            // 在等待平台，不更新位置和物品（保留之前的游戏区数据）
            plugin.getLogger().info(player.getName() + " 在等待平台退出，保留之前的位置和物品数据");
        }

        // 2. 传送到等待大厅
        TeleportUtil.teleportToLobby(player, plugin);

        // 3. 设置访客模式（清空物品栏）
        setGuestMode(player);

        // 标记为访客模式
        plugin.getQueueManager().setGuestMode(player.getUniqueId(), true);

        // 4. 检查名额分配
        handleQueueAssignment(player);
    }

    /**
     * 处理排队分配逻辑
     */
    private void handleQueueAssignment(Player player) {
        // 检查已占用名额，决定是否需要排队
        // 已占用名额 = 当前在线人数（包括自己）- 排队人数
        int occupiedSlots = Bukkit.getOnlinePlayers().size() - plugin.getQueueManager().getQueueSize();
        int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

        plugin.getLogger().info(player.getName() + " 加入服务器，当前占用名额: " + occupiedSlots + "/" + maxPlayers);

        if (occupiedSlots <= maxPlayers) {
            // 有空位，玩家占用一个名额，可以随时 /play 传送
            MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");
            MessageUtil.send(player, "§7输入 §6/play §7即可进入游戏");
            plugin.getLogger().info(player.getName() + " 占用名额 " + occupiedSlots + "/" + maxPlayers + "，可随时传送");
        } else {
            // 已满100人，加入排队系统等待空位
            plugin.getQueueManager().addToQueue(player);
            plugin.getLogger().info(player.getName() + " 加入排队系统（名额已满）");
        }
    }

    /**
     * 设置访客模式
     */
    private void setGuestMode(Player player) {
        // 清空物品栏
        player.getInventory().clear();

        // 清空装备
        player.getInventory().setArmorContents(null);

        // 设置饱食度
        player.setFoodLevel(20);
        player.setSaturation(20);

        // 设置生命值
        player.setHealth(player.getMaxHealth());

        plugin.getLogger().info(player.getName() + " 已设置为访客模式");
    }
}


