package me.bugsyftw.partysystem.party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.bugsyftw.partysystem.PartySystem;

public class Party {

	public static final int MAX_PLAYERS = 8;
	private List<String> privileges = new ArrayList<String>();
	private List<String> members = new ArrayList<String>();// UUID's Strings
	private Player leader;
	private Scoreboard board;
	private Team team;
	private Objective objective;
	private Inventory party_inv;
	private boolean availability;

	public Party(Player leader) {
		this.leader = leader;
		privileges.add(leader.getName());
		getManager().addParty(this);
		setAvailability(true);
		board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		team = board.registerNewTeam(leader.getName());
		team.setAllowFriendlyFire(PartySystem.getInstance().getConfig().getBoolean("Party-PVP"));
		team.setPrefix(ChatColor.translateAlternateColorCodes('&', PartySystem.getInstance().getConfig().getString("Party.Prefix")));
		objective = board.registerNewObjective("party_health", "dummy");
		addPlayer(leader, true);
	}

	public List<String> getMembers() {
		return members;
	}

	public void addPlayer(Player p, boolean display) {
		members.add(p.getUniqueId().toString());
		team.addEntry(p.getName());
		if (display) {
			displayBoard(p);
		}

	}

	@SuppressWarnings("deprecation")
	public void removePlayer(Player p) {
		if (p.getName().equals(leader.getName())) {
			getPrivileges().remove(p.getName());
			members.remove(p.getUniqueId().toString());
			team.removeEntry(p.getName());
			p.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
			p.sendMessage(ChatColor.YELLOW + "You have left the Party!");
			if (checkSize()) {
				Party.disolve(this);
				return;
			}
			if (getPrivileges().size() > 0) {
				for (String s : getPrivileges()) {
					Player nl = Bukkit.getServer().getPlayer(s); 
					leader = nl;
					if (!getPrivileges().contains(nl.getName()))
						getPrivileges().add(nl.getName());
					sendMessage(PartySystem.PARTY + "Leader just left, new Leader is : " + ChatColor.GREEN + leader.getName());
					leader.sendMessage(ChatColor.YELLOW + "You are the new party leader!");
					board.resetScores(ChatColor.WHITE + p.getName());
					update();
					if (checkSize()) {
						Party.disolve(this);
						return;
					}
					break;
				}
			} else {
				for (String s : getMembers()) {
					Player nl = Bukkit.getServer().getPlayer(UUID.fromString(s));
					leader = nl;
					if (!getPrivileges().contains(nl.getName()))
						getPrivileges().add(nl.getName());
					sendMessage(PartySystem.PARTY + "Leader just left, new Leader is : " + ChatColor.GREEN + leader.getName());
					leader.sendMessage(ChatColor.YELLOW + "You are the new party leader!");
					board.resetScores(ChatColor.WHITE + p.getName());
					update();
					if (checkSize()) {
						Party.disolve(this);
						return;
					}
					break;
				}
			}
		} else {
			members.remove(p.getUniqueId().toString());
			team.removeEntry(p.getName());
			privileges.remove(p.getName());
			sendMessage(ChatColor.YELLOW + "[" + p.getName() + "] " + ChatColor.RED + "has left the party!");
			checkSize();
			if (board != null)
				board.resetScores(ChatColor.WHITE + p.getName());
			update();
			p.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
			p.sendMessage(ChatColor.YELLOW + "You have left the Party!");
		}
	}
	
	public boolean checkSize() {
		if (getSize() == 1) {
			sendMessage(ChatColor.RED + "Party ended for lack of members!");
			for (String s : getMembers()) {
				Player pl = Bukkit.getServer().getPlayer(UUID.fromString(s));
				pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
			Party.disolve(this);
			return true;
		}
		return false;
	}

	public void displayBoard(Player p) {
		objective.setDisplayName(ChatColor.RED + leader.getName() + "'s" + ChatColor.BOLD + " Party");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (String uuid : members) {
			Player pp = Bukkit.getServer().getPlayer(UUID.fromString(uuid));
			int h = (int) Math.round(pp.getHealth());
			objective.getScore(ChatColor.WHITE + pp.getName()).setScore(h);
		}
		p.setScoreboard(board);
	}

	public void update() {
		new BukkitRunnable() {
			@Override
			public void run() {
				objective.setDisplayName(ChatColor.RED + leader.getName() + "'s" + ChatColor.BOLD + " Party");
				for (String s : getMembers()) {
					Player p = Bukkit.getServer().getPlayer(UUID.fromString(s));
					objective.getScore(ChatColor.WHITE + p.getName()).setScore((int) Math.round(p.getHealth()));
					p.setScoreboard(board);
				}
			}
		}.runTaskLater(PartySystem.getInstance(), 5L);
	}

	public void sendMessage(String msg) {
		for (String s : getMembers()) {
			Player p = Bukkit.getServer().getPlayer(UUID.fromString(s));
			p.sendMessage(msg);
		}
	}

	public static void disolve(Party party) {
		party.leader = null;
		party.privileges.clear();
		party.members.clear();
		party.team = null;
		party.board = null;
		party.objective = null;
		party.getManager().removeParty(party);
		party = null;
	}

	public Scoreboard getBoard(String name) {
		return board;
	}

	public String getMember(String uuid) {
		for (String s : members) {
			if (s.equals(uuid)) {
				return s;
			}
		}
		return null;
	}

	public boolean containsMember(String uuid) {
		if (getMember(uuid) != null) {
			return true;
		}
		return false;
	}

	public String getMember(int position) {
		return members.get(position);
	}
	
	public Player getPlayer(int position) {
		return Bukkit.getServer().getPlayer(UUID.fromString(members.get(position)));
	}

	public int getSize() {
		return members.size();
	}

	public void setLeader(Player leader) {
		this.leader = leader;
	}

	public Player getLeader() {
		return leader;
	}

	public List<String> getPrivileges() {
		return privileges;
	}

	public PartyManager getManager() {
		return PartySystem.getInstance().getManager();
	}

	public Inventory getPartyInventory() {
		return party_inv;
	}

	public boolean isAvailability() {
		return availability;
	}

	public void setAvailability(boolean availability) {
		this.availability = availability;
	}
}
