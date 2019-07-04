package net.pwing.races.race.attribute;

import net.pwing.races.PwingRaces;
import net.pwing.races.api.events.RaceChangeEvent;
import net.pwing.races.api.events.RaceElementPurchaseEvent;
import net.pwing.races.api.events.RaceExpChangeEvent;
import net.pwing.races.api.race.RaceManager;
import net.pwing.races.api.race.RacePlayer;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.inventory.ItemStack;

public class RaceAttributeListener implements Listener {

    private PwingRaces plugin;

    public RaceAttributeListener(PwingRaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getRaceManager().getAttributeManager().applyAttributeBonuses(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getRaceManager().getAttributeManager().removeAttributeBonuses(event.getPlayer());
    }

    @EventHandler
    public void onRaceChangeEvent(RaceChangeEvent event) {
        plugin.getRaceManager().getAttributeManager().removeAttributeBonuses(event.getPlayer());
    }

    @EventHandler
    public void onRaceExpChange(RaceExpChangeEvent event) {
        plugin.getRaceManager().getAttributeManager().applyAttributeBonuses(event.getPlayer());
    }

    @EventHandler
    public void onRaceElementPurchase(RaceElementPurchaseEvent event) {
        plugin.getRaceManager().getAttributeManager().removeAttributeBonuses(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.getRaceManager().getAttributeManager().applyAttributeBonuses(event.getPlayer());
    }

    @EventHandler
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        RaceManager raceManager = plugin.getRaceManager();
        Player player = (Player) arrow.getShooter();
        if (!raceManager.isRacesEnabledInWorld(player.getWorld()))
            return;

        RacePlayer racePlayer = raceManager.getRacePlayer(player);
        if (racePlayer == null)
            return;

        if (racePlayer.getActiveRace() == null)
            return;

        double bonus = raceManager.getAttributeManager().getAttributeBonus(player, "arrow-damage");
        plugin.getCompatCodeHandler().setDamage(arrow, plugin.getCompatCodeHandler().getDamage(arrow) + bonus);
    }

    @EventHandler
    public void onMeleeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        RaceManager raceManager = plugin.getRaceManager();
        Player player = (Player) event.getDamager();
        if (!raceManager.isRacesEnabledInWorld(player.getWorld()))
            return;

        RacePlayer racePlayer = raceManager.getRacePlayer(player);
        if (racePlayer == null)
            return;

        if (racePlayer.getActiveRace() == null)
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;

        double bonus = raceManager.getAttributeManager().getAttributeBonus(player, "melee-damage");

        ItemStack hand = plugin.getCompatCodeHandler().getItemInMainHand(player);
        if (hand != null && hand.getType().name().contains("SWORD")) {
            bonus += raceManager.getAttributeManager().getAttributeBonus(player, "swords-damage");
        }

        if (hand != null && hand.getType().name().contains("_AXE")) {
            bonus += raceManager.getAttributeManager().getAttributeBonus(player, "axes-damage");
        }

        event.setDamage(event.getDamage() + bonus);
    }
}
