package com.limited_life.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.limited_life.Main;

public class GiveMinutesCommand implements CommandExecutor, TabCompleter {
	Main plugin;

	public GiveMinutesCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 1)
			return new ArrayList<>();

		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//		validate command syntax
		if (!(sender instanceof Player && args.length == 2))
			return false;

		int given;
		try {
			given = Integer.parseInt(args[2]);
		} catch (NumberFormatException err) {
			return false;
		}

//		validate command semantics
		Player player = (Player) sender;
		Player receiver = Bukkit.getPlayerExact(args[0]);
		if (receiver == null || player == receiver) {
			sender.sendMessage(ChatColor.RED + "The receiver must be another player");
			return true;
		}

		int time = plugin.getTime(player) - 60 * given;
		if (time < 1) {
			sender.sendMessage(ChatColor.RED + "You do not have enough time left to give this much away");
			return true;
		}

//		give sender's time to reveiver
		plugin.setTime(player, time);
		plugin.setTime(receiver, plugin.getTime(receiver) + 60 * given);
		plugin.refresh(player);
		plugin.refresh(receiver);
		return true;
	}

}
