package me.bugsyftw.partysystem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class PickedItem {

	private String owner;
	private List<ItemStack> items;

	public PickedItem(String owner, List<ItemStack> items) {
		this.owner = owner;
		this.items = items;
	}
	
	public PickedItem(String owner, ItemStack item) {
		this.owner = owner;
		this.items = new ArrayList<ItemStack>();
		this.items.add(item);
	}

	public void addItems(List<ItemStack> items, boolean t) {
		if (t) {
			for (ItemStack item : items) {
				getItems().add(item);
			}
		} else {
			items.addAll(items);
		}
	}
	
	public void addItem(ItemStack item) {
		getItems().add(item);
	}

	public void removeItem(ItemStack item) {
		items.remove(item);
	}

	public void removeItems(List<ItemStack> items) {
		items.removeAll(items);
	}

	public boolean contains(ItemStack item) {
		if (items.contains(item)) {
			return true;
		}
		return false;
	}

	public boolean contains(List<ItemStack> items) {
		if (items.containsAll(items)) {
			return true;
		}
		return false;
	}

	public List<ItemStack> getItems() {
		return items;
	}

	public String getOwner() {
		return owner;
	}

	public void setItems(List<ItemStack> items) {
		this.items = items;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}
