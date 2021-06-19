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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class PlayerCommandSend implements Listener {

    private final TabCompleter tabCompleter;

    public PlayerCommandSend(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandSuggestion(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            if (!player.hasPermission("tabcompleter.bypass")) {

                // Clear ALL of the commands and add ours.
                // This is technically the wrong way to do it. The API says no to adding here but it works ¯\_(ツ)_/¯
                event.getCommands().clear();
                event.getCommands().addAll(tabCompleter.config.getStringList("commands"));
            }
        }
    }
}
