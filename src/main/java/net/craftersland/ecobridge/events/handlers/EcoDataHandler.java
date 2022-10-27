package net.craftersland.ecobridge.events.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.craftersland.ecobridge.Eco;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class EcoDataHandler {
	
	private Eco eco;
	private Map<Player, Double> backupMoney = new HashMap<Player, Double>();
	private Map<Player, Double> balanceMap = new HashMap<Player, Double>();
	private Map<Player, Integer> runningTasks = new HashMap<Player, Integer>();
	private Set<Player> playersInSync = new HashSet<Player>();
	
	public EcoDataHandler(Eco eco) {
		this.eco = eco;
	}
	
	public void onShutDownDataSave() {
		Eco.log.info("Saving online players data...");
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		
		for (Player p : onlinePlayers) {
			if (p.isOnline()) {
				onDataSaveFunction(p, true, "true", true);
			}
		}
		Eco.log.info("Data save complete for " + onlinePlayers.size() + " players.");
	}
	
	public void updateBalanceMap() {
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		if (!onlinePlayers.isEmpty()) {
			for (Player p : onlinePlayers) {
				if (playersInSync.contains(p)) {
					balanceMap.put(p, Eco.vault.getBalance(p));
				}
			}
		}
	}
	
	public boolean isSyncComplete(Player p) {
		if (playersInSync.contains(p)) {
			return true;
		} else {
			return false;
		}
	}
	
	private void dataCleanup(Player p, Boolean isDisabling) {
		if (!isDisabling) {
			playersInSync.remove(p);
			backupMoney.remove(p);
			balanceMap.remove(p);
			if (runningTasks.containsKey(p)) {
				Bukkit.getScheduler().cancelTask(runningTasks.get(p));
				runningTasks.remove(p);
			}
		}
	}
	
	private void setPlayerData(final Player p, String[] data, boolean cancelTask) {
		try {
			Double bal = Eco.vault.getBalance(p);
			
			if (bal != null) {
				if (bal != 0.0) {
					Eco.vault.withdrawPlayer(p, bal);
				}
				Double mysqlBal = Double.parseDouble(data[0]);
				Double localBal = Eco.vault.getBalance(p);
				if (mysqlBal >= localBal) {
					double finalBalance = mysqlBal - localBal;
					Eco.vault.depositPlayer(p, finalBalance);
				} else if (mysqlBal < localBal) {
					double finalBalance = localBal - mysqlBal;
					Eco.vault.withdrawPlayer(p, finalBalance);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Double backupBalance = backupMoney.get(p);
			if (backupBalance != 0.0) {
				Double bal = Eco.vault.getBalance(p);
				if (bal != null) {
					if (bal != 0.0) {
						Eco.vault.withdrawPlayer(p, bal);
					}
					Double localBal = Eco.vault.getBalance(p);
					if (backupBalance >= localBal) {
						double finalBalance = backupBalance - localBal;
						Eco.vault.depositPlayer(p, finalBalance);
					} else if (backupBalance < localBal) {
						double finalBalance = localBal - backupBalance;
						Eco.vault.depositPlayer(p, finalBalance);
					}
				}
			}
		}
		eco.getEcoMysqlHandler().setSyncStatus(p, "false");
		playersInSync.add(p);
		backupMoney.remove(p);
		if (cancelTask) {
			int taskID = runningTasks.get(p);
			runningTasks.remove(p);
			Bukkit.getScheduler().cancelTask(taskID);
		}
	}
	
	public void onDataSaveFunction(Player p, Boolean datacleanup, String syncStatus, Boolean isDisabling) {
		boolean isPlayerInSync = playersInSync.contains(p);
		if (!isDisabling) {
			if (datacleanup) {
				dataCleanup(p, isDisabling);
			}
			if (isPlayerInSync) {
				eco.getEcoMysqlHandler().setData(p, Eco.vault.getBalance(p), syncStatus);
			}
		} else {
			if (isPlayerInSync) {
				if (balanceMap.containsKey(p)) {
					eco.getEcoMysqlHandler().setData(p, balanceMap.get(p), syncStatus);
				}
			}
		}
	}
	
	public void onJoinFunction(final Player p) {
		if (eco.getEcoMysqlHandler().hasAccount(p)) {
			final double balance = Eco.vault.getBalance(p);
			backupMoney.put(p, balance);
			Bukkit.getScheduler().runTask(eco, new Runnable() {

				@Override
				public void run() {
					if (balance != 0.0) {
						Eco.vault.withdrawPlayer(p, balance);
					}
					String[] data = eco.getEcoMysqlHandler().getData(p);
					if (data[1].matches("true")) {
						setPlayerData(p, data, false);
					} else {
						final long taskStart = System.currentTimeMillis();
						BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(eco, () -> {
							if (p.isOnline()) {
								final String[] data1 = eco.getEcoMysqlHandler().getData(p);
								Bukkit.getScheduler().runTask(eco, () -> {
									if (data1[1].matches("true")) {
										setPlayerData(p, data1, true);
									} else if (System.currentTimeMillis() - Long.parseLong(data1[2]) >= 15 * 1000) {
										setPlayerData(p, data1, true);
									}
								});
							}
							if (System.currentTimeMillis() - taskStart >= 10 * 1000) {
								int taskID = runningTasks.get(p);
								runningTasks.remove(p);
								Bukkit.getScheduler().cancelTask(taskID);
							}
						}, 20L, 20L);
						runningTasks.put(p, task.getTaskId());
					}
				}
				
			});
		} else {
			playersInSync.add(p);
			onDataSaveFunction(p, false, "false", false);
		}
	}

}
