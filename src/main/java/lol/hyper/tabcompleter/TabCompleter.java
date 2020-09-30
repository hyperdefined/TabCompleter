package lol.hyper.tabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class TabCompleter extends JavaPlugin implements Listener {

    private static TabCompleter instance;
    public File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        loadConfig(configFile);
        Bukkit.getServer().getPluginManager().registerEvents(new CommandEvents(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static TabCompleter getInstance() {
        return instance;
    }

    public void loadConfig(File file) {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }
}
