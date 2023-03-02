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

import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.tabcompleter.commands.CommandReload;
import lol.hyper.tabcompleter.events.PlayerCommandPreprocess;
import lol.hyper.tabcompleter.events.PlayerCommandSend;
import lol.hyper.tabcompleter.events.PlayerLeave;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class TabCompleter extends JavaPlugin implements Listener {

    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;
    public final Logger logger = this.getLogger();

    // this stores which groups have which commands from the config
    public final HashMap<String, List<String>> groupCommands = new HashMap<>();

    public PlayerCommandPreprocess playerCommandPreprocess;
    public PlayerCommandSend playerCommandSend;
    public PlayerLeave playerLeave;

    public Permission permission = null;

    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        loadConfig(configFile);

        this.adventure = BukkitAudiences.create(this);

        playerCommandPreprocess = new PlayerCommandPreprocess(this);
        playerCommandSend = new PlayerCommandSend(this);
        playerLeave = new PlayerLeave();

        Bukkit.getServer().getPluginManager().registerEvents(playerCommandPreprocess, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerCommandSend, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerLeave, this);

        this.getCommand("tcreload").setExecutor(new CommandReload(this));

        new Metrics(this, 10305);

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            logger.severe("Vault is not installed!");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            permission = rsp.getProvider();
        }
    }

    public void loadConfig(File file) {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(file);

        groupCommands.clear();

        // load all base commands form config
        ConfigurationSection groups = config.getConfigurationSection("groups");
        if (groups == null) {
            logger.severe("The groups section in the config is missing! Plugin cannot function and will be disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        for (String configGroup : groups.getKeys(false)) {
            List<String> commands = config.getStringList("groups." + configGroup + ".commands");
            groupCommands.put(configGroup, commands);
        }

        if (groupCommands.size() == 0) {
            logger.warning("There were not groups listed in the groups section of the config. Please add the groups. The plugin will not function currently.");
        }

        if (config.getInt("config-version") != 2) {
            logger.warning("Your config file is outdated! Please regenerate the config.");
        }
    }

    /**
     * Gets a message from config.yml.
     *
     * @param path The path to the message.
     * @return Component with formatting applied.
     */
    public Component getMessage(String path) {
        String message = config.getString(path);
        if (message == null) {
            logger.warning(path + " is not a valid message!");
            return Component.text("Invalid path! " + path).color(NamedTextColor.RED);
        }
        return miniMessage.deserialize(message);
    }

    public void checkForUpdates() {
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("TabCompleter", "hyperdefined");
        } catch (IOException e) {
            logger.warning("Unable to check updates!");
            e.printStackTrace();
            return;
        }
        GitHubRelease current = api.getReleaseByTag(this.getDescription().getVersion());
        GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warning("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warning("A new version is available (" + latest.getTagVersion() + ")! You are running version " + current.getTagVersion() + ". You are " + buildsBehind + " version(s) behind.");
        }
    }

    public BukkitAudiences getAdventure() {
        if (this.adventure == null) {
            throw new IllegalStateException(
                    "Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    /**
     * Get all commands that a player can do/see.
     *
     * @param player The player to check.
     * @return List of all commands the player can do/see.
     */
    public List<String> getCommandsForPlayer(Player player) {
        List<String> allAllowedCommands = new ArrayList<>();
        String[] playerGroups = permission.getPlayerGroups(player);
        for (String playerGroup : playerGroups) {
            List<String> commands = groupCommands.get(playerGroup);
            // player is not in a group we have tracked
            // or the commands saved are empty
            // in this case, just ignore it
            if (commands == null || commands.isEmpty()) {
                continue;
            }
            allAllowedCommands.addAll(commands);
        }
        return allAllowedCommands;
    }
}
