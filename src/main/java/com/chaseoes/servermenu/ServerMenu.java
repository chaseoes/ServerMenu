package com.chaseoes.servermenu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
				warpToServer(event.getPlayer(), getConfig().getString("servers." + (event.getPosition() + 1) + ".server"));
				event.setWillClose(true);
			}
		}, this);
		for (String s : getConfig().getConfigurationSection("servers").getKeys(false)) {
			int i = Integer.parseInt(s);
			String[] itemParts = getConfig().getString("servers." + i + ".item").split(":");
			ItemStack item = new ItemStack(Material.getMaterial(itemParts[0]), 1);
			if (itemParts.length == 2) {
				item.setDurability(Short.parseShort(itemParts[1]));
			}
			menu.setOption(i - 1, item, translate(getConfig().getString("servers." + i + ".name")), translate(getConfig().getString("servers." + i + ".description")));
		}
	}

	public void onDisable() {
		reloadConfig();
		saveConfig();
		menu = null;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getType() == getItem().getType()) {
			if (event.hasBlock() && (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN || event.getClickedBlock().getType() == Material.JUKEBOX || event.getClickedBlock().getState() instanceof InventoryHolder)) {
				return;
			}
			menu.open(event.getPlayer());
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (event.getItemDrop().getItemStack().getType() == getItem().getType()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (!event.getPlayer().getInventory().contains(getItem().getType())) {
			event.getPlayer().getInventory().addItem(getItem());
		}
		if (!event.getPlayer().getInventory().contains(Material.WRITTEN_BOOK)) {
			giveWrittenBooks(event.getPlayer());
		}
	}

	public ItemStack getItem() {
		ItemStack i = new ItemStack(Material.getMaterial(getConfig().getString("item")), 1);
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(translate(getConfig().getString("item-name")));
		i.setItemMeta(im);
		return i;
	}

	private void warpToServer(Player player, String server) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(server);
		player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
	}

	private String translate(String s) {
		return ChatColor.translateAlternateColorCodes('&', ChatColor.stripColor(s));
	}

	int roundUp(int n) {
		return (n + 8) / 9 * 9;
	}

	public void giveWrittenBooks(final Player player) {
		for (String file : getConfig().getStringList("books")) {
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMeta bm = (BookMeta) book.getItemMeta();
			File f = new File(getDataFolder() + "/" + file);
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				int i = 0;

				while (line != null) {
					i++;

					if (i != 1 && i != 2) {
						if (line.equalsIgnoreCase("/n")) {
							bm.addPage(sb.toString());
							sb = new StringBuilder();
						} else {
							sb.append(translate(line));
							sb.append("\n");
						}
					} else {
						if (i == 1) {
							bm.setTitle(translate(line));
						}
						if (i == 2) {
							bm.setAuthor(translate(line));
						}
					}
					line = br.readLine();
				}

				br.close();
				bm.addPage(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
				getLogger().log(Level.WARNING, "Error encountered while trying to give a written book! (File " + file + ")");
				getLogger().log(Level.WARNING, "Please check that the file exists and is readable.");
			}

			book.setItemMeta(bm);
			player.getInventory().addItem(book);
		}
	}

}
