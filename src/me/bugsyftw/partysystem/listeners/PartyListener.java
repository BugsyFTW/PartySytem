package me.bugsyftw.partysystem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import me.bugsyftw.partysystem.PartySystem;
import me.bugsyftw.partysystem.PickedItem;
import me.bugsyftw.partysystem.party.Party;

public class PartyListener implements Listener {

	private Plugin plugin;

	public PartyListener(Plugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Party party = getPartySystem().getManager().getParty(p.getUniqueId());
			if (party != null) {
				party.update();
			}
		}
	}

	@EventHandler
	public void onMobDeath(EntityDeathEvent e) {
		if (e.getEntity().getKiller() instanceof Player) {
			Player killer = e.getEntity().getKiller();
			Party party = getPartySystem().getManager().getParty(killer.getUniqueId());
			if (party == null)
				return;
			if (party.isAvailability()) {
				for (ItemStack i : e.getDrops()) {
					if (i.hasItemMeta()) {
						if (party.getManager().getItemsMap().containsKey(party.getLeader().getName())) {
							PickedItem pt = party.getManager().getItemsMap().get(party.getLeader().getName());
							if (pt.getItems().size() >= 45) {
								killer.sendMessage(PartySystem.PARTY + "Party Chest is full!");
								return;
							}
							pt.addItem(i);
							killer.sendMessage(PartySystem.PARTY + "Added Item to Party Chest!");
							e.getDrops().remove(i);
						} else {
							PickedItem pt = new PickedItem(killer.getName(), i);
							party.getManager().getItemsMap().put(party.getLeader().getName(), pt);
							killer.sendMessage(PartySystem.PARTY + "Added Item to Party Chest!");
							e.getDrops().remove(i);
						}
					}
				}
			}
		}
	}

	/*@EventHandler
	public void onItemPickUp(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		if (e.getItem().getItemStack().hasItemMeta()) {
			Party party = getPartySystem().getManager().getParty(p.getUniqueId());
			if (party == null)
				return;
			if (getPlugin().getConfig().getBoolean("Party.ItemPickup")) {
				if (party.isAvailability()) {
					ItemMeta meta = e.getItem().getItemStack().getItemMeta();
					if (meta.getDisplayName().contains(",")) {
						if (meta.getDisplayName().split(",").length == 2) {
							String[] n_na = meta.getDisplayName().split(",");
							ItemStack new_item = new ItemStack(e.getItem().getItemStack().getType(), e.getItem().getItemStack().getAmount());
							ItemMeta new_meta = new_item.getItemMeta();
							new_meta.setDisplayName(n_na[0]);
							if (meta.hasEnchants()) {
								for (Entry<Enchantment, Integer> en : meta.getEnchants().entrySet()) {
									new_meta.addEnchant(en.getKey(), en.getValue(), true);
								}
							}
							new_item.setItemMeta(new_meta);
							if (party.getLeader().getName().equalsIgnoreCase(n_na[1])) {
								if (party.getManager().getItemsMap().containsKey(party.getLeader().getName())) {
									PickedItem pt = party.getManager().getItemsMap().get(party.getLeader().getName());
									pt.addItem(new_item);
								} else {
									PickedItem pt = new PickedItem(p.getName(), new_item);
									party.getManager().getItemsMap().put(party.getLeader().getName(), pt);
								}
								e.getItem().remove();
								e.setCancelled(true);
							}
						}
					}
				}
			}
		}
	}*/

	@EventHandler
	public void onPlayerRegen(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Party party = getPartySystem().getManager().getParty(p.getUniqueId());
			if (party != null)
				party.update();
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Party party = getPartySystem().getManager().getParty(p.getUniqueId());
			if (party == null)
				return;
			party.sendMessage(PartySystem.PARTY + p.getName() + " has died!");

		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		Party party = getPartySystem().getManager().getParty(p.getUniqueId());
		if (party == null)
			return;
		party.update();
	}

	public PartySystem getPartySystem() {
		return PartySystem.getInstance();
	}

	public Plugin getPlugin() {
		return plugin;
	}
}
