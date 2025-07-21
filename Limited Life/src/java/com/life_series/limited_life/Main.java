package com.life_series.limited_life;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.life_series.limited_life.command.FreezeCommand;
import com.life_series.limited_life.command.GetDefaultTimeCommand;
import com.life_series.limited_life.command.GiveTimeCommand;
import com.life_series.limited_life.command.ResetAllTimesCommand;
import com.life_series.limited_life.command.SetDefaultTimeCommand;
import com.life_series.limited_life.command.SetTimeCommand;
import com.life_series.limited_life.command.UnfreezeCommand;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;

public class Main extends JavaPlugin implements Listener {
	World world;
	public NamespacedKey timeKey;
	NamespacedKey defaultTimeKey;
	Thread timerThread;
	Team darkGreenTeam;
	Team greenTeam;
	Team yellowTeam;
	Team redTeam;
	Team blackTeam;
	static int startTime = 86400; // 24h
	static int greenTime = 64800; // 18h
	static int yellowTime = 43200; // 12h
	static int redTime = 21600; // 6h
	static int killProtectionTime = 30; // 30s
	boolean frozen;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		world = Bukkit.getWorlds().get(0);

		// data keys
		timeKey = new NamespacedKey(this, "time");
		defaultTimeKey = new NamespacedKey(this, "defaultTime");

		// freshen
		getDefaultTime();

		// setup commands
		GiveTimeCommand giveMinutesCommand = new GiveTimeCommand(this);
		getCommand("givetime").setExecutor(giveMinutesCommand);
		getCommand("givetime").setTabCompleter(giveMinutesCommand);

		FreezeCommand freezeCommand = new FreezeCommand(this);
		getCommand("freeze").setExecutor(freezeCommand);
		getCommand("freeze").setTabCompleter(freezeCommand);

		UnfreezeCommand unfreezeCommand = new UnfreezeCommand(this);
		getCommand("unfreeze").setExecutor(unfreezeCommand);
		getCommand("unfreeze").setTabCompleter(unfreezeCommand);

		SetTimeCommand setTimeCommand = new SetTimeCommand(this);
		getCommand("settime").setExecutor(setTimeCommand);
		getCommand("settime").setTabCompleter(setTimeCommand);

		GetDefaultTimeCommand getDefaultTimeCommand = new GetDefaultTimeCommand(this);
		getCommand("getdefaulttime").setExecutor(getDefaultTimeCommand);
		getCommand("getdefaulttime").setTabCompleter(getDefaultTimeCommand);

		SetDefaultTimeCommand setDefaultTimeCommand = new SetDefaultTimeCommand(this);
		getCommand("setdefaulttime").setExecutor(setDefaultTimeCommand);
		getCommand("setdefaulttime").setTabCompleter(setDefaultTimeCommand);

		ResetAllTimesCommand resetAllTimesCommand = new ResetAllTimesCommand(this);
		getCommand("resetalltimes").setExecutor(resetAllTimesCommand);
		getCommand("resetalltimes").setTabCompleter(resetAllTimesCommand);

		// setup teams
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

		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						for (Player player : Bukkit.getOnlinePlayers()) {
							refresh(player);
						}
						Thread.sleep(10);
					}
				} catch (InterruptedException err) {
					Thread.currentThread().interrupt();
				}
			}
		}).start();
		frozen = true;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
//		take hour of total time
		setTime(player, getTime(player) - 3600);
	}

	public int getDefaultTime() {
		Integer defaultTime = world.getPersistentDataContainer().get(defaultTimeKey, PersistentDataType.INTEGER);
		if (defaultTime == null) {
			defaultTime = startTime;
			setDefaultTime(startTime);
		}

		return Integer.max(0, defaultTime);
	}

	public void setDefaultTime(int value) {
		world.getPersistentDataContainer().set(defaultTimeKey, PersistentDataType.INTEGER, Integer.max(0, value));
	}

	public int getTime(Player player) {
		Integer time = player.getPersistentDataContainer().get(timeKey, PersistentDataType.INTEGER);
		if (time == null) {
			time = getDefaultTime();
			setTime(player, time);
		}

		return Integer.max(0, time);
	}

	public void setTime(Player player, int value) {
		player.getPersistentDataContainer().set(timeKey, PersistentDataType.INTEGER, Integer.max(0, value));
	}

	public String formatTime(int time) {
		time = Integer.max(0, time);
		int seconds = time % 60;
		time /= 60;
		int minutes = time % 60;
		time /= 60;
		return String.format("%02d:%02d:%02d", time, minutes, seconds);
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
							setDefaultTime(getDefaultTime() - 2);
							for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
								Player onlinePlayer = offlinePlayer.getPlayer();
								if (onlinePlayer != null) {
									setTime(onlinePlayer, getTime(onlinePlayer) - 1);
								} else {
									// OfflinePlayer doesn't load PersistantDataContainer
									// need write directly to <player uuid>.dat nbt file
									UUID id = offlinePlayer.getUniqueId();
									File playerDataFile = playerdata.resolve(String.join("", id.toString(), ".dat"))
											.toFile();
									NamedTag nbtData = NBTUtil.read(playerDataFile);
									CompoundTag bukkitValues = ((CompoundTag) nbtData.getTag())
											.getCompoundTag("BukkitValues");
									IntTag timeTag = bukkitValues.getIntTag(timeKey.toString());

									// Offline players lose time twice as fast
									timeTag.setValue(Integer.max(0, timeTag.asInt() - 2));
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

	void refresh(Player player) {
		if (player == null)
			return;

		int time = getTime(player);
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(formatTime(time)));

		String name = player.getName();
		if (time > greenTime)
			darkGreenTeam.addEntry(name);
		else if (time > yellowTime)
			greenTeam.addEntry(name);
		else if (time > redTime)
			yellowTeam.addEntry(name);
		else if (time > 0)
			redTeam.addEntry(name);
		else {
			blackTeam.addEntry(name);
			Bukkit.getScheduler().runTask(this, () -> {
				player.setGameMode(GameMode.SPECTATOR);
			});
		}
	}

}
