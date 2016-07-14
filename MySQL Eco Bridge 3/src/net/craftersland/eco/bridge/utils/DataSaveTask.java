package net.craftersland.eco.bridge.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.craftersland.eco.bridge.Eco;

public class DataSaveTask {
	
	private Eco eco;
	
	public DataSaveTask(Eco eco) {
		this.eco = eco;
		saveTask();
	}
	
	private void saveTask() {
		boolean taskEnabled = eco.getConfigHandler().getBoolean("General.saveDataTask.enabled");
		if (taskEnabled == true) {
			Eco.log.info("Data save task is enabled.");
			int time = eco.getConfigHandler().getInteger("General.saveDataTask.interval") * 60;
			Bukkit.getScheduler().runTaskTimerAsynchronously(eco, new Runnable() {

				@Override
				public void run() {
					if (eco.isDisabling == false) {
						if (eco.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages") == false) {
							Eco.log.info("Saving online players data...");
						}
						List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
						for (Player p : onlinePlayers) {
							if (p.isOnline() == true) {
								eco.getEcoDataHandler().onDataSaveFunction(p, false, "false");
							}
						}
						if (eco.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages") == false) {
							Eco.log.info("Data save complete for " + onlinePlayers.size() + " players.");
						}
					}
				}
				
			}, time * 20L, time * 20L);
		} else {
			Eco.log.info("Data save task is disabled.");
		}
	}

}
