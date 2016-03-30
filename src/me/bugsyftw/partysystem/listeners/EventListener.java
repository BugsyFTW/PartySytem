package me.bugsyftw.partysystem.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.bugsyftw.partysystem.PartyGUI;
import me.bugsyftw.partysystem.PartySystem;
import me.bugsyftw.partysystem.PickedItem;
import me.bugsyftw.partysystem.party.Party;

public class EventListener implements Listener {

	private Plugin plugin;
	private List<String> teleporting = new ArrayList<String>();

	public EventListener(Plugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onChatInteract(AsyncPlayerChatEvent e) {
		String[] message = e.getMessage().split(" ");
		if (message.length == 2) {
			Player p = e.getPlayer();
			if (getPartySystem().getManager().isInvited(p.getName())) {
				String[] msg = e.getMessage().split(" ");
				for (String pts : getPartySystem().getManager().getInviteList(p.getName())) {
					if (pts.equalsIgnoreCase(msg[1])) {
						Party pt = getPartySystem().getManager().getParty(pts);
						if (msg[1].equalsIgnoreCase(pt.getLeader().getName())) {
							if (msg[0].equalsIgnoreCase("accept") && msg[1].equalsIgnoreCase(pt.getLeader().getName())) {
								getPartySystem().getManager().removeInvite(p.getName(), pt);
								pt.addPlayer(p, true);
								pt.update();
								pt.sendMessage(ChatColor.GREEN + p.getName() + " has joined the Party!");
								e.setCancelled(true);
								break;
							} else if (msg[0].equalsIgnoreCase("decline") && msg[1].equalsIgnoreCase(pt.getLeader().getName())) {
								getPartySystem().getManager().removeInvite(p.getName(), pt);
								p.sendMessage(ChatColor.RED + "[Party]" + ChatColor.GRAY + " You have refused " + pt.getLeader().getName() + "'s Party invitation!");
								e.setCancelled(true);
								if (pt.getMembers().size() == 1) {
									Player oP = Bukkit.getServer().getPlayer(UUID.fromString(pt.getMember(pt.getSize() - 1)));
									oP.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
									oP.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GRAY + " Was disolved because of no members!");
									Party.disolve(pt);
								}
								break;
							}
						}
						break;
					}
				}

			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		for (Party party : getPartySystem().getManager().getParties()) {
			if (party.getMember(p.getUniqueId().toString()) != null) {
				if (e.getInventory().getTitle().equals(party.getLeader().getName() + "'s Party!") && e.getInventory().getSize() == 9) {
					ItemStack item = e.getCurrentItem();
					if (item == null)
						return;
					if (item.getType().equals(Material.SKULL_ITEM) && item.hasItemMeta()) {
						if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
							SkullMeta meta = (SkullMeta) item.getItemMeta();
							Player tp = Bukkit.getServer().getPlayer(meta.getOwner());
							if (party.getLeader().getName().equals(p.getName()) && (party.getPrivileges().contains(tp.getName()) == false)) {
								if (tp == p) {
									e.setCancelled(true);
									p.closeInventory();
									return;
								}
								party.getPrivileges().add(tp.getName());
								tp.sendMessage(PartySystem.PARTY + "You have been given privileges!");
								p.sendMessage(PartySystem.PARTY + "You have given privileges to " + tp.getName());
								e.setCancelled(true);
								p.closeInventory();
								return;
							} else if (party.getLeader().getName().equals(p.getName()) && party.getPrivileges().contains(tp.getName())) {
								if (tp == p) {
									e.setCancelled(true);
									p.closeInventory();
									return;
								}
								party.getPrivileges().remove(tp.getName());
								tp.sendMessage(PartySystem.PARTY + "You have been removed privileges");
								p.sendMessage(PartySystem.PARTY + "You have removed privileges to " + tp.getName());
								e.setCancelled(true);
								p.closeInventory();
								return;
							}
						} else if (e.getClick().equals(ClickType.LEFT) || e.getClick().equals(ClickType.RIGHT)) {
							if (party.getPrivileges().contains(p.getName())) {
								SkullMeta meta = (SkullMeta) item.getItemMeta();
								Player tp = Bukkit.getServer().getPlayer(meta.getOwner());
								if (tp == p) {
									e.setCancelled(true);
									return;
								}
								if (party.getMember(tp.getUniqueId().toString()) != null) {
									if (getPlugin().getConfig().getBoolean("Party.Teleportion.Enable")) {
										p.sendMessage(PartySystem.PARTY + ChatColor.GREEN + "Teleporting you to " + ChatColor.UNDERLINE + tp.getName() + ChatColor.GREEN + "...");
										p.sendMessage(PartySystem.PARTY + "Teleportion in 3 seconds...");
										teleporting.add(p.getName());
										e.setCancelled(true);
										p.closeInventory();
										new BukkitRunnable() {
											int time = 12;

											@Override
											public void run() {
												if (time != 0 || time != -1) {
													p.getLocation().getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 5);
													p.getLocation().getWorld().playSound(p.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 5, 5);
													time--;
												}
												if (time == 12) {
													p.getLocation().getWorld().playSound(p.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 5, 5);
												}
												if (time == 0) {
													p.teleport(tp);
													teleporting.remove(p.getName());
													p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 5, 5);
													time--;
												}
												if (time == -1) {
													cancel();
												}
											}
										}.runTaskTimer(getPlugin(), 0L, 5L);
									}
								}
								e.setCancelled(true);
							}
						}
					} else if (item.equals(PartyGUI.PartyChest(p, party, party.isAvailability()))) {
						if (e.getClick().equals(ClickType.LEFT) || e.getClick().equals(ClickType.RIGHT)) {
							if (party.isAvailability()) {
								PartyGUI gui = new PartyGUI(getPlugin(), p, party);
								gui.presentChest();
							}
						} else if (e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)) {
							if (party.getPrivileges().contains(p.getName())) {
								if (party.isAvailability()) {
									party.setAvailability(false);
									e.getInventory().setItem(8, PartyGUI.PartyChest(p, party, party.isAvailability()));
									party.sendMessage(PartySystem.PARTY + "Chest Disabled!");
									for (String mem : party.getMembers()) {
										Player pp = Bukkit.getServer().getPlayer(UUID.fromString(mem));
										if (pp.getOpenInventory().getTitle().equals(party.getLeader().getName() + "'s Party!")) {
											pp.getInventory().setItem(8, PartyGUI.PartyChest(pp, party, party.isAvailability()));
										}
									}
								} else {
									party.setAvailability(true);
									e.getInventory().setItem(8, PartyGUI.PartyChest(p, party, party.isAvailability()));
									party.sendMessage(PartySystem.PARTY + "Chest Enabled!");
									for (String mem : party.getMembers()) {
										Player pp = Bukkit.getServer().getPlayer(UUID.fromString(mem));
										if (pp.getOpenInventory().getTitle().equals(party.getLeader().getName() + "'s Party!")) {
											pp.getOpenInventory().setItem(8, PartyGUI.PartyChest(pp, party, party.isAvailability()));
										}
									}
								}
							}
						}
					}
					e.setCancelled(true);
				} else if (e.getInventory().getTitle().equals(party.getLeader().getName() + "'s Party's Chest!")) {
					if (e.getRawSlot() == e.getSlot()) {
						if (e.getAction().equals(InventoryAction.PICKUP_ALL) || e.getAction().equals(InventoryAction.PICKUP_SOME) || e.getAction().equals(InventoryAction.PICKUP_HALF) || e.getAction().equals(InventoryAction.PICKUP_ONE)) {
							ItemStack item = e.getCurrentItem();
							if (item.getType().equals(Material.AIR)) {
								return;
							}
							if (party.getManager().getItemsMap().containsKey(party.getLeader().getName())) {
								PickedItem items = party.getManager().getItemsMap().get(party.getLeader().getName());
								if (items.contains(item)) {
									items.removeItem(item);
									for (String mem : party.getMembers()) {
										Player pp = Bukkit.getServer().getPlayer(UUID.fromString(mem));
										if (pp.getOpenInventory().getTitle().equals(party.getLeader().getName() + "'s Party's Chest!") && (pp != p)) {
											pp.getOpenInventory().getTopInventory().remove(item);
										}
									}
								}
							}
						} else if (e.getAction().equals(InventoryAction.PLACE_ALL) || e.getAction().equals(InventoryAction.PLACE_SOME) || e.getAction().equals(InventoryAction.PLACE_ONE)) {
							ItemStack item = new ItemStack(e.getCursor().getType(), e.getCursor().getAmount());
							item.setItemMeta(e.getCursor().getItemMeta());
							if (party.getManager().getItemsMap().containsKey(party.getLeader().getName())) {
								PickedItem items = party.getManager().getItemsMap().get(party.getLeader().getName());
								items.addItem(item);
								for (String mem : party.getMembers()) {
									Player pp = Bukkit.getServer().getPlayer(UUID.fromString(mem));
									if (pp.getOpenInventory().getTitle().equals(party.getLeader().getName() + "'s Party's Chest!") && (pp != p)) {
										pp.getOpenInventory().getTopInventory().addItem(item);
									}
								}
							} else {
								PickedItem items = new PickedItem(p.getName(), item);
								party.getManager().getItemsMap().put(party.getLeader().getName(), items);
								for (String mem : party.getMembers()) {
									Player pp = Bukkit.getServer().getPlayer(UUID.fromString(mem));
									if (pp.getOpenInventory().getTitle().equals(party.getLeader().getName() + "'s Party's Chest!") && (pp != p)) {
										pp.getOpenInventory().getTopInventory().addItem(item);
									}
								}
							}
						}
					} else if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || e.getAction().equals(InventoryAction.COLLECT_TO_CURSOR) || e.getAction().equals(InventoryAction.CLONE_STACK)) {
						e.setCancelled(true);
						e.setResult(Result.DENY);
						p.sendMessage(PartySystem.PARTY + "Can't use this actions while in Inventory.");
						p.closeInventory();
					}
				}
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (getPlugin().getConfig().getBoolean("Party.Teleportion.Lock-Moviment")) {
			Player p = e.getPlayer();
			Party party = getPartySystem().getManager().getParty(p.getUniqueId());
			if (party == null)
				return;
			if (teleporting.contains(p.getName())) {
				e.setCancelled(true);
			}
		}
	}

	public PartySystem getPartySystem() {
		return PartySystem.getInstance();
	}

	public Plugin getPlugin() {
		return plugin;
	}

}
