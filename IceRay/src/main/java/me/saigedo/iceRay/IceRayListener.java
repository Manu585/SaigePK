package me.saigedo.iceRay;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.PlayerSwingEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IceRayListener implements Listener {

    @EventHandler
    public void onSwing(PlayerSwingEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (bPlayer == null) return;

        if (!bPlayer.getBoundAbilityName().equalsIgnoreCase("FrostBreath")) return;

        IceRay ability = CoreAbility.getAbility(player, IceRay.class);
        if (ability != null) {
            ability.setContinueMove(true); // flip your boolean so progress() knows to run
        }
    }
}
