package me.bugsyftw.partysystem.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import me.bugsyftw.partysystem.PartySystem;
import me.bugsyftw.partysystem.party.Party;
import me.bugsyftw.partysystem.party.PartyManager;

public class ConnectionListener implements Listener {

	private Plugin plugin;

	public ConnectionListener(Plugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Party party = getManager().getParty(p.getUniqueId());
		if (party == null)
			return;
		if (party.getSize() <= 2) {
			party.removePlayer(p);
			party.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GRAY + "Ended for lack of members!");
			for (String s : party.getMembers()) {
				Player pl = Bukkit.getServer().getPlayer(UUID.fromString(s));
				pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
			Party.disolve(party);
		} else if (party.getLeader() == p) {
			party.removePlayer(p);
			Player oP = Bukkit.getServer().getPlayer(UUID.fromString(party.getMember(party.getSize() - 1)));
			party.setLeader(oP);
			party.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GRAY + "Party leader has left. New Leader is: " + ChatColor.UNDERLINE + oP.getName());

		}
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		Player p = e.getPlayer();
		Party party = getManager().getParty(p.getUniqueId());
		if (party == null)
			return;
		if (party.getSize() <= 2) {
			party.removePlayer(p);
			party.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GRAY + "Ended for lack of members!");
			for (String s : party.getMembers()) {
				Player pl = Bukkit.getServer().getPlayer(UUID.fromString(s));
				pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
			Party.disolve(party);
		} else if (party.getLeader() == p) {
			party.removePlayer(p);
			Player oP = Bukkit.getServer().getPlayer(UUID.fromString(party.getMember(party.getSize() - 1)));
			party.setLeader(oP);
			party.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GRAY + "Party leader has left. New Leader is: " + ChatColor.UNDERLINE + oP.getName());

		}
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public PartyManager getManager() {
		return PartySystem.getInstance().getManager();
	}
}
