package me.bugsyftw.partysystem;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.bugsyftw.partysystem.listeners.CommandExecuter;
import me.bugsyftw.partysystem.listeners.ConnectionListener;
import me.bugsyftw.partysystem.listeners.EventListener;
import me.bugsyftw.partysystem.listeners.PartyListener;
import me.bugsyftw.partysystem.party.PartyManager;
import me.bugsyftw.partysystem.party.PartyPermissions;

public class PartySystem extends JavaPlugin {

	private static PartySystem instance;
	private PartyManager manager;
	public static String PARTY = ChatColor.RED + "[Party] " + ChatColor.GRAY;

	public void onEnable() {
		instance = this;
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.addPermission(PartyPermissions.CREATE.getPermission());
		manager = new PartyManager();
		getCommand("party").setExecutor(new CommandExecuter(this));
		getCommand("p").setExecutor(new CommandExecuter(this));
		pm.registerEvents(new EventListener(this), this);
		pm.registerEvents(new ConnectionListener(this), this);
		pm.registerEvents(new PartyListener(this), this);
		getConfig().options().header("If you don't know the meaning of these configurations, check the page where you download for more information");
		getConfig().addDefault("Party.PVP", false);
		getConfig().addDefault("Party.Invitation-Time", 15);
		getConfig().addDefault("Party.Teleportion.Enable", true);
		getConfig().addDefault("Party.Teleportion.Lock-Moviment", true);
		getConfig().addDefault("Party.Prefix", "&c[P]&f");
		getConfig().addDefault("Party.Invite.Nearby", true);
		getConfig().addDefault("Party.Invite.Radious", 15);
		getConfig().addDefault("Player.Damage-Output", false);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public static void logMessage(String msg) {
		Logger.getLogger("Minecraft").info(msg);
	}

	public PartyManager getManager() {
		return manager;
	}

	public static PartySystem getInstance() {
		return instance;
	}
}
