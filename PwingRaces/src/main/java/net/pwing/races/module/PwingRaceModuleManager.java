package net.pwing.races.module;

import lombok.AllArgsConstructor;

import net.pwing.races.api.module.RaceModule;
import net.pwing.races.api.module.RaceModuleManager;

import java.util.Map;

@AllArgsConstructor
public class PwingRaceModuleManager implements RaceModuleManager {

    private PwingRaceModuleLoader loader;

    @Override
    public void enableModule(RaceModule module) {
        loader.enableModule(module);
    }

    @Override
    public void disableModule(RaceModule module) {
        loader.disableModule(module);
    }

    @Override
    public Map<String, RaceModule> getModules() {
        return loader.modules;
    }
}
