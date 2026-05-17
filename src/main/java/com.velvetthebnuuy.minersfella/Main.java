package com.velvetthebnuuy.minersfella;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
        @Override
        public void onEnable() {
            getLogger().info("onEnable is called!");
            Bukkit.getPluginManager().registerEvents(new Events(), this);
            getLogger().info("YAYYYYY!");
        }
        @Override
        public void onDisable() {
            getLogger().info("onDisable is called!");
        }
    }

