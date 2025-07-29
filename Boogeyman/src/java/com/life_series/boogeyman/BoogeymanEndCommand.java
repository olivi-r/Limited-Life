package com.life_series.boogeyman;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

class BoogeymanEndCommand implements CommandExecutor, TabCompleter {
	BoogeymanMain plugin;

	BoogeymanEndCommand(BoogeymanMain plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		plugin.getBoogeymen().forEach(uuid -> BoogeymanMain.getHandler().boogeyFail(Bukkit.getOfflinePlayer(uuid)));
		plugin.setBoogeymen(new ArrayList<>());
		return true;
	}

}
