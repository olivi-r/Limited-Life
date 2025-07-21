package com.life_series.limited_life.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.life_series.limited_life.Main;

public class SetDefaultTimeCommand implements CommandExecutor, TabCompleter {
	Main plugin;

	public SetDefaultTimeCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 1)
			return false;

		Integer defaultTime = 86400;
		try {
			defaultTime = Integer.max(0, Integer.parseInt(args[0]));
		} catch (NumberFormatException err) {
			return false;
		}

		plugin.setDefaultTime(defaultTime);
		return true;
	}

}
