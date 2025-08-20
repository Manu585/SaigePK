package me.hiro3.terrasense;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;


public class UtilityMethods {

    public static void addGlowAll(TerraSense ts) {
        for (LivingEntity le : ts.getGlowingEntities()) {
            setGlowing(le, ts.getPlayer(), true);
        }
    }

    public static void removeGlowAll(TerraSense ts) {
        for (LivingEntity le : ts.getGlowingEntities()) {
            setGlowing(le, ts.getPlayer(), false);
        }
    }

    public static void setGlowing(LivingEntity glowingEntity, Player sendPacketPlayer, boolean glow) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);

        packet.getIntegers().write(0, glowingEntity.getEntityId());

        //StructureModifier<List<WrappedDataValue>> watchableAccessor = packet.getDataValueCollectionModifier();

        List<WrappedDataValue> values = Lists.newArrayList(
                new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) (glow ? 0x40 : 0)));

        // Insert and read back
        //watchableAccessor.write(0, values);

        // Set the entity's data watcher (metadata) in the packet.
        packet.getDataValueCollectionModifier().write(0, values);

        // Send the packet to the player using ProtocolLib's ProtocolManager.
        ProtocolLibrary.getProtocolManager().sendServerPacket(sendPacketPlayer, packet);
    }
    //magma invis ekle
    public static void sendGlowingBlock(Player p, Location loc, long lifetime){
        if (CoreAbility.hasAbility(p, TerraSense.class)
                && !CoreAbility.getAbility(p, TerraSense.class).canSense()) {
            return;
        }

        PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        int entityId = new Random().nextInt(Integer.MAX_VALUE / 2) * -1;

        spawnPacket.getIntegers().write(0, entityId);
        spawnPacket.getUUIDs().write(0, UUID.randomUUID());
        spawnPacket.getEntityTypeModifier().write(0, EntityType.MAGMA_CUBE);

        spawnPacket.getDoubles()
                .write(0, loc.getX())
                .write(1, loc.getY())
                .write(2, loc.getZ());

        PacketContainer glowPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        glowPacket.getIntegers().write(0, entityId);
        List<WrappedDataValue> glowValue = Lists.newArrayList(
                new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) (0x60)));
        glowPacket.getDataValueCollectionModifier().write(0, glowValue);

        ProtocolLibrary.getProtocolManager().sendServerPacket(p, spawnPacket);
        ProtocolLibrary.getProtocolManager().sendServerPacket(p, glowPacket);

        Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, () -> {
            PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

            destroyPacket.getModifier().write(0, new IntArrayList(new int[]{entityId}));

            ProtocolLibrary.getProtocolManager().sendServerPacket(p, destroyPacket);
        },  lifetime);
    }

    public static void sendPulse(Player player, Location center, double maxRadius, int step, int angleIncrease) {

        new BukkitRunnable() {

            double angle;
            double radius = 0;
            double speed = maxRadius/step;

            @Override
            public void run() {

                if (radius >= maxRadius) {
                    Bukkit.getScheduler().cancelTask(getTaskId());
                    return;
                }

                radius += speed;
                for(int i = 0; i <= 360; i+=angleIncrease) {
                    angle = Math.toRadians(i);
                    center.add(radius * Math.cos(angle), 0, radius * Math.sin(angle));
                    if (TerraSense.senseOnlyEarthBlocks) {
                        if (EarthAbility.isEarthbendable(player, center.getBlock()))
                            sendGlowingBlock(player, center, 1);
                    } else {
                        if (GeneralMethods.isSolid(center.getBlock()))
                            sendGlowingBlock(player, center, 1);
                    }
                    center.subtract(radius * Math.cos(angle), 0, radius * Math.sin(angle));
                }
            }

        }.runTaskTimer(ProjectKorra.plugin, 0, 1);

    }

    public static void sendPulseImproved(Player player, Location center, double maxRadius, int step, int angleIncrease) {
        Vector startingVector = new Vector(maxRadius/step, 0, 0);
        Location startingLocation = center;

        ArrayList<Location> l = new ArrayList<Location>();
        ArrayList<Vector> d = new ArrayList<Vector>();
        for (int i = 0; i < 360; i += angleIncrease) {
            l.add(startingLocation.clone());
            d.add(UtilityMethods.rotateVectorAroundY(startingVector, i));
        }

        new BukkitRunnable() {

            int tick = 0;

            @Override
            public void run() {
                if (tick >= step) {
                    Bukkit.getScheduler().cancelTask(getTaskId());
                    return;
                }

                if (TerraSense.senseOnlyEarthBlocks) {
                    for (int i = 0; i < l.size(); i++) {
                        if (EarthAbility.isEarthbendable(player, l.get(i).getBlock())) {
                            UtilityMethods.sendGlowingBlock(player, l.get(i), 1);
                        }
                        if (EarthAbility.isEarthbendable(player, l.get(i).getBlock().getRelative(BlockFace.UP))) {
                            l.get(i).add(0, 1, 0);
                        } else {
                            l.get(i).add(d.get(i));
                            if (EarthAbility.isEarthbendable(player, l.get(i).getBlock().getRelative(BlockFace.UP))) {
                                l.get(i).add(0, 1, 0);
                            } else {
                                l.set(i, UtilityMethods.getFloor(player, l.get(i)));
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < l.size(); i++) {
                        if (GeneralMethods.isSolid(l.get(i).getBlock())) {
                            UtilityMethods.sendGlowingBlock(player, l.get(i), 1);
                        }
                        if (GeneralMethods.isSolid(l.get(i).getBlock().getRelative(BlockFace.UP))) {
                            l.get(i).add(0, 1, 0);
                        } else {
                            l.get(i).add(d.get(i));
                            if (GeneralMethods.isSolid(l.get(i).getBlock().getRelative(BlockFace.UP))) {
                                l.get(i).add(0, 1, 0);
                            } else {
                                l.set(i, UtilityMethods.getFloor(player, l.get(i)));
                            }
                        }
                    }
                }

                tick++;
            }

        }.runTaskTimer(ProjectKorra.plugin, 0, 1);
    }

    public static Location getFloor(Player player, Location loc) {
        Location f = loc.clone();
        double sqrMaxHeight = 30 * 30;
        if (TerraSense.senseOnlyEarthBlocks) {
            while (!EarthAbility.isEarthbendable(player, loc.getBlock()) && loc.distanceSquared(f) < sqrMaxHeight) {
                loc.add(0, -1, 0);
            }
        } else {
            while (!GeneralMethods.isSolid(loc.getBlock()) && loc.distanceSquared(f) < sqrMaxHeight) {
                loc.add(0, -1, 0);
            }
        }
        return loc;
    }

    public static void refreshAllTerraSenses() {
        for (TerraSense ts : CoreAbility.getAbilities(TerraSense.class)) {
            addGlowAll(ts);
        }
    }

    public static Vector rotateVectorAroundY(Vector vector, double degrees) {
        double rad = Math.toRadians(degrees);

        double currentX = vector.getX();
        double currentZ = vector.getZ();

        double cosine = Math.cos(rad);
        double sine = Math.sin(rad);

        return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
    }

    public static String getVersion() {
        return "4.0";
    }

}
