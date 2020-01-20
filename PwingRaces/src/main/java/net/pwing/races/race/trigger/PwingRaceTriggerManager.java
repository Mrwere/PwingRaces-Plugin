package net.pwing.races.race.trigger;

import lombok.AccessLevel;
import lombok.Getter;

import net.pwing.races.PwingRaces;
import net.pwing.races.api.race.Race;
import net.pwing.races.api.race.RaceData;
import net.pwing.races.api.race.RaceManager;
import net.pwing.races.api.race.RacePlayer;
import net.pwing.races.api.race.skilltree.RaceSkilltree;
import net.pwing.races.api.race.trigger.RaceTrigger;
import net.pwing.races.api.race.trigger.RaceTriggerManager;
import net.pwing.races.api.race.trigger.RaceTriggerPassive;
import net.pwing.races.api.race.trigger.condition.RaceCondition;
import net.pwing.races.race.trigger.passive.*;
import net.pwing.races.race.trigger.trigger.InRegionTrigger;
import net.pwing.races.race.trigger.trigger.SneakTrigger;
import net.pwing.races.util.math.NumberUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class PwingRaceTriggerManager implements RaceTriggerManager {

    @Getter(AccessLevel.NONE)
    private PwingRaces plugin;

    private Map<String, RaceTriggerPassive> triggerPassives = new HashMap<>();
    private Map<String, RaceCondition> conditions = new HashMap<>();

    private Map<String, Map<UUID, Long>> delay = new HashMap<>();

    public PwingRaceTriggerManager(PwingRaces plugin) {
        this.plugin = plugin;

        initTriggerPassives();
        Bukkit.getServer().getPluginManager().registerEvents(new RaceTriggerListener(plugin), plugin);
    }

    public void initTriggerPassives() {
        triggerPassives.put("add-potion-effect", new AddPotionEffectTriggerPassive(plugin, "add-potion-effect"));
        triggerPassives.put("allow-flight", new AllowFlightTriggerPassive(plugin, "allow-flight"));
        triggerPassives.put("burn", new BurnTriggerPassive(plugin, "burn"));
        triggerPassives.put("damage", new DamageTriggerPassive(plugin, "damage"));
        triggerPassives.put("disguise", new DisguiseTriggerPassive(plugin, "disguise"));
        triggerPassives.put("drop-item", new DropItemTriggerPassive(plugin, "drop-item"));
        triggerPassives.put("give-exp", new GiveExpTriggerPassive(plugin, "give-exp"));
        triggerPassives.put("give-health", new GiveHealthTriggerPassive(plugin, "give-health"));
        triggerPassives.put("give-race-exp", new GiveRaceExpTriggerPassive(plugin, "give-race-exp"));
        triggerPassives.put("give-saturation", new GiveSaturationTriggerPassive(plugin, "give-saturation"));
        triggerPassives.put("reapply-attributes", new ReapplyAttributesTriggerPassive(plugin, "reapply-attributes"));
        triggerPassives.put("remove-potion-effect", new RemovePotionEffectTriggerPassive(plugin, "remove-potion-effect"));
        triggerPassives.put("run-command", new RunCommandTriggerPassive(plugin, "run-command"));
        triggerPassives.put("send-actionbar-message", new SendActionBarMessageTriggerPassive("send-action-bar-message"));
        triggerPassives.put("send-message", new SendMessageTriggerPassive(plugin, "send-message"));
        triggerPassives.put("set-attribute", new SetAttributeTriggerPassive(plugin, "set-attribute"));
        triggerPassives.put("set-default-attributes", new SetDefaultAttributesTriggerPassive(plugin, "set-default-attributes"));
        triggerPassives.put("toggle-fly", new ToggleFlyTriggerPassive(plugin, "toggle-fly"));
        triggerPassives.put("undisguise", new UndisguiseTriggerPassive(plugin, "undisguise"));

        registerCondition("in-region", new InRegionTrigger(plugin));
        registerCondition("sneak", new SneakTrigger(this));
    }

    // TODO: configure for multiple conditions
    public void runTriggers(Player player, String trigger) {
        if (!plugin.getRaceManager().isRacesEnabledInWorld(player.getWorld()))
            return;

        Collection<RaceTrigger> raceTriggers = getApplicableTriggers(player, trigger);
        if (raceTriggers == null || raceTriggers.isEmpty())
            return;

        for (RaceTrigger raceTrigger : raceTriggers) {
            if (hasDelay(player, raceTrigger.getInternalName()))
                continue;

            setDelay(player, raceTrigger.getInternalName(), raceTrigger.getDelay());

            // Run chance afterward so it doesnt idle
            if ((ThreadLocalRandom.current().nextFloat() * 100) > raceTrigger.getChance())
                continue;

            // Run task synchronously
            Bukkit.getScheduler().runTask(plugin, () -> runTriggerPassives(player, raceTrigger));
        }
    }

    public void runTaskTriggers(Player player, String trigger, int tick) {
        if (!trigger.startsWith("ticks "))
            return;

        Random random = new Random();
        for (RaceTrigger raceTrigger : getApplicableTaskTriggers(player)) {
            if (hasDelay(player, raceTrigger.getInternalName())) {
                continue;
            }

            if (!NumberUtil.isInteger(raceTrigger.getTrigger().split(" ")[1])) {
                plugin.getLogger().warning("Could not properly parse trigger " + raceTrigger.getTrigger() + ", expected a number but got " + trigger.split(" ")[1] + "");
                continue;
            }

            int tickDelay = Integer.parseInt(raceTrigger.getTrigger().split(" ")[1]);
            if (tick % tickDelay == 0) {
                setDelay(player, raceTrigger.getInternalName(), raceTrigger.getDelay());

                // Run chance afterward so it doesnt idle
                if ((random.nextFloat() * 100) > raceTrigger.getChance())
                    continue;

                Bukkit.getScheduler().runTask(plugin, () -> runTriggerPassives(player, raceTrigger));
            }
        }
    }

    public Collection<RaceTrigger> getApplicableTriggers(Player player, String trigger) {
        RaceManager raceManager = plugin.getRaceManager();
        RacePlayer racePlayer = raceManager.getRacePlayer(player);
        if (racePlayer == null)
            return new ArrayList<>();

        if (!racePlayer.getRace().isPresent())
            return new ArrayList<>();

        Race race = racePlayer.getRace().get();
        RaceData data = raceManager.getPlayerData(player, race);

        Map<String, RaceTrigger> triggers = new HashMap<>();
        for (String key : race.getRaceTriggersMap().keySet()) {
            List<RaceTrigger> definedTriggers = race.getRaceTriggersMap().get(key);

            for (RaceTrigger definedTrigger : definedTriggers) {
                String req = definedTrigger.getRequirement();

                if (req.equals("none")) {
                    triggers.put(definedTrigger.getInternalName(), definedTrigger);

                } else if (req.startsWith("level")) { // best to assume it's a level-based trigger
                    int level = Integer.parseInt(req.replace("level", ""));

                    if (data.getLevel() < level)
                        continue;

                    triggers.put(definedTrigger.getInternalName(), definedTrigger);
                } else {
                    for (RaceSkilltree skillTree : raceManager.getSkilltreeManager().getSkilltrees()) {
                        if (data.hasPurchasedElement(skillTree.getInternalName(), req)) {
                            triggers.put(definedTrigger.getInternalName(), definedTrigger);
                        }
                    }
                }
            }
        }

        // Remove triggers that may be overridden
        List<String> toRemove = new ArrayList<>();
        for (String str : triggers.keySet()) {
            RaceTrigger raceTrigger = triggers.get(str);

            if (!raceTrigger.getTrigger().equalsIgnoreCase(trigger)) {
                toRemove.add(str);
            }
        }

        toRemove.forEach(triggers::remove);
        return triggers.values();
    }

    public Collection<RaceTrigger> getApplicableTaskTriggers(Player player) {
        RaceManager raceManager = plugin.getRaceManager();
        RacePlayer racePlayer = raceManager.getRacePlayer(player);
        if (racePlayer == null)
            return new ArrayList<>();

        if (!racePlayer.getRace().isPresent())
            return new ArrayList<>();

        Race race = racePlayer.getRace().get();
        RaceData data = raceManager.getPlayerData(player, race);

        Map<String, RaceTrigger> triggers = new HashMap<>();
        for (String key : race.getRaceTriggersMap().keySet()) {
            List<RaceTrigger> definedTriggers = race.getRaceTriggersMap().get(key);

            for (RaceTrigger definedTrigger : definedTriggers) {
                String req = definedTrigger.getRequirement();

                if (req.equals("none")) {
                    triggers.put(definedTrigger.getInternalName(), definedTrigger);

                } else if (req.startsWith("level")) { // best to assume it's a level-based trigger
                    int level = Integer.parseInt(req.replace("level", ""));

                    if (data.getLevel() < level)
                        continue;

                    triggers.put(definedTrigger.getInternalName(), definedTrigger);
                } else {
                    for (RaceSkilltree skillTree : raceManager.getSkilltreeManager().getSkilltrees()) {
                        if (data.hasPurchasedElement(skillTree.getInternalName(), req)) {
                            triggers.put(definedTrigger.getInternalName(), definedTrigger);
                        }
                    }
                }
            }
        }

        // Remove triggers that may be overridden
        List<String> toRemove = new ArrayList<>();
        for (String str : triggers.keySet()) {
            RaceTrigger raceTrigger = triggers.get(str);

            if (!raceTrigger.getTrigger().startsWith("ticks ")) {
                toRemove.add(str);
            }
        }

        toRemove.forEach(triggers::remove);
        return triggers.values();
    }

    public void runTriggerPassives(Player player, RaceTrigger trigger) {
        runTriggerPassives(player, trigger.getPassives());
    }

    public void runTriggerPassives(Player player, List<RaceTriggerPassive> triggers) {
        triggerPassives.values().forEach(racePassive -> racePassive.runPassive(player, racePassive.getName()));
    }

    public boolean hasDelay(Player player, String trigger) {
        if (!delay.containsKey(trigger))
            return false;

        if (!delay.get(trigger).containsKey(player.getUniqueId()))
            return false;

        return delay.get(trigger).get(player.getUniqueId()) >= System.currentTimeMillis();
    }

    public void setDelay(Player player, String trigger, int amt) {
        Map<UUID, Long> delayMap = delay.getOrDefault(trigger, new HashMap<>());
        delayMap.put(player.getUniqueId(), (amt * 1000) + System.currentTimeMillis());
        delay.put(trigger, delayMap);
    }

    private void registerCondition(String internalName, RaceCondition condition) {
        conditions.put(internalName, condition);
        if (condition instanceof Listener)
            plugin.getServer().getPluginManager().registerEvents((Listener) condition, plugin);
    }
}
