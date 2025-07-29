package com.life_series.boogeyman;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

public class BoogeymanMain extends JavaPlugin {
	static final UUIDListPersistentDataType UUIDList = new UUIDListPersistentDataType();
	static BoogeymanHandler handler;
	PersistentDataContainer dataContainer;
	NamespacedKey boogeymenKey;

	public static void setHandler(BoogeymanHandler handler) {
		BoogeymanMain.handler = handler;
	}

	public static BoogeymanHandler getHandler() {
		return handler;
	}

	@Override
	public void onEnable() {
		dataContainer = Bukkit.getWorlds().get(0).getPersistentDataContainer();
		boogeymenKey = new NamespacedKey(this, "boogeymen");

		BoogeymanStartCommand boogeymanStartCommand = new BoogeymanStartCommand(this);
		getCommand("boogeyman-start").setExecutor(boogeymanStartCommand);
		getCommand("boogeyman-start").setTabCompleter(boogeymanStartCommand);

		BoogeymanEndCommand boogeymanEndCommand = new BoogeymanEndCommand(this);
		getCommand("boogeyman-end").setExecutor(boogeymanEndCommand);
		getCommand("boogeyman-end").setTabCompleter(boogeymanEndCommand);

		BoogeymanListCommand boogeymanListCommand = new BoogeymanListCommand(this);
		getCommand("boogeyman-list").setExecutor(boogeymanListCommand);
		getCommand("boogeyman-list").setTabCompleter(boogeymanListCommand);

		BoogeymanCureCommand boogeymanCureCommand = new BoogeymanCureCommand(this);
		getCommand("boogeyman-cure").setExecutor(boogeymanCureCommand);
		getCommand("boogeyman-cure").setTabCompleter(boogeymanCureCommand);
	}

	public List<UUID> getBoogeymen() {
		List<UUID> boogeymen = dataContainer.get(boogeymenKey, UUIDList);
		if (boogeymen == null) {
			boogeymen = new ArrayList<>();
			setBoogeymen(boogeymen);
		}

		return boogeymen;
	}

	public void cure(UUID boogeymanPlayerId) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(boogeymanPlayerId);
		List<UUID> updatedBoogeymen = getBoogeymen();
		updatedBoogeymen.remove(boogeymanPlayerId);
		setBoogeymen(updatedBoogeymen);
		if (handler != null)
			handler.boogeySucceed(player);
	}

	public void setBoogeymen(List<UUID> boogeymen) {
		dataContainer.set(boogeymenKey, UUIDList, boogeymen);
	}

}
