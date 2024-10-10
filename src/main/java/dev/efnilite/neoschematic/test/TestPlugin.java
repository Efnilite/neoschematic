package dev.efnilite.neoschematic.test;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.shutdown();
    }

}
