package com.life_series.boogeyman;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	List<String> boogeys;

	@Override
	public void onEnable() {
		boogeys = new ArrayList<>();

		BoogeymanCommand boogeymanCommand = new BoogeymanCommand(this);
		getCommand("boogeyman").setExecutor(boogeymanCommand);
		getCommand("boogeyman").setTabCompleter(boogeymanCommand);
	}

}
