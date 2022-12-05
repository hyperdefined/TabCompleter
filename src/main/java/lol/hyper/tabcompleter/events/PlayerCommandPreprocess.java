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

package lol.hyper.tabcompleter.events;

import lol.hyper.tabcompleter.TabCompleter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocess implements Listener {

    private final TabCompleter tabCompleter;

    public PlayerCommandPreprocess(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!tabCompleter.config.getBoolean("actually-block-command-execution")) {
            return;
        }

        // convert whatever was typed into an array and only get the first element
        // remove the slash too
        String[] array = event.getMessage().split(" ");
        String command = array[0].replace("/", "");

        Player player = event.getPlayer();
        String[] playerGroups = tabCompleter.permission.getPlayerGroups(player);

        // ignore if the player can bypass
        if (player.hasPermission("tabcompleter.bypass") || player.isOp()) {
            return;
        }

        // block the command!
        for (String playerGroup : playerGroups) {
            boolean matched = tabCompleter.groupCommands.get(playerGroup).stream().anyMatch(command::equalsIgnoreCase);
            if (!matched) {
                Component message = tabCompleter.getMessage("invalid-command-message");
                tabCompleter.getAdventure().player(player).sendMessage(message);
                event.setCancelled(true);
                return;
            }
        }
    }
}
