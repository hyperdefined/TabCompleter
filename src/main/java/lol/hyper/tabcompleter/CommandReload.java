package lol.hyper.tabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandReload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp() || sender.hasPermission("tabcompleter.reload")) {
            TabCompleter.getInstance().loadConfig(TabCompleter.getInstance().configFile);
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
