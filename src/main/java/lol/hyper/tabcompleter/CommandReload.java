package lol.hyper.tabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandReload implements CommandExecutor {

    private final TabCompleter tabCompleter;

    public CommandReload(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp() || sender.hasPermission("tabcompleter.reload")) {
            tabCompleter.loadConfig(tabCompleter.configFile);
            sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload.");
        }
        return true;
    }
}
