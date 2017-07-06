package net.craftersland.games.money;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BackgroundTask {
	
	private Money m;
	private long lastSave = System.currentTimeMillis();
	
	public BackgroundTask(Money m) {
		this.m = m;
		runTask();
	}
	
	public void saveDataOnShutdown() {
		if (m.playersBalance.isEmpty() == false) {
			if (Bukkit.getOnlinePlayers().isEmpty() == false) {
				Money.log.info("Saving players data...");
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (m.playersBalance.containsKey(player.getUniqueId()) == true) {
						double bal = m.playersBalance.get(player.getUniqueId());
						if (bal != 0.0) {
							m.getMoneyMysqlInterface().setBalance(player.getUniqueId(), bal);
						}
					}
				}
			}
		}
	}
	
	private void runTask() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(m, new Runnable() {

			@Override
			public void run() {
				updateLiveData();
				runSaveData();
			}
			
		}, 20L, 20L);
	}
	
	private void runSaveData() {
		if (m.getConfigurationHandler().getBoolean("General.dataSaveTask.enabled") == true) {
			if (Bukkit.getOnlinePlayers().isEmpty() == false) {
				if (System.currentTimeMillis() - lastSave >= m.getConfigurationHandler().getInteger("General.dataSaveTask.interval") * 60 * 1000) {
					Money.log.info("Saving online players data...");
					for (Player p : Bukkit.getOnlinePlayers()) {
						Double localBalance = Money.econ.getBalance(p);
						if (localBalance != 0) {
							m.getMoneyMysqlInterface().setBalance(p.getUniqueId(), localBalance);
						}
					}
					lastSave = System.currentTimeMillis();
					Money.log.info("Data save is complete!");
				}
			}
		}
	}
	
	private void updateLiveData() {
		if (Bukkit.getOnlinePlayers().isEmpty() == false) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (m.playersSync.containsKey(player.getName()) == true) {
					m.playersBalance.put(player.getUniqueId(), Money.econ.getBalance(player));
				}
			}
		}
 	}

}
