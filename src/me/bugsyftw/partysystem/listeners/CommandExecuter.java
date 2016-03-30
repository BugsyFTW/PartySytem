package me.bugsyftw.partysystem.listeners;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.bugsyftw.partysystem.PartyGUI;
import me.bugsyftw.partysystem.PartySystem;
import me.bugsyftw.partysystem.party.Party;
import me.bugsyftw.partysystem.party.PartyManager;
import me.bugsyftw.partysystem.party.PartyPermissions;

public class CommandExecuter implements CommandExecutor {

	private Plugin plugin;

	public CommandExecuter(Plugin plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("party")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (args.length == 0) {
					Party party = getPartySystem().getManager().getParty(p.getUniqueId());
					if (party != null) {
						PartyGUI gui = new PartyGUI(getPlugin(), p, party);
						if (party.getPrivileges().contains(p.getName())) {
							gui.present(true);
							return true;
						} else {
							gui.present(false);
							return true;
						}
					}
					p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Wrong Syntax: " + ChatColor.RED + "/party invite/leave/kick/disband");
					return false;
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("leave")) {
						Party party = getPartySystem().getManager().getParty(p.getUniqueId());
						if (party == null)
							return false;
						party.removePlayer(p);
						return false;
					} else if (args[0].equalsIgnoreCase("roll")) {
						Party party = getPartySystem().getManager().getParty(p.getUniqueId());
						if (party == null)
							return false;
						int r = new Random().nextInt(100);
						party.sendMessage(PartySystem.PARTY + p.getName() + " has rolled " + "'" + ChatColor.BOLD + r + ChatColor.GRAY + "' of 100!");
						return true;
					}
				}
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("invite")) {
						Player player = Bukkit.getServer().getPlayer(args[1]);
						Party party = getPartySystem().getManager().getParty(p.getUniqueId());
						if (player.isOnline() && player != p && player != null) {
							Party o_party = getPartySystem().getManager().getParty(player.getUniqueId());
							if (o_party != null) {
								p.sendMessage(PartySystem.PARTY + "The player you have invited is already in a party");
								return true;
							}
							if (party != null) {
								if (party.getMembers().size() < Party.MAX_PLAYERS) {
									invite(party, p, player);
									return true;

								} else {
									p.sendMessage(PartySystem.PARTY + "The party is full!");
									return true;
								}
							} else {
								if (p.hasPermission(PartyPermissions.CREATE.getPermission())) {
									party = new Party(p);
									invite(party, p, player);
									return true;
								} else {
									p.sendMessage(PartySystem.PARTY + "You do not have permission to create a new party!");
									return false;
								}
							}
						} else {
							p.sendMessage(ChatColor.RED + "The player you have invited was not found!");
							return false;
						}
					} else if (args[0].equalsIgnoreCase("kick")) {
						Player player = Bukkit.getServer().getPlayer(args[1]);
						Party party = getPartySystem().getManager().getParty(p.getUniqueId());
						if (player.isOnline() && player != null) {
							if (party != null) {
								if (party.getPrivileges().contains(p.getName())) {
									party.removePlayer(player);
									player.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
									player.sendMessage(ChatColor.YELLOW + "You have been kicked from " + party.getLeader().getName() + "'s Party!");
									party.sendMessage(PartySystem.PARTY + player.getName() + " was kicked from the Party!");
									party.update();
									if (party.getSize() == 1) {
										party.sendMessage(ChatColor.RED + "Party ended for lack of members!");
										for (String s : party.getMembers()) {
											Player pl = Bukkit.getServer().getPlayer(UUID.fromString(s));
											pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
										}
										Party.disolve(party);
										return false;
									}
								} else {
									p.sendMessage(PartySystem.PARTY + "You don't have permission to kick players!");
									return true;
								}
							}
						} else {
							p.sendMessage(PartySystem.PARTY + "No player found!");
							return true;
						}
					} else if (args[0].equalsIgnoreCase("leader")) {
						Party party = getPartySystem().getManager().getParty(p.getUniqueId());
						if (party == null)
							return false;
						Player nl = Bukkit.getServer().getPlayer(args[1]);
						if (nl.isOnline() && nl != null) {
							if (party.containsMember(nl.getUniqueId().toString()) && party.getLeader().getName().equals(p.getName())) {
								party.setLeader(nl);
								party.update();
								party.sendMessage(PartySystem.PARTY + "Party Leader changed to: " + nl.getName());
							} else {
								p.sendMessage(PartySystem.PARTY + "Select a player from the party");
							}
						} else {
							p.sendMessage(PartySystem.PARTY + "Player not found!");
						}
					}
				}
			}
		} else if (commandLabel.equalsIgnoreCase("p")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				Party party = getPartySystem().getManager().getParty(p.getUniqueId());
				if (party == null) {
					return false;
				}
				String msg = "";
				for (String a : args) {
					msg += a + " ";
				}
				party.sendMessage(ChatColor.RED + "[" + p.getName() + "]: " + ChatColor.GRAY + msg);
			}
		}
		return false;
	}

	public void invite(Party party, Player p, Player player) {
		if (party.getMember(player.getUniqueId().toString()) == null) {
			if (party.getPrivileges().contains(p.getName())) {
				if (isNearby(p, player)) {
					if (party.getManager().isInvited(player.getName(), party) == false) {
						player.sendMessage(ChatColor.YELLOW + "You have been invited to " + ChatColor.UNDERLINE + party.getLeader().getName() + ChatColor.YELLOW + "'s Party!");
						player.sendMessage(ChatColor.GRAY + "Invitation expires in " + getPlugin().getConfig().getInt("Party.Invitation-Time") + " seconds!");
						player.sendMessage(ChatColor.GREEN + "Type " + ChatColor.BOLD + "'accept' <name>" + ChatColor.YELLOW + " OR " + ChatColor.RED + ChatColor.BOLD + "'decline' <name>" + ChatColor.GREEN + " the party invitation!");
						p.sendMessage(ChatColor.YELLOW + "You have invited " + player.getName() + " to your party!");
						getPartySystem().getManager().addInvite(player.getName(), party);
						new BukkitRunnable() {
							@Override
							public void run() {
								PartyManager manager = getPartySystem().getManager();
								if (manager.isInvited(player.getName(), party)) {
									manager.removeInvite(player.getName(), party);
									player.sendMessage(PartySystem.PARTY + "Your invitation has expired!");
									if (party.getMembers().size() == 1) {
										Party.disolve(party);
										p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
										p.sendMessage(PartySystem.PARTY + "Has been disolved because you have no members!");
									}
								}
							}
						}.runTaskLater(getPlugin(), (20L * getPlugin().getConfig().getInt("Party.Invitation-Time")));
					} else {
						p.sendMessage(PartySystem.PARTY + "The player is already invited!");
					}
				} else {
					p.sendMessage(PartySystem.PARTY + "The player you invited needs to be in a " + (int)getPlugin().getConfig().getDouble("Party.Invite.Radious") +" block radious from you to be invited!");
				}
			} else {
				p.sendMessage(PartySystem.PARTY + "You do not have permission to invite players to a party!");
			}
		} else {
			p.sendMessage(PartySystem.PARTY + "Player is already in your party!");
			return;
		}
	}

	@SuppressWarnings("deprecation")
	public boolean isNearby(Player p, Player target) {
		if (getPlugin().getConfig().getBoolean("Party.Invite.Nearby") == false) {
			return true;
		}
		double r = getPlugin().getConfig().getDouble("Party.Invite.Radious");
		for (Entity e : p.getNearbyEntities(r, r, r)) {
			if (e instanceof Player) {
				Player t = Bukkit.getServer().getPlayer(e.getName());
				if (t.getName().equals(target.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public PartySystem getPartySystem() {
		return PartySystem.getInstance();
	}

	public Plugin getPlugin() {
		return plugin;
	}
}
