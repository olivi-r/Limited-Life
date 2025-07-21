package com.life_series.limited_life.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.life_series.limited_life.Main;

public class GiveTimeCommand implements CommandExecutor, TabCompleter {
	Main plugin;

	public GiveTimeCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1)
			return null;

		else if (args.length == 2) {
			List<String> options = new ArrayList<>();
			options.add("hours");
			options.add("minutes");
			options.add("seconds");
			return options;
		}

		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//		validate command syntax
		if (args.length != 3)
			return false;

		int amount;
		try {
			amount = Integer.parseInt(args[2]);
		} catch (NumberFormatException err) {
			return false;
		}

		String type = args[1];
		if (type.equals("hours"))
			amount *= 3600;

		else if (type.equals("minutes"))
			amount *= 60;

//		validate command semantics
		Player receiver = Bukkit.getPlayerExact(args[0]);
		if (receiver == null) {
			sender.sendMessage(ChatColor.RED + "No player was found");
			return true;
		}

//		give time to reveiver
		boolean frozen = plugin.isFrozen();
		plugin.freeze();
		plugin.setTime(receiver, plugin.getTime(receiver) + amount);
		if (!frozen)
			plugin.unfreeze();

		return true;
	}

}
