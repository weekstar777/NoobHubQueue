package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AuthMeLoginListener implements Listener {

    private final NoobHubQueue plugin;

    public AuthMeLoginListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthMeLogin(LoginEvent event) {
        Player player = event.getPlayer();

        // AuthMe登录成功，清除刚登录标记，开始正常的安全检查
        plugin.getQueueManager().clearRecentlyJoined(player.getUniqueId());

        // AuthMe登录成功后，检查游戏世界人数
        int currentPlayers = plugin.getQueueManager().getGamePlayers();
        int maxPlayers = plugin.getConfigManager().getMaxGamePlayers();

        if (currentPlayers >= maxPlayers) {
            // 游戏世界已满，自动加入排队
            plugin.getQueueManager().addToQueue(player);
        } else {
            // 游戏世界未满，占用名额（即使还没/play）
            plugin.getQueueManager().reserveSlot(player);
            MessageUtil.send(player, "§7输入 §6/play §7进入游戏");
        }

        plugin.getLogger().info(player.getName() + " AuthMe登录成功，已处理排队逻辑");
    }
}
