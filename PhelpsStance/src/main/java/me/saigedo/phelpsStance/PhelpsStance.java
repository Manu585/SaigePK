package me.saigedo.phelpsStance;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.StanceAbility;
import com.projectkorra.projectkorra.chiblocking.passive.ChiAgility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PhelpsStance extends ChiAbility implements StanceAbility,AddonAbility {

    public PhelpsStance(Player player) {
        super(player);
        if (!this.bPlayer.canBend(this)){
            return;
        }

        final StanceAbility stance = this.bPlayer.getStance();
        if (stance instanceof CoreAbility) {
            ((CoreAbility)stance).remove();
            if (stance instanceof PhelpsStance) {
                this.bPlayer.setStance(null);
                return;
            }
        }
        this.start();
        this.bPlayer.setStance(this);
        player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 0.5F, 2F);
    }


    @Override
    public void progress() {
        if (!this.bPlayer.canBendIgnoreBinds(this) || !this.bPlayer.hasElement(Element.CHI)) {
            this.remove();
            return;
        }

        // Apply your stance effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 10, 0, true, false), true);
    }
    @Override
    public void remove() {

        super.remove();
        this.bPlayer.addCooldown(this);
        this.bPlayer.setStance(null);
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.5F, 2F);
        this.player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }

    @Override
    public long getCooldown() {
        return 0;
    }

    @Override
    public Location getLocation() {
        return null;
    }


    public void load() {
        Bukkit.getPluginManager().registerEvents(new AbilityListener(), ProjectKorra.plugin);

        ProjectKorra.log.info("PhelpsStance 1.0 Loaded");

    }


    public void stop() {

    }


    public String getAuthor() {
        return "Saigedo";
    }


    public String getVersion() {
        return "1.0";
    }
    @Override
    public String getName() {
        return "PhelpsStance";
    }

    @Override
    public String getInstructions() {
        return "Left Click to toggle stance";
    }

    @Override
    public String getDescription() {
        return "Swim Faster, Devoloped by Jerry Phelps";
    }

    @Override
    public String getStanceName() {
        return "PhelpsStance";
    }
}
