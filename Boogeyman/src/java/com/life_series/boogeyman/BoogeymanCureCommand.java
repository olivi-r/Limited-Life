package com.life_series.boogeyman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.md_5.bungee.api.ChatColor;

class BoogeymanCureCommand implements CommandExecutor, TabCompleter {
	BoogeymanMain plugin;

	BoogeymanCureCommand(BoogeymanMain plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		if (args.length == 1)
			plugin.getBoogeymen().forEach(uuid -> completions.add(Bukkit.getOfflinePlayer(uuid).getName()));

		return completions;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 1)
			return false;

		Map<String, UUID> boogeymen = new HashMap<>();
		plugin.getBoogeymen().forEach(uuid -> boogeymen.put(Bukkit.getOfflinePlayer(uuid).getName(), uuid));

		if (boogeymen.containsKey(args[0]))
			plugin.cure(boogeymen.get(args[0]));
		else
			sender.sendMessage(ChatColor.RED + "No boogeyman with that username");

		return true;
	}

}
