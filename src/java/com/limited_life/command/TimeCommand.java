package com.limited_life.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.limited_life.Main;

public class TimeCommand implements CommandExecutor, TabCompleter {
	Main plugin;

	public TimeCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			int time = plugin.getTime((Player) sender);
			int seconds = time % 60;
			time /= 60;
			int minutes = time % 60;
			time /= 60;
			sender.sendMessage(String.format("You have %02d:%02d:%02d left", time, minutes, seconds));
			return true;
		}
		return false;
	}

}
