package com.life_series.boogeyman;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

class BoogeymanCommand implements CommandExecutor, TabCompleter {
	Main plugin;

	BoogeymanCommand(Main plugin) {
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

		List<String> names = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));

		plugin.boogeys.clear();
		if (count > names.size()) {
			sender.sendMessage(ChatColor.RED + "Not enough players remaining");
			return true;
		}

		for (int i = 0; i < count; i++) {
			int index = ThreadLocalRandom.current().nextInt(names.size());
			plugin.boogeys.add(names.remove(index));
		}

		plugin.boogeys.forEach(name -> {
			Player player = Bukkit.getPlayerExact(name);
			player.sendMessage("boogey");
		});

		return true;
	}

}
