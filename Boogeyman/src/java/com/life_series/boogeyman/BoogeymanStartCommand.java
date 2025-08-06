package com.life_series.boogeyman;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

class BoogeymanStartCommand implements CommandExecutor, TabCompleter {
	BoogeymanMain plugin;

	BoogeymanStartCommand(BoogeymanMain plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 1)
			return false;

		int count = 1;
		if (args.length == 1)
			try {
				count = Integer.max(0, Integer.parseInt(args[0]));
			} catch (NumberFormatException err) {
			}

		List<UUID> nonBoogeymen = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach(player -> nonBoogeymen.add(player.getUniqueId()));

		if (count > nonBoogeymen.size()) {
			sender.sendMessage(ChatColor.RED + "Not enough players remaining");
			return true;
		}

		List<UUID> boogeymen = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int index = ThreadLocalRandom.current().nextInt(nonBoogeymen.size());
			boogeymen.add(nonBoogeymen.remove(index));
		}

		new Thread(new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.DARK_RED + "The Boogeymen are about to be chosen");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Bukkit.getOnlinePlayers().forEach(player -> {
					player.sendTitle(ChatColor.GREEN + "3", "", 10, 70, 20);
					player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Bukkit.getOnlinePlayers().forEach(player -> {
					player.sendTitle(ChatColor.YELLOW + "2", "", 10, 70, 20);
					player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Bukkit.getOnlinePlayers().forEach(player -> {
					player.sendTitle(ChatColor.RED + "1", "", 10, 70, 20);
					player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Bukkit.getOnlinePlayers()
						.forEach(player -> player.sendTitle(ChatColor.GOLD + "You are...", "", 10, 70, 20));

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				nonBoogeymen.forEach(uuid -> {
					Player player = Bukkit.getPlayer(uuid);
					if (player != null) {
						player.sendTitle(ChatColor.GREEN + "NOT the Boogeyman.", "", 10, 70, 20);
						player.sendMessage(ChatColor.GREEN + "You are not the Boogeyman.");
						player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
					}
				});

				boogeymen.forEach(uuid -> {
					Player player = Bukkit.getPlayer(uuid);
					if (player != null) {
						player.sendTitle(ChatColor.RED + "The Boogeyman.", "", 10, 70, 20);
						player.sendMessage(ChatColor.RED
								+ "You are the Boogeyman. Kill another player before the end of the session.");
						player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
					}
				});

				plugin.setBoogeymen(boogeymen);
			}
		}).start();
		return true;
	}

}
