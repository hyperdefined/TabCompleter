/*
 * This file is part of TabCompleter.
 *
 * TabCompleter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TabCompleter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TabCompleter.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.tabcompleter;

import lol.hyper.tabcompleter.commands.CommandReload;
import lol.hyper.tabcompleter.events.PlayerCommandPreprocess;
import lol.hyper.tabcompleter.events.PlayerCommandSend;
import lol.hyper.tabcompleter.events.PlayerLeave;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TabCompleter extends JavaPlugin implements Listener {

    public File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;
    public Logger logger = this.getLogger();

    // this stores which groups have which commands from the config
    public HashMap<String, List<String>> groupCommands = new HashMap<>();

    public PlayerCommandPreprocess playerCommandPreprocess;
    public PlayerCommandSend playerCommandSend;
    public PlayerLeave playerLeave;

    @Override
    public void onEnable() {
        loadConfig(configFile);

        playerCommandPreprocess = new PlayerCommandPreprocess(this);
        playerCommandSend = new PlayerCommandSend(this);
        playerLeave = new PlayerLeave();

        Bukkit.getServer().getPluginManager().registerEvents(playerCommandPreprocess, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerCommandSend, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerLeave, this);

        this.getCommand("tcreload").setExecutor(new CommandReload(this));

        Metrics metrics = new Metrics(this, 10305);
    }

    public void loadConfig(File file) {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(file);

        groupCommands.clear();
        for (String group : config.getConfigurationSection("groups").getKeys(false)) {
            List<String> commands = config.getStringList("groups." + group + ".commands");

            // inherit is set to false if you don't want it
            if (!config.getString("groups." + group + ".inherit").equalsIgnoreCase("false")) {
                String otherGroup = config.getString("groups." + group + ".inherit");
                if (!config.getConfigurationSection("groups").getKeys(false).contains(otherGroup)) {
                    logger.warning(otherGroup + " does NOT EXIST! Group " + group + " is trying to inherit commands from this group!");
                } else {
                    commands.addAll(config.getStringList("groups." + otherGroup + ".commands"));
                }
            }
            groupCommands.put(group, commands);
        }

        if (!config.getConfigurationSection("groups").contains("default")) {
            logger.warning("There is no default group set! Things will break!");
        }

        if (config.getInt("config-version") != 1) {
            logger.warning("Your config file is outdated! Please regenerate the config.");
        }
    }

    /**
     * Get the group for a player.
     * @param player Player to check group for.
     * @return Group player is in. Returns null if we can't get the group.
     */
    public String getGroup(Player player) {
        String group = null;
        // probably a better way of doing this
        for (String perm : groupCommands.keySet()) {
            if (player.hasPermission("tabcompleter.groups." + perm)) {
                group = perm;
            }
        }
        return group;
    }
}
