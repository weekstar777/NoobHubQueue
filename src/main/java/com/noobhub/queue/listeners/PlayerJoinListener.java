package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import com.noobhub.queue.utils.TeleportUtil;
import org.bukkit.Bukkit;
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

        plugin.getLogger().info(player.getName() + " 进入服务器，传送到主城世界");

        // 1. 传送到主城世界（world_lobby）
        // Minecraft 会自动加载该世界的 playerdata（空物品栏）
        org.bukkit.World lobbyWorld = Bukkit.getWorld("world_lobby");
        if (lobbyWorld == null) {
            plugin.getLogger().severe("主城世界 world_lobby 不存在！请先创建主城世界。");
            player.kickPlayer("§c服务器配置错误：主城世界不存在");
            return;
        }

        // 传送到主城世界的出生点
        org.bukkit.Location lobbySpawn = lobbyWorld.getSpawnLocation();
        player.teleport(lobbySpawn);

        // 2. 设置冒险模式
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);

        // 3. 标记为访客模式
        plugin.getQueueManager().setGuestMode(player.getUniqueId(), true);

        // 4. 所有玩家都发送欢迎消息
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");

        // 5. 检查名额分配
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
            MessageUtil.send(player, "§7输入 §6/play §7即可进入游戏");
            plugin.getLogger().info(player.getName() + " 占用名额 " + occupiedSlots + "/" + maxPlayers + "，可随时传送");
        } else {
            // 已满100人，加入排队系统等待空位
            plugin.getQueueManager().addToQueue(player);
            plugin.getLogger().info(player.getName() + " 加入排队系统（名额已满）");
        }
    }

    /**
     * 设置访客模式（主城等待）
     * 注意：使用双世界方案后，物品栏由 Minecraft 自动管理，不需要手动清空
     */
    private void setGuestMode(Player player) {
        // 已经在主城世界，Minecraft 自动加载该世界的 playerdata
        // 物品栏会自动是空的（或该世界上次的状态）

        // 只需要确保游戏模式、血量、饥饿值正确
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setHealth(player.getMaxHealth());

        plugin.getLogger().info(player.getName() + " 已设置为访客模式（冒险模式）");
    }
}
