package com.chaseoes.servermenu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class ServerMenu extends JavaPlugin implements Listener {
	
	IconMenu menu;
	
	public void onEnable() {
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		menu = new IconMenu(ChatColor.translateAlternateColorCodes('&', getConfig().getString("title")), roundUp(getConfig().getConfigurationSection("servers").getKeys(false).size()), new IconMenu.OptionClickEventHandler() {
            @Override
            public void onOptionClick(IconMenu.OptionClickEvent event) {
                warpToServer(event.getPlayer(), getConfig().getString("servers." + event.getPosition() + ".server"));
                event.setWillClose(true);
            }
        }, this);
		for (String s : getConfig().getConfigurationSection("servers").getKeys(false)) {
			int i = Integer.parseInt(s);
			menu.setOption(i - 1, new ItemStack(Material.getMaterial(getConfig().getString("servers." + i + ".item")), 1), translate(getConfig().getString("servers." + i + ".name")), translate(getConfig().getString("servers." + i + ".description")));
		}
	}
	
	public void onDisable() {
		reloadConfig();
		saveConfig();
		menu = null;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getType() == Material.getMaterial(getConfig().getString("item"))) {
			menu.open(event.getPlayer());
		}
	}
	
	private void warpToServer(Player player, String server) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("Connect");
	    out.writeUTF(server);
	    player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
	}
	
	private String translate(String s) {
		return ChatColor.translateAlternateColorCodes('&', ChatColor.RESET + ChatColor.stripColor(s));
	}
	
	int roundUp(int n) {
	    return (n + 8) / 9 * 9;
	}

}
