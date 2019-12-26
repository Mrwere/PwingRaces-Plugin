package net.pwing.races.race.trigger.passives;

import net.pwing.races.PwingRaces;
import net.pwing.races.api.race.trigger.RaceTriggerPassive;
import net.pwing.races.util.NumberUtil;
import org.bukkit.entity.Player;

public class DamageTrigger extends RaceTriggerPassive {

    private PwingRaces plugin;

    public DamageTrigger(PwingRaces plugin, String name) {
        super(name);

        this.plugin = plugin;
    }

    @Override
    public void runTriggerPassive(Player player, String trigger) {
        String[] split = trigger.split(" ");
        if (split.length < 2)
            return;

        double damage = 60;
        if (NumberUtil.isDouble(split[1]))
            damage = Double.parseDouble(split[1]);

        player.damage(damage);
    }
}
