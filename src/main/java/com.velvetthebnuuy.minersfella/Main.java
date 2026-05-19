package com.velvetthebnuuy.minersfella;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(new Events(this), this);
		getLogger().info("Ready to mine :3 !");
	}

	@Override
	public void onDisable() {
		getLogger().info("Plugin shutting down...");
	}
}
