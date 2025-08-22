package me.saigedo.beTheLeaf;

import com.projectkorra.projectkorra.BendingPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

public class AbilityListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only continue if it’s a left click in air or on a block
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());

            if (bPlayer != null && bPlayer.getBoundAbilityName() != null) {
                if (bPlayer.getBoundAbilityName().equals("BeTheLeaf")) {
                    new BeTheLeaf(event.getPlayer());
                }
            }
        }
    }
}