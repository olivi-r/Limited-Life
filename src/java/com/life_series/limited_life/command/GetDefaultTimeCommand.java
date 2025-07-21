package com.life_series.limited_life.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.life_series.limited_life.Main;

public class GetDefaultTimeCommand implements CommandExecutor, TabCompleter {
	Main plugin;

	public GetDefaultTimeCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		sender.sendMessage(Integer.toString(plugin.getDefaultTime()));
		return true;
	}

}
