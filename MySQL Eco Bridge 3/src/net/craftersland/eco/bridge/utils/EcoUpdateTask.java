package net.craftersland.eco.bridge.utils;

import org.bukkit.Bukkit;

import net.craftersland.eco.bridge.Eco;

public class EcoUpdateTask {
	
	private Eco eco;
	
	public EcoUpdateTask(Eco eco) {
		this.eco = eco;
		updateTask();
	}
	
	private void updateTask() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(eco, new Runnable() {

			@Override
			public void run() {
				if (eco.isDisabling == false) {
					eco.getEcoDataHandler().updateBalanceMap();
				}
			}
			
		}, 10L, 20L);
	}

}
