package net.pwing.races.race;

import java.util.Map;

import net.pwing.races.api.race.Race;
import net.pwing.races.api.race.RaceData;
import net.pwing.races.api.race.RacePlayer;
import org.bukkit.OfflinePlayer;

public class PwingRacePlayer implements RacePlayer {

	private OfflinePlayer player;
	private Race activeRace;
	
	private Map<String, RaceData> raceDataMap;
	
	public PwingRacePlayer(OfflinePlayer player, Race activeRace, Map<String, RaceData> raceDataMap) {
		this.player = player;
		this.activeRace = activeRace;
		this.raceDataMap = raceDataMap;
	}
	 
	public OfflinePlayer getPlayer() {
		return player;
	}
	
	public Race getActiveRace() {
		return activeRace;
	}
	
	public void setActiveRace(Race activeRace) {
		this.activeRace = activeRace;
	}
	
	public RaceData getRaceData(Race race) {
		return raceDataMap.get(race.getName());
	}

	public RaceData getActiveRaceData() {
		return getRaceData(activeRace);
	}

	public Map<String, RaceData> getRaceDataMap() {
		return raceDataMap;
	}
}