package lol.hyper.tabcompleter;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class CommandEvents implements Listener {

    private final TabCompleter tabCompleter;

    public CommandEvents(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] array = event.getMessage().split(" ");
        String command = array[0].replace("/", "");

        if (!tabCompleter.config.getStringList("commands").contains(command) && tabCompleter.config.getBoolean("block-commands-not-on-list")) {
            if (!event.getPlayer().isOp() || !event.getPlayer().hasPermission("tabcompleter.bypass")) {
                event.getPlayer().sendMessage(tabCompleter.config.getString("invalid-command-message"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandSuggestion(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            if (!player.hasPermission("tabcompleter.bypass")) {
                event.getCommands().retainAll(tabCompleter.config.getStringList("commands"));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().updateCommands();
    }
}
