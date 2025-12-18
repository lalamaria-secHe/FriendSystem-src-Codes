/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.oefen.friendsystem;

import com.oefen.friendsystem.VersionHandler;
import com.oefen.friendsystem.cmd.BlockCommand;
import com.oefen.friendsystem.cmd.BlockCommandTabCompleter;
import com.oefen.friendsystem.cmd.FriendCommand;
import com.oefen.friendsystem.cmd.FriendSystemCommand;
import com.oefen.friendsystem.cmd.FriendSystemCommandTabCompleter;
import com.oefen.friendsystem.cmd.MessageCommand;
import com.oefen.friendsystem.cmd.MessageCommandTabCompleter;
import com.oefen.friendsystem.cmd.ReplyCommand;
import com.oefen.friendsystem.cmd.ReplyCommandTabCompleter;
import com.oefen.friendsystem.listener.FriendListener;
import com.oefen.friendsystem.manager.BlockManager;
import com.oefen.friendsystem.manager.DataManager;
import com.oefen.friendsystem.manager.FriendManager;
import com.oefen.friendsystem.manager.MessageManager;
import com.oefen.friendsystem.manager.RequestManager;
import java.util.List;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class FriendSystem
extends JavaPlugin {
    private static FriendSystem instance;
    private DataManager dataManager;
    private FriendManager friendManager;
    private RequestManager requestManager;
    private MessageManager messageManager;
    private BlockManager blockManager;
    private BlockCommand blockCommand;
    private List<String> allowedWorlds;
    private List<String> allowedServers;

    public void onEnable() {
        instance = this;
        VersionHandler.init();
        this.saveDefaultConfig();
        this.reloadConfig();
        this.loadGuiConfig();
        this.initializeManagers();
        this.dataManager.loadAllData();
        this.requestManager.loadAllPendingRequests();
        this.registerCommands();
        this.registerListeners();
        this.getLogger().info("FriendSystem has been enabled successfully!");
        this.getLogger().info("Server version: " + VersionHandler.getServerVersion());
    }

    public void onDisable() {
        if (this.dataManager != null) {
            this.dataManager.saveAllData();
        }
        this.getLogger().info("FriendSystem has been disabled!");
    }

    private void initializeManagers() {
        this.dataManager = new DataManager(this);
        this.friendManager = new FriendManager(this);
        this.requestManager = new RequestManager(this);
        this.messageManager = new MessageManager(this);
        this.blockManager = new BlockManager(this);
        this.blockCommand = new BlockCommand(this);
    }

    private void registerCommands() {
        FriendCommand friendCommand = new FriendCommand(this);
        this.getCommand("friend").setExecutor((CommandExecutor)friendCommand);
        this.getCommand("f").setExecutor((CommandExecutor)friendCommand);
        this.getCommand("msg").setExecutor((CommandExecutor)new MessageCommand(this));
        this.getCommand("msg").setTabCompleter((TabCompleter)new MessageCommandTabCompleter(this));
        this.getCommand("r").setExecutor((CommandExecutor)new ReplyCommand(this));
        this.getCommand("r").setTabCompleter((TabCompleter)new ReplyCommandTabCompleter());
        this.getCommand("block").setExecutor((CommandExecutor)new BlockCommand(this));
        this.getCommand("block").setTabCompleter((TabCompleter)new BlockCommandTabCompleter());
        this.getCommand("friendsystem").setExecutor((CommandExecutor)new FriendSystemCommand(this));
        this.getCommand("friendsystem").setTabCompleter((TabCompleter)new FriendSystemCommandTabCompleter());
    }

    private void registerListeners() {
        FriendListener listener = new FriendListener(this);
        this.getServer().getPluginManager().registerEvents((Listener)listener, (Plugin)this);
        listener.scheduleCleanupTasks();
    }

    public static FriendSystem getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public FriendManager getFriendManager() {
        return this.friendManager;
    }

    public RequestManager getRequestManager() {
        return this.requestManager;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public BlockManager getBlockManager() {
        return this.blockManager;
    }

    public void loadGuiConfig() {
        this.allowedWorlds = this.getConfig().getStringList("gui.lobby_worlds");
        this.allowedServers = this.getConfig().getStringList("gui.lobby_servers");
    }

    public static boolean isGuiAllowed(Player player) {
        FriendSystem plugin = FriendSystem.getInstance();
        String world = player.getWorld().getName();
        if (plugin.allowedWorlds != null && plugin.allowedWorlds.contains(world)) {
            return true;
        }
        String server = player.getServer().getServerName();
        return plugin.allowedServers != null && plugin.allowedServers.contains(server);
    }
}

