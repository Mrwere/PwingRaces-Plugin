package net.pwing.races.race.trigger.passive;

import net.pwing.races.PwingRaces;
import net.pwing.races.api.race.RacePlayer;
import net.pwing.races.api.race.trigger.RaceTriggerPassive;

import org.bukkit.entity.Player;

public class SetDefaultAttributesTriggerPassive extends RaceTriggerPassive {

    private PwingRaces plugin;

    public SetDefaultAttributesTriggerPassive(PwingRaces plugin, String name) {
        super(name);

        this.plugin = plugin;
    }

    @Override
    public void runTriggerPassive(Player player, String trigger) {
        RacePlayer racePlayer = plugin.getRaceManager().getRacePlayer(player);
        if (!racePlayer.hasRace())
            return;

        plugin.getRaceManager().getAttributeManager().removeAttributeBonuses(player);
    }
}