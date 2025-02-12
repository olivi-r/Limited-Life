package com.limited_life;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.limited_life.command.TimeCommand;

public class Main extends JavaPlugin implements Listener {
	NamespacedKey timeKey;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		timeKey = new NamespacedKey(this, "time");

		TimeCommand timeCommand = new TimeCommand(this);
		getCommand("time").setExecutor(timeCommand);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
//		set time to 24hrs for new players
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore()) {
			player.getPersistentDataContainer().set(timeKey, PersistentDataType.INTEGER, 24 * 60 * 60);
		}
	}

	public Integer getTime(Player player) {
		return player.getPersistentDataContainer().get(timeKey, PersistentDataType.INTEGER);
	}
}
