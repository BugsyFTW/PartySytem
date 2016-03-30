package me.bugsyftw.partysystem.party;

import org.bukkit.permissions.Permission;

public enum PartyPermissions {
	
	CREATE("bparty.create");
	
	private String perm;
	
	private PartyPermissions(String perm) {
		this.perm = perm;
	}
	
	public Permission getPermission() {
		return new Permission(perm);
	}
}
