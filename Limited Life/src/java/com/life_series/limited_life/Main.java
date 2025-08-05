package com.life_series.limited_life;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

import com.life_series.boogeyman.BoogeymanHandler;
import com.life_series.boogeyman.BoogeymanMain;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;

public class Main extends JavaPlugin implements BoogeymanHandler, Listener {
	Path playerdata;
	World world;
	NamespacedKey timeKey;
	NamespacedKey defaultTimeKey;
	NamespacedKey lastDeathTimeKey;
	Thread timerThread;
	Team darkGreenTeam;
	Team greenTeam;
	Team yellowTeam;
	Team redTeam;
	Team blackTeam;
	static final int startTime = 86400; // 24h
	static final int greenTime = 64800; // 18h
	static final int yellowTime = 43200; // 12h
	static final int redTime = 21600; // 6h
	static final int killProtectionTime = 30; // 30s
	boolean frozen;

	@Override
	public void boogeySucceed(OfflinePlayer boogeyman) {
		Player onlinePlayer = boogeyman.getPlayer();
		if (onlinePlayer != null)
			onlinePlayer.sendTitle(ChatColor.GREEN + "You are cured", ChatColor.GREEN + "No longer the boogeyman", 10,
					70, 20);
	}

	@Override
	public void boogeyFail(OfflinePlayer boogeyman) {
		Player onlinePlayer = boogeyman.getPlayer();
		setTime(boogeyman, getTime(boogeyman) - 21600);
		if (onlinePlayer != null)
			onlinePlayer.sendTitle(ChatColor.RED + "You failed the task", ChatColor.RED + "Ran out of time to kill", 10,
					70, 20);
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		BoogeymanMain.setHandler(this);

		world = Bukkit.getWorlds().get(0);
		playerdata = Bukkit.getWorlds().getFirst().getWorldFolder().toPath().resolve("playerdata");

		// data keys
		timeKey = new NamespacedKey(this, "time");
		defaultTimeKey = new NamespacedKey(this, "defaultTime");
		lastDeathTimeKey = new NamespacedKey(this, "lastDeath");

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
						Thread.sleep(1000);
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
		Player killer = player.getKiller();
		int playerTime = getTime(player);

		LocalDateTime lastDeath = getLastDeath(player);
		LocalDateTime now = LocalDateTime.now();
		if (frozen || ChronoUnit.SECONDS.between(lastDeath, now) < 60) {
			updateLastDeath(player);
			return;
		}

		if (killer != null) {
			UUID playerId = player.getUniqueId();
			UUID killerId = killer.getUniqueId();
			if (!playerId.equals(killerId)) {
				BoogeymanMain boogeymanPlugin = (BoogeymanMain) Bukkit.getPluginManager().getPlugin("Boogeyman");
				boogeymanPlugin.getBoogeymen().forEach(uuid -> {
					if (killerId.equals(uuid))
						boogeymanPlugin.cure(killerId);
				});

				int killerTime = getTime(killer);
				if (killerTime <= redTime)
					setTime(killer, killerTime + 2700); // +45m

				else if (killerTime <= yellowTime && playerTime > yellowTime)
					setTime(killer, killerTime + 1800); // +30m

				else if (killerTime <= greenTime && playerTime > greenTime)
					setTime(killer, killerTime + 900); // +15m
			}
		}

		// take hour of total time
		setTime(player, playerTime - 3600);
		updateLastDeath(player);
	}

	int getDefaultTime() {
		Integer defaultTime = world.getPersistentDataContainer().get(defaultTimeKey, PersistentDataType.INTEGER);
		if (defaultTime == null) {
			defaultTime = startTime;
			setDefaultTime(startTime);
		}

		return Integer.max(0, defaultTime);
	}

	void setDefaultTime(int value) {
		world.getPersistentDataContainer().set(defaultTimeKey, PersistentDataType.INTEGER, Integer.max(0, value));
	}

	void updateLastDeath(Player player) {
		player.getPersistentDataContainer().set(lastDeathTimeKey, PersistentDataType.STRING,
				LocalDateTime.now().toString());
	}

	LocalDateTime getLastDeath(Player player) {
		String lastDeath = player.getPersistentDataContainer().get(lastDeathTimeKey, PersistentDataType.STRING);
		if (lastDeath == null)
			return LocalDateTime.MIN;

		return LocalDateTime.parse(lastDeath);
	}

	int getTime(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		Integer time = null;
		if (onlinePlayer != null)
			time = onlinePlayer.getPersistentDataContainer().get(timeKey, PersistentDataType.INTEGER);
		else
			try {
				UUID playerId = player.getUniqueId();
				File playerDataFile = playerdata.resolve(String.join("", playerId.toString(), ".dat")).toFile();
				NamedTag nbtData = NBTUtil.read(playerDataFile);
				CompoundTag bukkitValues = ((CompoundTag) nbtData.getTag()).getCompoundTag("BukkitValues");
				IntTag timeTag = bukkitValues.getIntTag(timeKey.toString());
				time = timeTag.asInt();
			} catch (IOException e) {
				e.printStackTrace();
			}

		if (time == null) {
			time = getDefaultTime();
			setTime(player, time);
		}

		return Integer.max(0, time);
	}

	void setTime(OfflinePlayer player, int value) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer != null)
			onlinePlayer.getPersistentDataContainer().set(timeKey, PersistentDataType.INTEGER, Integer.max(0, value));
		else
			try {
				UUID playerId = player.getUniqueId();
				File playerDataFile = playerdata.resolve(String.join("", playerId.toString(), ".dat")).toFile();
				NamedTag nbtData = NBTUtil.read(playerDataFile);
				CompoundTag bukkitValues = ((CompoundTag) nbtData.getTag()).getCompoundTag("BukkitValues");
				IntTag timeTag = bukkitValues.getIntTag(timeKey.toString());
				timeTag.setValue(Integer.max(0, value));
				NBTUtil.write(nbtData, playerDataFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	String formatTime(int time) {
		time = Integer.max(0, time);
		int seconds = time % 60;
		time /= 60;
		int minutes = time % 60;
		time /= 60;
		return String.format("%02d:%02d:%02d", time, minutes, seconds);
	}

	void unfreeze() {
		freeze();
		timerThread = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						Thread.sleep(1000);
						setDefaultTime(getDefaultTime() - 2);
						for (OfflinePlayer player : Bukkit.getOfflinePlayers())
							if (player.isOnline())
								setTime(player, getTime(player) - 1);
							else
								setTime(player, getTime(player) - 2);
					}
				} catch (InterruptedException err) {
					Thread.currentThread().interrupt();
				}
			}
		});
		timerThread.start();
		frozen = false;
	}

	void freeze() {
		if (timerThread != null)
			timerThread.interrupt();

		frozen = true;
	}

	boolean isFrozen() {
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
