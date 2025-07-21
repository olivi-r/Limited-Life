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

public class SetTimeCommand implements CommandExecutor, TabCompleter {
	Main plugin;

	public SetTimeCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1)
			return null;

		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// validate command syntax
		if (args.length != 2)
			return false;

		Integer time;
		try {
			time = Integer.parseInt(args[1]);
		} catch (NumberFormatException err) {
			return false;
		}

		// validate command semantics
		Player player = Bukkit.getPlayerExact(args[0]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "The target must be a player");
			return true;
		}

		boolean frozen = plugin.isFrozen();
		plugin.freeze();
		plugin.setTime(player, time);
		if (!frozen)
			plugin.unfreeze();

		return true;
	}

}
