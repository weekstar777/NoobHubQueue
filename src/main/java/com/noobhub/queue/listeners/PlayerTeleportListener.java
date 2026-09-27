package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerTeleportListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // 如果玩家在主城（访客模式），阻止其他插件的传送
        if (!plugin.getQueueManager().isGuestMode(player.getUniqueId())) {
            return; // 不在主城，允许传送
        }

        // 玩家在主城（访客模式）
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            Location to = event.getTo();
            Location lobby = plugin.getConfigManager().getLobbyLocation();

            // 判断目标位置是否是主城
            if (to != null && !isSameLocation(to, lobby)) {
                // 不是传送到主城，阻止（可能是RandomSpawn等插件）
                event.setCancelled(true);
                plugin.getLogger().info("阻止 " + player.getName() + " 被其他插件传送到 " +
                    String.format("%.1f, %.1f, %.1f", to.getX(), to.getY(), to.getZ()));
            }
        }
    }

    /**
     * 判断两个位置是否相同（容差5格）
     */
    private boolean isSameLocation(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }

        return Math.abs(loc1.getX() - loc2.getX()) < 5
            && Math.abs(loc1.getY() - loc2.getY()) < 5
            && Math.abs(loc1.getZ() - loc2.getZ()) < 5;
    }
}
