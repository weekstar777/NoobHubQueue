package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerJoinListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerJoinListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否处于死亡状态（修复bug：死亡后退出，下次登录仍死亡）
        if (player.isDead()) {
            // 标记为"待恢复死亡状态"，/play时恢复
            NamespacedKey key = new NamespacedKey(plugin, "pending_death");
            player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

            // 强制重生，让玩家活着在lobby
            player.spigot().respawn();
            plugin.getLogger().info(player.getName() + " 登录时处于死亡状态，标记为待恢复死亡");
        }

        // 所有玩家登录时都在lobby
        World lobby = Bukkit.getWorld(plugin.getConfigManager().getLobbyWorld());
        if (lobby != null) {
            Location spawnLocation = new Location(lobby, 0.5, 70.0, 0.5, 0.0f, 90.0f);
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }

        // 发送欢迎消息
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");

        // 排队逻辑移至AuthMeLoginListener，只在AuthMe登录成功后触发
    }
}
