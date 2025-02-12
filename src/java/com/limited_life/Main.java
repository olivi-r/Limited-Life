package com.limited_life;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.limited_life.command.GiveMinutesCommand;
import com.limited_life.command.TimeCommand;

public class Main extends JavaPlugin implements Listener {
	NamespacedKey timeKey;
	NamespacedKey killProtectionKey;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		timeKey = new NamespacedKey(this, "time");
		killProtectionKey = new NamespacedKey(this, "killProtection");

		TimeCommand timeCommand = new TimeCommand(this);
		getCommand("time").setExecutor(timeCommand);
		getCommand("time").setTabCompleter(timeCommand);

		GiveMinutesCommand giveMinutesCommand = new GiveMinutesCommand(this);
		getCommand("giveminutes").setExecutor(giveMinutesCommand);
		getCommand("giveminutes").setTabCompleter(giveMinutesCommand);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (getLastDeath(player) == 0) {
//			take hour of total time and set spawn kill protection to 5mins
			setTime(player, getTime(player) - 3600);
			setLastDeath(player, 300);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
//		set time to 24hrs for new players
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore()) {
			player.getPersistentDataContainer().set(timeKey, PersistentDataType.INTEGER, 86400);
			player.getPersistentDataContainer().set(killProtectionKey, PersistentDataType.INTEGER, 0);
		}
	}

	public int getTime(Player player) {
		return Integer.max(0, player.getPersistentDataContainer().get(timeKey, PersistentDataType.INTEGER));
	}

	public void setTime(Player player, int value) {
		player.getPersistentDataContainer().set(timeKey, PersistentDataType.INTEGER, value);
	}

	public int getLastDeath(Player player) {
		return Integer.max(0, player.getPersistentDataContainer().get(killProtectionKey, PersistentDataType.INTEGER));
	}

	public void setLastDeath(Player player, int value) {
		player.getPersistentDataContainer().set(killProtectionKey, PersistentDataType.INTEGER, value);
	}

}
