package me.bugsyftw.partysystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import me.bugsyftw.partysystem.party.Party;

public class PartyGUI {

	private Plugin plugin;
	private Inventory inventory;
	private Player player;
	private Party party;

	public PartyGUI(Plugin plugin, Player player, Party party) {
		this.plugin = plugin;
		this.player = player;
		this.party = party;
	}

	public void present(boolean promote) {
		inventory = Bukkit.getServer().createInventory(player, 9, party.getLeader().getName() + "'s Party!");
		for (int i = 0; i < party.getSize(); i++) {
			String uuid = party.getMember(i);
			Player p = Bukkit.getServer().getPlayer(UUID.fromString(uuid));
			ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Distance: " + ChatColor.UNDERLINE + Math.round(p.getLocation().distance(player.getLocation())) + " blocks away!");
			meta.setOwner(p.getName());
			if (party.getPrivileges().contains(player.getName()) && p != player && getPlugin().getConfig().getBoolean("Party.Teleportion.Enable")) {
				lore.add("" + ChatColor.GREEN + ChatColor.BOLD + "Click to Teleport to player!");
			}
			if (promote && p != player && (party.getPrivileges().contains(p.getName()) == false && party.getLeader().getName().equals(player.getName()))) {
				lore.add("" + ChatColor.RED + ChatColor.BOLD + "Shift-Right-Click to grant privileges!");
			}
			if (party.getLeader().getName().equals(player.getName()) && p != player && promote && party.getPrivileges().contains(p.getName())) {
				lore.add("" + ChatColor.RED + ChatColor.BOLD + "Shift-Right-Click to remove privileges");
			}
				
			if (party.getLeader().getUniqueId().equals(p.getUniqueId())) {
				meta.setDisplayName("" + ChatColor.WHITE + ChatColor.BOLD + p.getName());
			} else {
				meta.setDisplayName(ChatColor.WHITE + p.getName());
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			getInventory().setItem(i, item);
		}
		getInventory().setItem(8, PartyChest(player, party, party.isAvailability()));
		player.openInventory(getInventory());
	}

	public void presentChest() {
		Inventory inv = Bukkit.getServer().createInventory(player, (9 * 5), party.getLeader().getName() + "'s Party's Chest!");
		for (Entry<String, PickedItem> item : PartySystem.getInstance().getManager().getItemsMap().entrySet()) {
			if (item.getKey().equals(party.getLeader().getName())) {
				List<ItemStack> it = item.getValue().getItems();
				for (int i = 0; i < it.size(); i++) {
					inv.setItem(i, it.get(i));
				}
			}
		}
		player.openInventory(inv);
	}

	public static ItemStack PartyChest(Player p, Party party, boolean availability) {
		ItemStack item = new ItemStack(Material.CHEST);
		ItemMeta meta = item.getItemMeta();
		if (availability)
			meta.setDisplayName(ChatColor.RED + "[Items] " + ChatColor.GREEN + ChatColor.BOLD + "- ENABLED");
		else
			meta.setDisplayName(ChatColor.RED + "[Items] " + ChatColor.RED + ChatColor.BOLD + "- DISABLED");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GREEN + "- " + ChatColor.GRAY + "Items found by party will");
		lore.add(ChatColor.GRAY + "    be saved here until withdraw.");
		if (party.getPrivileges().contains(p.getName())) {
			lore.add(ChatColor.GREEN + "- " + ChatColor.RED + ChatColor.BOLD + "Shift-Click to enable/disable");
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public Party getParty() {
		return party;
	}

	public Player getPlayer() {
		return player;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Plugin getPlugin() {
		return plugin;
	}
}
