/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.luckperms.api.LuckPerms
 *  net.luckperms.api.LuckPermsProvider
 *  net.luckperms.api.model.user.User
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.oefen.friendsystem.listener;

import com.oefen.friendsystem.FriendSystem;
import com.oefen.friendsystem.VersionHandler;
import com.oefen.friendsystem.cmd.FriendCommand;
import com.oefen.friendsystem.model.FriendRequest;
import com.oefen.friendsystem.model.PlayerData;
import com.oefen.friendsystem.model.PlayerSettings;
import com.oefen.friendsystem.util.ActionBarUtil;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FriendListener
implements Listener {
    private final FriendSystem plugin;
    private final Map<UUID, Integer> actionBarTasks = new ConcurrentHashMap<UUID, Integer>();

    public FriendListener(FriendSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        new BukkitRunnable(){

            public void run() {
                FriendListener.this.handlePlayerJoin(player);
                FriendListener.this.startPersistentActionBar(player);
            }
        }.runTaskLater((Plugin)this.plugin, 10L);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        this.handlePlayerQuit(player);
        this.stopPersistentActionBar(playerUUID);
    }

    private void handlePlayerJoin(Player player) {
        Set<UUID> friends;
        UUID playerUUID = player.getUniqueId();
        this.plugin.getRequestManager().activateRequestsFor(playerUUID);
        this.plugin.getRequestManager().clearExpiredRequests();
        this.plugin.getDataManager().clearExpiredRequests();
        PlayerData playerData = this.plugin.getDataManager().getPlayerData(playerUUID);
        playerData.setPlayerName(player.getName());
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(playerUUID);
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix == null) {
                    prefix = "";
                }
                playerData.setLastKnownPrefix(prefix);
                String color = FriendListener.extractLastColorCode(prefix);
                playerData.setLastKnownColor(color);
            }
        }
        catch (Exception luckPerms) {
            // empty catch block
        }
        this.plugin.getDataManager().savePlayerData(playerData);
        Map<UUID, FriendRequest> incomingRequests = this.plugin.getRequestManager().getIncomingRequests(playerUUID);
        if (!incomingRequests.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&eYou have &6" + incomingRequests.size() + " &epending friend request(s)!")));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&7Use &e/friend requests &7to view them."));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        }
        if ((friends = this.plugin.getFriendManager().getFriends(playerUUID)).isEmpty()) {
            return;
        }
        if (this.plugin.getDataManager().getPlayerData(playerUUID).getSettings().isAppearOffline()) {
            return;
        }
        String display = FriendCommand.getRankColorName(playerUUID, playerData);
        String message = "&aFriend > " + display + " &ejoined.";
        for (UUID friendUUID : friends) {
            Player friend = VersionHandler.getOnlinePlayer(friendUUID);
            if (friend == null || !friend.isOnline() || !this.plugin.getDataManager().getPlayerData(friendUUID).getSettings().isNotifyOnline()) continue;
            this.plugin.getMessageManager().sendSystemMessage(friendUUID, message);
        }
        this.plugin.getRequestManager().clearSenderBlocksOnLogin(playerUUID);
    }

    private void handlePlayerQuit(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<UUID> friends = this.plugin.getFriendManager().getFriends(playerUUID);
        if (friends.isEmpty()) {
            return;
        }
        if (this.plugin.getDataManager().getPlayerData(playerUUID).getSettings().isAppearOffline()) {
            return;
        }
        PlayerData playerData = this.plugin.getDataManager().getPlayerData(playerUUID);
        String display = FriendCommand.getRankColorName(playerUUID, playerData);
        String message = "&aFriend > " + display + " &eleft.";
        for (UUID friendUUID : friends) {
            Player friend = VersionHandler.getOnlinePlayer(friendUUID);
            if (friend == null || !friend.isOnline() || !this.plugin.getDataManager().getPlayerData(friendUUID).getSettings().isNotifyOnline()) continue;
            this.plugin.getMessageManager().sendSystemMessage(friendUUID, message);
        }
        this.plugin.getMessageManager().clearMessageHistory(playerUUID);
        playerData.setLastLogoutTime(System.currentTimeMillis());
        this.plugin.getDataManager().savePlayerData(playerData);
    }

    public void scheduleCleanupTasks() {
        new BukkitRunnable(){

            public void run() {
                FriendListener.this.plugin.getRequestManager().clearExpiredRequests();
                FriendListener.this.plugin.getDataManager().clearExpiredRequests();
            }
        }.runTaskTimer((Plugin)this.plugin, 6000L, 6000L);
        new BukkitRunnable(){

            public void run() {
                FriendListener.this.plugin.getDataManager().saveAllData();
            }
        }.runTaskTimer((Plugin)this.plugin, 12000L, 12000L);
    }

    private void startPersistentActionBar(Player player) {
        UUID uuid = player.getUniqueId();
        this.stopPersistentActionBar(uuid);
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (!FriendSystem.isGuiAllowed(player)) {
                ActionBarUtil.sendActionBar(player, "");
                return;
            }
            PlayerSettings.OnlineStatus status = this.plugin.getDataManager().getPlayerData(uuid).getSettings().getOnlineStatus();
            if (status == PlayerSettings.OnlineStatus.ONLINE) {
                ActionBarUtil.sendActionBar(player, "");
            } else if (status == PlayerSettings.OnlineStatus.AWAY) {
                ActionBarUtil.sendActionBar(player, "\u00a7fYou are currently \u00a7cAWAY");
            } else if (status == PlayerSettings.OnlineStatus.BUSY) {
                ActionBarUtil.sendActionBar(player, "\u00a7fYou are currently \u00a7cBUSY");
            } else if (status == PlayerSettings.OnlineStatus.APPEAR_OFFLINE) {
                ActionBarUtil.sendActionBar(player, "\u00a7fYou are currently \u00a7cAPPEARING OFFLINE");
            }
        }, 0L, 20L);
        this.actionBarTasks.put(uuid, taskId);
    }

    private void stopPersistentActionBar(UUID uuid) {
        Integer taskId = this.actionBarTasks.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId.intValue());
        }
    }

    private static String extractLastColorCode(String input) {
        String last = "";
        if (input == null) {
            return "&f";
        }
        for (int i = 0; i < input.length() - 1; ++i) {
            if (input.charAt(i) != '\u00a7' && input.charAt(i) != '&') continue;
            last = "&" + input.charAt(i + 1);
        }
        return last.isEmpty() ? "&f" : last;
    }
}

