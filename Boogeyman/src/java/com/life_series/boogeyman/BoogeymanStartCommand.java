package com.life_series.boogeyman;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
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

		nonBoogeymen.forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null)
				player.sendTitle(ChatColor.YELLOW + "You are not the boogeyman", "", 10, 70, 20);
		});

		boogeymen.forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null)
				player.sendTitle(ChatColor.RED + "You are the boogeyman",
						ChatColor.RED + "Kill another player before the end of the session", 10, 70, 20);
		});

		plugin.setBoogeymen(boogeymen);
		return true;
	}

}
