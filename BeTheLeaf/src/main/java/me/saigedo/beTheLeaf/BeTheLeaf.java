package me.saigedo.beTheLeaf;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;



public class BeTheLeaf extends AirAbility implements StanceAbility,AddonAbility {

    private int speedboost;
    private int jumpboost;



    public BeTheLeaf(Player player) {
        super(player);

        this.speedboost = getConfig().getInt("Abilities.Air.BeTheLeaf.Speed", 1);
        this.jumpboost = getConfig().getInt("Abilities.Air.BeTheLeaf.Jump", 1);



        if (!this.bPlayer.canBend(this)){
            return;
        }

        final StanceAbility stance = this.bPlayer.getStance();
        if (stance instanceof CoreAbility) {
            ((CoreAbility)stance).remove();
            if (stance instanceof BeTheLeaf) {
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
        if (!this.bPlayer.canBendIgnoreBinds(this) || !this.bPlayer.hasElement(Element.AIR)) {
            this.remove();
            return;
        }

        // Apply your stance effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 10, jumpboost, true, false), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, speedboost, true, false), true);
    }
    @Override
    public void remove() {

        super.remove();
        this.bPlayer.addCooldown(this);
        this.bPlayer.setStance(null);
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.5F, 2F);
        this.player.removePotionEffect(PotionEffectType.SPEED);
        this.player.removePotionEffect(PotionEffectType.JUMP_BOOST);

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

        ProjectKorra.log.info("BeTheLeaf 2.0 Loaded");

    }


    public void stop() {

    }


    public String getAuthor() {
        return "Saigedo";
    }


    public String getVersion() {
        return "2.0";
    }
    @Override
    public String getName() {
        return "BeTheLeaf";
    }

    @Override
    public String getInstructions() {
        return "Left Click to toggle stance.";
    }

    @Override
    public String getDescription() {
        return "Toggles air passive to always be running even without sprinting.";
    }

    @Override
    public String getStanceName() {
        return "BeTheLeaf";
    }
}
