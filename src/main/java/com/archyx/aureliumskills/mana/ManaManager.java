package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.ManaRegenerateEvent;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.stats.Stats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ManaManager implements Listener {

    private final AureliumSkills plugin;

    public ManaManager(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    /**
     * Start regenerating Mana
     */
    public void startRegen() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData != null) {
                        double originalMana = playerData.getMana();
                        double maxMana = playerData.getMaxMana();
                        if (originalMana < maxMana) {
                            if (!playerData.getAbilityData(MAbility.ABSORPTION).getBoolean("activated")) {
                                double regen = OptionL.getDouble(Option.REGENERATION_BASE_MANA_REGEN) + playerData.getStatLevel(Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_MANA_MODIFIER);
                                double finalRegen = Math.min(originalMana + regen, maxMana) - originalMana;
                                ManaRegenerateEvent event = new ManaRegenerateEvent(player, finalRegen);
                                Bukkit.getPluginManager().callEvent(event);
                                if (!event.isCancelled()) {
                                    playerData.setMana(originalMana + event.getAmount());
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

}
