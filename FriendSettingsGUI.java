/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 */
package com.oefen.friendsystem.gui;

import com.oefen.friendsystem.FriendSystem;
import com.oefen.friendsystem.VersionHandler;
import com.oefen.friendsystem.model.PlayerData;
import com.oefen.friendsystem.model.PlayerSettings;
import com.oefen.friendsystem.util.ActionBarUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class FriendSettingsGUI
implements Listener {
    private static final String GUI_TITLE = "\u00a78Social Settings";
    private static final int GUI_SIZE = 54;
    private final FriendSystem plugin;
    private final Player player;
    private final UUID playerUUID;
    private Inventory inventory;

    public FriendSettingsGUI(FriendSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerUUID = player.getUniqueId();
    }

    public void open() {
        this.inventory = Bukkit.createInventory(null, (int)54, (String)GUI_TITLE);
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        this.updateItems();
        this.player.openInventory(this.inventory);
    }

    private void updateItems() {
        this.inventory.clear();
        this.addOnlineStatusSetting();
        this.addRequestPrivacySetting();
        this.addMessagePrivacySetting();
        this.addFriendNotificationsSetting();
    }

    private void addOnlineStatusSetting() {
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(this.playerUUID).getSettings();
        PlayerSettings.OnlineStatus currentStatus = settings.getOnlineStatus();
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("\u00a77Change your Online Status.");
        lore.add(" ");
        lore.add("\u00a77Options:");
        for (PlayerSettings.OnlineStatus status : PlayerSettings.OnlineStatus.values()) {
            String color;
            switch (status) {
                case AWAY: {
                    color = "\u00a7e";
                    break;
                }
                case BUSY: {
                    color = "\u00a75";
                    break;
                }
                case APPEAR_OFFLINE: {
                    color = "\u00a78";
                    break;
                }
                default: {
                    color = "\u00a7a";
                }
            }
            String prefix = status == currentStatus ? color + "\u00bb " : "  ";
            lore.add(prefix + color + this.toTitleCase(status.name()));
        }
        lore.add(" ");
        lore.add("\u00a7eClick to change!");
        ItemStack settingItem = this.createItem(VersionHandler.getMaterial("ENDER_PEARL", "ENDER_PEARL"), "\u00a7aOnline Status", lore);
        ItemStack indicatorItem = this.createIndicatorDye((Object)currentStatus);
        this.setLore(indicatorItem, lore);
        String name = "\u00a7fOnline Status: " + indicatorItem.getItemMeta().getDisplayName();
        this.setDisplayName(indicatorItem, name);
        this.inventory.setItem(4, settingItem);
        this.inventory.setItem(13, indicatorItem);
    }

    private void addRequestPrivacySetting() {
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(this.playerUUID).getSettings();
        if (settings == null) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            Material settingMaterial = VersionHandler.getMaterial("JUKEBOX", "JUKEBOX");
            ItemStack settingItem = new ItemStack(settingMaterial);
            ItemMeta meta = settingItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Friend Request Privacy");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GRAY + "Change who can send you friend");
            lore.add(ChatColor.GRAY + "requests.");
            lore.add("");
            lore.add(ChatColor.GRAY + "Options:");
            PlayerSettings.RequestPrivacy current = settings.getRequestPrivacy();
            lore.add((current == PlayerSettings.RequestPrivacy.MAX ? ChatColor.RED + "\u00bb " : "  ") + ChatColor.RED + "Max " + ChatColor.GRAY + "(Staff)");
            lore.add((current == PlayerSettings.RequestPrivacy.HIGH ? ChatColor.GOLD + "\u00bb " : "  ") + ChatColor.GOLD + "High " + ChatColor.GRAY + "(Staff, Lobby)");
            lore.add((current == PlayerSettings.RequestPrivacy.NONE ? ChatColor.WHITE + "\u00bb " : "  ") + ChatColor.WHITE + "None " + ChatColor.GRAY + "(Anyone)");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to change!");
            meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes((char)'&', (String)l)).collect(Collectors.toList()));
            settingItem.setItemMeta(meta);
            ItemStack indicatorItem = this.createIndicatorDye((Object)current);
            ItemMeta indicatorMeta = indicatorItem.getItemMeta();
            indicatorMeta.setDisplayName(ChatColor.GREEN + "Friend Request Privacy");
            indicatorMeta.setLore(meta.getLore());
            indicatorItem.setItemMeta(indicatorMeta);
            this.inventory.setItem(30, settingItem);
            this.inventory.setItem(39, indicatorItem);
        });
    }

    private void addMessagePrivacySetting() {
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(this.playerUUID).getSettings();
        if (settings == null) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            Material settingMaterial = VersionHandler.getMaterial("SIGN", "SIGN");
            ItemStack settingItem = new ItemStack(settingMaterial);
            ItemMeta meta = settingItem.getItemMeta();
            if (meta == null) {
                Bukkit.getLogger().warning("[FriendSystem] Could not get ItemMeta for material: " + settingMaterial);
                return;
            }
            meta.setDisplayName(ChatColor.GREEN + "Private Message Privacy");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GRAY + "Change who can private message you.");
            lore.add("");
            lore.add(ChatColor.GRAY + "Options:");
            PlayerSettings.MessagePrivacy current = settings.getMessagePrivacy();
            lore.add((current == PlayerSettings.MessagePrivacy.MAX ? ChatColor.RED + "\u00bb " : "  ") + ChatColor.RED + "Max " + ChatColor.GRAY + "(Staff)");
            lore.add((current == PlayerSettings.MessagePrivacy.HIGH ? ChatColor.GOLD + "\u00bb " : "  ") + ChatColor.GOLD + "High " + ChatColor.GRAY + "(Staff, Best Friends)");
            lore.add((current == PlayerSettings.MessagePrivacy.MEDIUM ? ChatColor.YELLOW + "\u00bb " : "  ") + ChatColor.YELLOW + "Medium " + ChatColor.GRAY + "(Staff, Friends)");
            lore.add((current == PlayerSettings.MessagePrivacy.LOW ? ChatColor.GREEN + "\u00bb " : "  ") + ChatColor.GREEN + "Low " + ChatColor.GRAY + "(Lobby)");
            lore.add((current == PlayerSettings.MessagePrivacy.NONE ? ChatColor.WHITE + "\u00bb " : "  ") + ChatColor.WHITE + "None " + ChatColor.GRAY + "(Anyone)");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to change!");
            meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes((char)'&', (String)l)).collect(Collectors.toList()));
            settingItem.setItemMeta(meta);
            ItemStack indicatorItem = this.createIndicatorDye((Object)current);
            ItemMeta indicatorMeta = indicatorItem.getItemMeta();
            indicatorMeta.setDisplayName(ChatColor.GREEN + "Private Message Privacy");
            indicatorMeta.setLore(meta.getLore());
            indicatorItem.setItemMeta(indicatorMeta);
            this.inventory.setItem(31, settingItem);
            this.inventory.setItem(40, indicatorItem);
        });
    }

    private void addFriendNotificationsSetting() {
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(this.playerUUID).getSettings();
        int notifSetting = settings.getNotificationLevel();
        String[] names = new String[]{"All", "Best", "None"};
        ChatColor[] colors = new ChatColor[]{ChatColor.GOLD, ChatColor.GREEN, ChatColor.WHITE};
        String[] dyeNames = new String[]{"YELLOW_DYE", "LIME_DYE", "BONE_MEAL"};
        String[] loreOptions = new String[3];
        for (int i = 0; i < 3; ++i) {
            String arrow = notifSetting == i ? colors[i] + "\u00bb " : "  ";
            loreOptions[i] = arrow + colors[i] + names[i];
        }
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Receive messages when your fellow friends log in or out.");
        lore.add("");
        lore.add(ChatColor.GRAY + "Options:");
        lore.addAll(Arrays.asList(loreOptions));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to change!");
        ItemStack settingItem = this.createItem(Material.BOOK, ChatColor.GREEN + "Friend Notifications", lore);
        this.inventory.setItem(32, settingItem);
        ItemStack indicatorItem = new ItemStack(Material.valueOf((String)dyeNames[notifSetting]), 1);
        this.inventory.setItem(41, indicatorItem);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory() != this.inventory) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == 4 || slot == 13) {
            this.toggleOnlineStatus();
        } else if (slot == 30 || slot == 39) {
            this.toggleRequestPrivacy();
        } else if (slot == 31 || slot == 40) {
            this.toggleMessagePrivacy();
        } else if (slot == 32 || slot == 41) {
            this.toggleFriendNotifications();
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory() == this.inventory) {
            InventoryClickEvent.getHandlerList().unregister((Listener)this);
            InventoryCloseEvent.getHandlerList().unregister((Listener)this);
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void setLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void setDisplayName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    private ItemStack createIndicatorDye(Object setting) {
        if (VersionHandler.isLegacy()) {
            return new ItemStack(Material.valueOf((String)"INK_SACK"), 1, this.getIndicatorColor(setting));
        }
        String dyeName = "GRAY_DYE";
        if (setting instanceof PlayerSettings.OnlineStatus) {
            switch ((PlayerSettings.OnlineStatus)((Object)setting)) {
                case ONLINE: {
                    dyeName = "LIME_DYE";
                    break;
                }
                case AWAY: {
                    dyeName = "YELLOW_DYE";
                    break;
                }
                case BUSY: {
                    dyeName = "PURPLE_DYE";
                    break;
                }
                case APPEAR_OFFLINE: {
                    dyeName = "GRAY_DYE";
                }
            }
        } else if (setting instanceof PlayerSettings.RequestPrivacy) {
            switch ((PlayerSettings.RequestPrivacy)((Object)setting)) {
                case MAX: {
                    dyeName = "RED_DYE";
                    break;
                }
                case HIGH: {
                    dyeName = "ORANGE_DYE";
                    break;
                }
                case NONE: {
                    dyeName = "BONE_MEAL";
                }
            }
        } else if (setting instanceof PlayerSettings.MessagePrivacy) {
            switch ((PlayerSettings.MessagePrivacy)((Object)setting)) {
                case MAX: {
                    dyeName = "RED_DYE";
                    break;
                }
                case HIGH: {
                    dyeName = "ORANGE_DYE";
                    break;
                }
                case MEDIUM: {
                    dyeName = "YELLOW_DYE";
                    break;
                }
                case LOW: {
                    dyeName = "LIME_DYE";
                    break;
                }
                case NONE: {
                    dyeName = "BONE_MEAL";
                }
            }
        }
        return new ItemStack(Material.valueOf((String)dyeName), 1);
    }

    private short getIndicatorColor(Object setting) {
        if (setting instanceof PlayerSettings.OnlineStatus) {
            switch ((PlayerSettings.OnlineStatus)((Object)setting)) {
                case ONLINE: {
                    return 10;
                }
                case AWAY: {
                    return 11;
                }
                case BUSY: {
                    return 1;
                }
                case APPEAR_OFFLINE: {
                    return 8;
                }
            }
        } else if (setting instanceof PlayerSettings.RequestPrivacy) {
            switch ((PlayerSettings.RequestPrivacy)((Object)setting)) {
                case MAX: {
                    return 1;
                }
                case HIGH: {
                    return 14;
                }
                case NONE: {
                    return 10;
                }
            }
        } else if (setting instanceof PlayerSettings.MessagePrivacy) {
            switch ((PlayerSettings.MessagePrivacy)((Object)setting)) {
                case MAX: {
                    return 1;
                }
                case HIGH: {
                    return 14;
                }
                case MEDIUM: {
                    return 11;
                }
                case LOW: {
                    return 10;
                }
                case NONE: {
                    return 5;
                }
            }
        }
        return 8;
    }

    private String toTitleCase(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase().replace("_", " ");
    }

    private void toggleOnlineStatus() {
        PlayerData data = this.plugin.getDataManager().getPlayerData(this.playerUUID);
        PlayerSettings settings = data.getSettings();
        PlayerSettings.OnlineStatus current = settings.getOnlineStatus();
        PlayerSettings.OnlineStatus next = this.getNextStatus(current);
        settings.setOnlineStatus(next);
        if (next == PlayerSettings.OnlineStatus.APPEAR_OFFLINE) {
            data.setAppearOfflineSince(System.currentTimeMillis());
            ActionBarUtil.sendActionBar(this.player, "\u00a7fYou are currently \u00a7cAPPEARING OFFLINE");
        } else if (next == PlayerSettings.OnlineStatus.AWAY) {
            ActionBarUtil.sendActionBar(this.player, "\u00a7fYou are currently \u00a7cAWAY");
        } else if (next == PlayerSettings.OnlineStatus.BUSY) {
            ActionBarUtil.sendActionBar(this.player, "\u00a7fYou are currently \u00a7cBUSY");
        } else if (current == PlayerSettings.OnlineStatus.APPEAR_OFFLINE) {
            data.setAppearOfflineSince(0L);
        }
        this.plugin.getDataManager().savePlayerData(data);
        this.player.playSound(this.player.getLocation(), VersionHandler.getSound("UI_BUTTON_CLICK", "CLICK"), 1.0f, 1.0f);
        this.updateItems();
    }

    private PlayerSettings.OnlineStatus getNextStatus(PlayerSettings.OnlineStatus current) {
        PlayerSettings.OnlineStatus[] statuses = PlayerSettings.OnlineStatus.values();
        int nextOrdinal = (current.ordinal() + 1) % statuses.length;
        return statuses[nextOrdinal];
    }

    private void toggleRequestPrivacy() {
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(this.playerUUID).getSettings();
        if (settings == null) {
            return;
        }
        PlayerSettings.RequestPrivacy current = settings.getRequestPrivacy();
        PlayerSettings.RequestPrivacy next = PlayerSettings.RequestPrivacy.values()[(current.ordinal() + 1) % PlayerSettings.RequestPrivacy.values().length];
        settings.setRequestPrivacy(next);
        this.player.playSound(this.player.getLocation(), VersionHandler.getSound("UI_BUTTON_CLICK", "CLICK"), 1.0f, 1.0f);
        this.updateItems();
    }

    private void toggleMessagePrivacy() {
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(this.playerUUID).getSettings();
        if (settings == null) {
            return;
        }
        PlayerSettings.MessagePrivacy current = settings.getMessagePrivacy();
        PlayerSettings.MessagePrivacy next = PlayerSettings.MessagePrivacy.values()[(current.ordinal() + 1) % PlayerSettings.MessagePrivacy.values().length];
        settings.setMessagePrivacy(next);
        this.player.playSound(this.player.getLocation(), VersionHandler.getSound("UI_BUTTON_CLICK", "CLICK"), 1.0f, 1.0f);
        this.updateItems();
    }

    private void toggleFriendNotifications() {
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(this.playerUUID).getSettings();
        int notifSetting = settings.getNotificationLevel();
        notifSetting = (notifSetting + 1) % 3;
        settings.setNotificationLevel(notifSetting);
        this.plugin.getDataManager().savePlayerData(this.plugin.getDataManager().getPlayerData(this.playerUUID));
        this.player.playSound(this.player.getLocation(), VersionHandler.getSound("UI_BUTTON_CLICK", "CLICK"), 1.0f, 1.0f);
        this.updateItems();
    }
}

