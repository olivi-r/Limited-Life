package com.limited_life;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.limited_life.command.FreezeCommand;
import com.limited_life.command.GiveMinutesCommand;
import com.limited_life.command.SetTimeCommand;
import com.limited_life.command.TimeCommand;
import com.limited_life.command.UnfreezeCommand;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;

public class Main extends JavaPlugin implements Listener {
	NamespacedKey timeKey;
	NamespacedKey killProtectionKey;
	Thread timerThread;
	Team darkGreenTeam;
	Team greenTeam;
	Team yellowTeam;
	Team redTeam;
	Team blackTeam;
	static int greenTime = 64800; // 18hrs
	static int yellowTime = 43200; // 12hrs
	static int redTime = 21600; // 6hrs
	static int killProtectionTime = 300; // 5mins
	boolean frozen;

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

		FreezeCommand freezeCommand = new FreezeCommand(this);
		getCommand("freeze").setExecutor(freezeCommand);
		getCommand("freeze").setTabCompleter(freezeCommand);

		UnfreezeCommand unfreezeCommand = new UnfreezeCommand(this);
		getCommand("unfreeze").setExecutor(unfreezeCommand);
		getCommand("unfreeze").setTabCompleter(unfreezeCommand);

		SetTimeCommand setTimeCommand = new SetTimeCommand(this);
		getCommand("settime").setExecutor(setTimeCommand);
		getCommand("settime").setTabCompleter(setTimeCommand);

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getMainScoreboard();
		darkGreenTeam = board.getTeam("darkGreen");
		greenTeam = board.getTeam("green");
		yellowTeam = board.getTeam("yellow");
		redTeam = board.getTeam("red");
		blackTeam = board.getTeam("black");

		if (darkGreenTeam == null) {
			darkGreenTeam = board.registerNewTeam("darkGreen");
			darkGreenTeam.setColor(ChatColor.DARK_GREEN);
		}

		if (greenTeam == null) {
			greenTeam = board.registerNewTeam("green");
			greenTeam.setColor(ChatColor.GREEN);
		}

		if (yellowTeam == null) {
			yellowTeam = board.registerNewTeam("yellow");
			yellowTeam.setColor(ChatColor.YELLOW);
		}

		if (redTeam == null) {
			redTeam = board.registerNewTeam("red");
			redTeam.setColor(ChatColor.RED);
		}

		if (blackTeam == null) {
			blackTeam = board.registerNewTeam("black");
			blackTeam.setColor(ChatColor.BLACK);
		}

		frozen = true;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (getKillProtection(player) == 0) {
//			take hour of total time and set spawn kill protection to 5mins
			setTime(player, getTime(player) - 3600);
			setKillProtection(player, killProtectionTime);
			refresh(player);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
//		set time to 24hrs for new players
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore()) {
			setTime(player, 86400);
			setKillProtection(player, killProtectionTime);
		}
		refresh(player);
	}

	public int getTime(Player player) {
		return Integer.max(0, player.getPersistentDataContainer().get(timeKey, PersistentDataType.INTEGER));
	}

	public void setTime(Player player, int value) {
		player.getPersistentDataContainer().set(timeKey, PersistentDataType.INTEGER, Integer.max(0, value));
	}

	public int getKillProtection(Player player) {
		return Integer.max(0, player.getPersistentDataContainer().get(killProtectionKey, PersistentDataType.INTEGER));
	}

	public void setKillProtection(Player player, int value) {
		player.getPersistentDataContainer().set(killProtectionKey, PersistentDataType.INTEGER, Integer.max(0, value));
	}

	public void unfreeze() {
		Path playerdata = Bukkit.getWorlds().getFirst().getWorldFolder().toPath().resolve("playerdata");
		freeze();
		timerThread = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						try {
							Thread.sleep(1000);
							OfflinePlayer[] players = Bukkit.getOfflinePlayers();
							for (int i = 0; i < players.length; i++) {
								OfflinePlayer offlinePlayer = players[i];
								Player onlinePlayer = offlinePlayer.getPlayer();
								if (onlinePlayer != null) {
									setTime(onlinePlayer, getTime(onlinePlayer) - 1);
									setKillProtection(onlinePlayer, getKillProtection(onlinePlayer) - 1);
									refresh(onlinePlayer);
								} else {
									// OfflinePlayer doesnt load PersistantDataContainer
									// need write directly to <player uuid>.dat nbt file
									UUID id = offlinePlayer.getUniqueId();
									File playerDataFile = playerdata.resolve(String.join("", id.toString(), ".dat"))
											.toFile();
									NamedTag nbtData = NBTUtil.read(playerDataFile);
									CompoundTag bukkitValues = ((CompoundTag) nbtData.getTag())
											.getCompoundTag("BukkitValues");
									IntTag timeTag = bukkitValues.getIntTag(timeKey.toString());
									IntTag killProtectionTag = bukkitValues.getIntTag(killProtectionKey.toString());

									// Offline players lose time twice as fast
									timeTag.setValue(Integer.max(0, timeTag.asInt() - 2));
									killProtectionTag.setValue(Integer.max(0, killProtectionTag.asInt() - 1));
									NBTUtil.write(nbtData, playerDataFile);
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (InterruptedException err) {
					Thread.currentThread().interrupt();
				}
			}
		});
		timerThread.start();
		frozen = false;
	}

	public void freeze() {
		if (timerThread != null)
			timerThread.interrupt();

		frozen = true;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void refresh(Player player) {
		int time = getTime(player);
		String name = player.getName();
		if (time > greenTime)
			darkGreenTeam.addEntry(name);
		else if (time > yellowTime)
			greenTeam.addEntry(name);
		else if (time > redTime)
			yellowTeam.addEntry(name);
		else if (time > 0)
			redTeam.addEntry(name);
		else
			blackTeam.addEntry(name);
	}

}
