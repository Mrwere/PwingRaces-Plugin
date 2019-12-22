package net.pwing.races.race.skilltree;

import lombok.Getter;

import net.pwing.races.api.race.skilltree.RaceSkilltree;
import net.pwing.races.api.race.skilltree.RaceSkilltreeManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class PwingRaceSkilltreeManager implements RaceSkilltreeManager {

    private List<RaceSkilltree> skilltrees;

    public PwingRaceSkilltreeManager(File dir) {
        skilltrees = new ArrayList<>();
        initSkilltrees(dir);
    }

    public void initSkilltrees(File dir) {
        if (dir == null || !dir.isDirectory())
            return;

        for (File file : dir.listFiles()) {
            if (!file.getName().endsWith(".yml"))
                continue;

            initSkilltree(file.getName().replace(".yml", ""), YamlConfiguration.loadConfiguration(file));
        }
    }

    public void initSkilltree(String regName, FileConfiguration config) {
        skilltrees.add(new PwingRaceSkilltree(regName, config));
    }
}
