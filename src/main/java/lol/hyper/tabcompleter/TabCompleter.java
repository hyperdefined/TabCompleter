package lol.hyper.tabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class TabCompleter extends JavaPlugin implements Listener {

    public File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;

    @Override
    public void onEnable() {
        loadConfig(configFile);
        Bukkit.getServer().getPluginManager().registerEvents(new CommandEvents(this), this);
        this.getCommand("tcreload").setExecutor(new CommandReload(this));
    }

    public void loadConfig(File file) {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }
}
