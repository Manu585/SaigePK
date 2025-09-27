package me.saigedo.iceRay;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public final class IceRay extends WaterAbility implements AddonAbility, ComboAbility {
    private final long cooldown;
    private final int range;
    private final double speed;
    private final double radius;
    private final long revertIce;

    private int count;

    private boolean continueMove;
    private Location origin;
    private Location location;
    private Vector direction;

    public IceRay(Player player) {
        super(player);

        // Config Options
        int sourceRange = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Saigedo.IceRay.SourceRange", 6);
        this.cooldown = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Saigedo.IceRay.Cooldown",5000);
        this.range = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Saigedo.IceRay.Range",20);
        this.speed = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Saigedo.IceRay.Speed",4);
        this.radius = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Saigedo.IceRay.Radius",6);
        this.revertIce = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Saigedo.IceRay.revertIce",6000);

        if (!this.bPlayer.canBendIgnoreBinds(this)) {
            return;
        }

        Block sourceBlock = BlockSource.getWaterSourceBlock(this.player, sourceRange, ClickType.SHIFT_DOWN, true, true, this.bPlayer.canPlantbend());
        if (!isWater(sourceBlock)) return;

        this.direction = this.player.getEyeLocation().getDirection();
        this.origin = sourceBlock.getLocation().add(0,1,0);
        this.location = this.origin.clone();
        this.bPlayer.addCooldown(this);

        start();
    }
    @Override
    public void progress() {
        if (!continueMove) {
            count++;
            player.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, location, 5, 0, 1, 0, 0.5);

            if (count > 40) {
                remove();
                return;
            }
            return;
        }

        this.bPlayer.addCooldown(this);
        this.direction = this.player.getEyeLocation().getDirection();
        this.location.add(this.direction.normalize().multiply(speed));

        // Leads the location, used to follow land
        Location lead = this.location.clone().add(this.direction.clone().normalize());
        player.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, location, 5, 1, 1, 1, 0.5);

        for (Block block : GeneralMethods.getBlocksAroundPoint(this.location, this.radius)) {
            Block above = block.getRelative(BlockFace.UP);

            if (block.getType().isSolid() && GeneralMethods.isTransparent(above)) {
                TempBlock tempBlock = new TempBlock(above, Material.SNOW.createBlockData());
                tempBlock.setBendableSource(true);
                tempBlock.setRevertTime(revertIce);
            }

            if (block.getType() == Material.WATER) {
                TempBlock tempBlock = new TempBlock(block, Material.ICE.createBlockData());
                tempBlock.setBendableSource(true);
                tempBlock.setRevertTime(revertIce);
            }

            if (block.getType() == Material.LAVA) {
                player.getLocation().add(0, 1, 0).getWorld().spawnParticle(Particle.CLOUD, location, 20, Math.random(), Math.random(), Math.random(), .3);
                remove();
                return;
            }
        }

        if (GeneralMethods.isSolid(this.location.getBlock())) {
            remove();
            return;
        }

        if (this.location.distance(this.origin) > this.range) {
            remove();
        }

        // Terrain follower
        if (this.location.clone().add(0,-1,0).getBlock().getType().isAir()) {
            if (this.location.clone().add(0,-2,0).getBlock().getType().isAir()) {
             remove();
             return;
            }
            this.location.add(0,-1,0);
        }

        if (GeneralMethods.isSolid(lead.getBlock())) {
            if (!GeneralMethods.isSolid(lead.add(0,1,0).getBlock())){
                this.location.add(0,1,0);
            }
        }
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(new IceRayListener(), ProjectKorra.plugin);

        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Saigedo.IceRay.SourceRange", 6);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Saigedo.IceRay.Cooldown", 5000);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Saigedo.IceRay.Range", 20);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Saigedo.IceRay.Speed", 4);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Saigedo.IceRay.Radius", 6);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Saigedo.IceRay.revertIce", 6000);

        ConfigManager.defaultConfig.save();
    }

    @Override
    public void stop() {}

    @Override
    public Object createNewComboInstance(Player player) {
        return new IceRay(player);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        ArrayList<ComboManager.AbilityInformation> combo = new ArrayList<>();
        combo.add(new ComboManager.AbilityInformation("PhaseChange", ClickType.SHIFT_DOWN));
        combo.add(new ComboManager.AbilityInformation("PhaseChange", ClickType.SHIFT_UP));
        combo.add(new ComboManager.AbilityInformation("PhaseChange", ClickType.SHIFT_DOWN));
        combo.add(new ComboManager.AbilityInformation("PhaseChange", ClickType.SHIFT_UP));
        combo.add(new ComboManager.AbilityInformation("PhaseChange", ClickType.SHIFT_DOWN));

        return combo;
    }

    @Override
    public String getAuthor() {
        return "Saigedo";
    }

    @Override
    public String getName() {
        return "IceRay";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Send out a line of snow";
    }

    @Override
    public String getInstructions() {
        return """
                PhaseChange (Tap Sneak on Water Source) ->\s
                PhaseChange (Tap Sneak on Water Source) ->\s
                PhaseChange (Hold Sneak on a Water Source) ->\s
                FrostBreath (Left Click)""";
    }

    public void setContinueMove(boolean continueMove) {
        this.continueMove = continueMove;
    }
}
