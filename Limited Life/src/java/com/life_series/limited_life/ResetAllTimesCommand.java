package com.life_series.limited_life;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;

class ResetAllTimesCommand implements CommandExecutor, TabCompleter {
	Main plugin;

	ResetAllTimesCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Path playerdata = Bukkit.getWorlds().getFirst().getWorldFolder().toPath().resolve("playerdata");
		for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
			Player onlinePlayer = offlinePlayer.getPlayer();
			if (onlinePlayer != null) {
				plugin.setTime(onlinePlayer, plugin.getDefaultTime());
			} else {
				try {
					UUID id = offlinePlayer.getUniqueId();
					File playerDataFile = playerdata.resolve(String.join("", id.toString(), ".dat")).toFile();
					NamedTag nbtData = NBTUtil.read(playerDataFile);
					CompoundTag bukkitValues = ((CompoundTag) nbtData.getTag()).getCompoundTag("BukkitValues");
					IntTag timeTag = bukkitValues.getIntTag(plugin.timeKey.toString());

					// Offline players lose time twice as fast
					timeTag.setValue(plugin.getDefaultTime());
					NBTUtil.write(nbtData, playerDataFile);
				} catch (IOException err) {
				}
			}
		}

		return true;
	}

}
