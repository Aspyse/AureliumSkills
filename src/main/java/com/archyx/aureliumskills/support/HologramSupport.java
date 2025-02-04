package com.archyx.aureliumskills.support;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

public class HologramSupport implements Listener {

    private final AureliumSkills plugin;
    private final Random r = new Random();
    private final NumberFormat nf;

    public HologramSupport(AureliumSkills plugin) {
        this.plugin = plugin;
        nf = new DecimalFormat("#." + StringUtils.repeat("#", OptionL.getInt(Option.DAMAGE_HOLOGRAMS_DECIMAL_MAX)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!event.isCancelled()) {
            if (event.getEntity() instanceof LivingEntity) {
                if (plugin.isHolographicDisplaysEnabled()) {
                    if (OptionL.getBoolean(Option.DAMAGE_HOLOGRAMS)) {
                        if (plugin.getWorldManager().isInDisabledWorld(event.getEntity().getLocation())) {
                            return;
                        }
                        if (event.getDamager() instanceof Player) {
                            Player player = (Player) event.getDamager();
                            if (player.hasMetadata("skillsCritical")) {
                                //If only critical
                                createHologram(new StringBuilder(ChatColor.RED + "crit!").toString(), event.getEntity(), player);
                                createHologram(getText(event.getFinalDamage(), true), event.getEntity(), player);
                            } else {
                                //If none
                                createHologram(getText(event.getFinalDamage(), false), event.getEntity(), player);

                            }
                        } else if (event.getDamager() instanceof Arrow) {
                            Arrow arrow = (Arrow) event.getDamager();
                            if (arrow.getShooter() instanceof Player) {
                                Player player = (Player) arrow.getShooter();
                                if (player.hasMetadata("skillsCritical")) {
                                    createHologram(new StringBuilder(ChatColor.RED + "crit!").toString(), event.getEntity(), player);
                                    createHologram(getText(event.getFinalDamage(), true), event.getEntity(), player);
                                } else {
                                    createHologram(getText(event.getFinalDamage(), false), event.getEntity(), player);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Location getLocation(Location location, Location playerLocation) {
        double distance = playerLocation.distance(location);
        double fixedDistance = 2.5;
        double factor = fixedDistance / distance;

        if (OptionL.getBoolean(Option.DAMAGE_HOLOGRAMS_OFFSET_RANDOM_ENABLED)) {
            //Calculate random holograms
            double xMin = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_RANDOM_X_MIN);
            double xMax = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_RANDOM_X_MAX);
            double x = xMin + (xMax - xMin) * r.nextDouble();
            double yMin = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_RANDOM_Y_MIN);
            double yMax = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_RANDOM_Y_MAX);
            double y = yMin + (yMax - yMin) * r.nextDouble();
            double zMin = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_RANDOM_Z_MIN);
            double zMax = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_RANDOM_Z_MAX);
            double z = zMin + (zMax - zMin) * r.nextDouble();
            location.add(x, y, z);
        }
        else {
            double x = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_X);
            x += (location.getX() - playerLocation.getX()) * factor;
            double y = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_Y);
            y += (location.getY() - playerLocation.getY()) * factor;
            double z = OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_OFFSET_Z);
            z += (location.getZ() - playerLocation.getZ()) * factor;
            playerLocation.add(x, y, z);
        }
        return playerLocation;
    }

    private String getText(double damage, boolean critical) {
        StringBuilder text = new StringBuilder(ChatColor.GRAY + "");
        String damageText;
        if (OptionL.getBoolean(Option.DAMAGE_HOLOGRAMS_SCALING)) {
            double damageScaling = damage * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
            if (damageScaling < OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_DECIMAL_LESS_THAN)) {
                damageText = nf.format(damageScaling);
            }
            else {
                damageText = "" + (int) (damageScaling);
            }
        }
        else {
            if (damage < OptionL.getDouble(Option.DAMAGE_HOLOGRAMS_DECIMAL_LESS_THAN)) {
                damageText = nf.format(damage);
            }
            else {
                damageText = "" + (int) damage;
            }
        }
        if (critical) {
            text.append(ChatColor.RED).append(damageText);
        }
        else {
            text.append(damageText);
        }
        return text.toString();
    }

    private void createHologram(String text, Entity target, Player player) {
        Location location = target.getLocation();
        location.add(0.0, target.getHeight() - target.getHeight() * 0.1, 0.0);
        Location playerLocation = player.getLocation();

        int maxTicks = 18;
        double bounceHeight = 0.8;
        double travel = 0.8;
        double velX = (2*Math.random() - 1) * travel/maxTicks;
        double velY = 1 / maxTicks;
        double velZ = (2*Math.random() - 1) * travel/maxTicks;

        Hologram hologram = HologramsAPI.createHologram(plugin, getLocation(location, playerLocation));
        hologram.appendTextLine(text);

        new BukkitRunnable() {
            int ticks;
            Location playerLocation;
            Location hologramLocation;

            @Override
            public void run() { 
                playerLocation = player.getLocation();
                hologramLocation = getLocation(location, playerLocation);
                hologram.teleport(hologramLocation.add(velX*ticks, (1-(velY*ticks)) * ((4*bounceHeight*ticks)/maxTicks), velZ*ticks));

                if (ticks > maxTicks) {
                    hologram.delete();
                    cancel();
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
