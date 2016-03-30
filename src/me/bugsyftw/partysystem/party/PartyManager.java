package me.bugsyftw.partysystem.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.bugsyftw.partysystem.PickedItem;

public class PartyManager {

	private List<Party> parties; // Lista de parties
	private Map<String, ArrayList<String>> inv_list = new HashMap<String, ArrayList<String>>();
	private HashMap<String, PickedItem> items = new HashMap<String, PickedItem>();

	public PartyManager() {
		parties = new ArrayList<Party>();
	}

	public void addParty(Party p) {
		parties.add(p);
	}

	public void removeParty(Party p) {
		parties.remove(p);
	}

	public Party getParty(Player leader) {
		for (Party pp : parties) {
			if (pp.getLeader().getName().equals(leader.getName())) {
				return pp;
			}
		}
		return null;
	}

	public Party getParty(String leader) {
		for (Party pp : parties) {
			if (pp.getLeader().getName().equals(leader)) {
				return pp;
			}
		}
		return null;
	}

	public Party getParty(UUID uuid) {
		for (Party pp : parties) {
			if (pp.getMember(uuid.toString()) != null) {
				return pp;
			}
		}
		return null;
	}

	public ArrayList<String> getInviteList(String name) {
		if (inv_list.containsKey(name)) {
			return inv_list.get(name);
		}
		return null;
	}

	public Party getInvite(String name, Party party) {
		if (inv_list.containsKey(name)) {
			for (Entry<String, ArrayList<String>> invites : inv_list.entrySet()) {
				if (invites.getKey().equals(name)) {
					for (String pt : invites.getValue()) {
						if (pt.equals(party.getLeader().getName())) {
							Party prt = getParty(pt);
							return prt;
						}
						break;
					}
					break;
				}
			}
		}
		return null;
	}

	public void addInvite(String name, Party p) {
		if (inv_list.containsKey(name)) {
			inv_list.get(name).add(p.getLeader().getName());
		} else {
			ArrayList<String> list = new ArrayList<>();
			list.add(p.getLeader().getName());
			inv_list.put(name, list);
		}
	}

	public void removeInvite(String name, Party party) {
		if (inv_list.containsKey(name)) {
			ArrayList<String> invites = inv_list.get(name);
			invites.remove(party.getLeader().getName());
		}
	}

	public boolean isInvited(String name) {
		if (inv_list.containsKey(name)) {
			return true;
		}
		return false;
	}

	public boolean isInvited(String name, Party party) {
		if (inv_list.containsKey(name)) {
			for (String leader : inv_list.get(name)) {
				if (party.getLeader().getName().equals(leader)) {
					return true;
				}
			}
		}
		return false;
	}

	public Map<String, ArrayList<String>> getInvitesList() {
		return inv_list;
	}

	public List<Party> getParties() {
		return parties;
	}

	public HashMap<String, PickedItem> getItemsMap() {
		return items;
	}

	public void setItemsMap(HashMap<String, PickedItem> items) {
		this.items = items;
	}
}
