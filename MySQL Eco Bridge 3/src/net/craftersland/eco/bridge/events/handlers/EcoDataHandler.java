package net.craftersland.eco.bridge.events.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.craftersland.eco.bridge.Eco;

public class EcoDataHandler {
	
	private Eco eco;
	private Map<Player, Double> backupMoney = new HashMap<Player, Double>();
	private Map<Player, Double> balanceMap = new HashMap<Player, Double>();
	private Map<Player, Integer> runningTasks = new HashMap<Player, Integer>();
	private Set<Player> playersOnDatabase = new HashSet<Player>();
	private Set<Player> playersInSync = new HashSet<Player>();
	private Set<Player> loginComplete = new HashSet<Player>();
	
	public EcoDataHandler(Eco eco) {
		this.eco = eco;
	}
	
	public void updateBalanceMap() {
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		if (onlinePlayers.isEmpty() == false) {
			for (Player p : onlinePlayers) {
				if (playersInSync.contains(p) == true) {
					balanceMap.put(p, Eco.vault.getBalance(p));
				}
			}
		}
	}
	
	public boolean isSyncComplete(Player p) {
		if (playersInSync.contains(p) == true) {
			return true;
		} else {
			return false;
		}
	}
	
	private void dataCleanup(Player p) {
		if (eco.isDisabling == false) {
			playersInSync.remove(p);
			playersOnDatabase.remove(p);
			backupMoney.remove(p);
			balanceMap.remove(p);
			loginComplete.remove(p);
			if (runningTasks.containsKey(p) == true) {
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
					Double finalBalance = mysqlBal - localBal;
					Eco.vault.depositPlayer(p, finalBalance);
				} else if (mysqlBal < localBal) {
					Double finalBalance = localBal - mysqlBal;
					Eco.vault.depositPlayer(p, finalBalance);
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
						Double finalBalance = backupBalance - localBal;
						Eco.vault.depositPlayer(p, finalBalance);
					} else if (backupBalance < localBal) {
						Double finalBalance = localBal - backupBalance;
						Eco.vault.depositPlayer(p, finalBalance);
					}
				}
			}
		}
		eco.getEcoMysqlHandler().setSyncStatus(p, "false");
		playersInSync.add(p);
		backupMoney.remove(p);
		playersOnDatabase.remove(p);
		if (cancelTask == true) {
			int taskID = runningTasks.get(p);
			runningTasks.remove(p);
			Bukkit.getScheduler().cancelTask(taskID);
		}
	}
	
	public void onDataSaveFunction(Player p, Boolean datacleanup, String syncStatus) {
		boolean isPlayerInSync = playersInSync.contains(p);
		if (eco.isDisabling == false) {
			if (datacleanup == true) {
				dataCleanup(p);
			}
			if (isPlayerInSync == true) {
				eco.getEcoMysqlHandler().setData(p, Eco.vault.getBalance(p), syncStatus);
			}
		} else {
			if (isPlayerInSync == true) {
				if (balanceMap.containsKey(p) == true) {
					eco.getEcoMysqlHandler().setData(p, balanceMap.get(p), syncStatus);
				}
			}
		}
	}
	
	public void onJoinFunction(final Player p) {
		boolean hasAccount = true;
		if (loginComplete.contains(p) == true) {
			hasAccount = playersOnDatabase.contains(p);
		} else {
			hasAccount = eco.getEcoMysqlHandler().hasAccount(p);
		}
		loginComplete.remove(p);
		if (hasAccount == true) {
			String[] data = eco.getEcoMysqlHandler().getData(p);
			if (data[1].matches("true")) {
				setPlayerData(p, data, false);
			} else {
				final long taskStart = System.currentTimeMillis();
				BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(eco, new Runnable() {

					@Override
					public void run() {
						if (p.isOnline() == true) {
							String[] data = eco.getEcoMysqlHandler().getData(p);
							if (data[2].matches("true")) {
								setPlayerData(p, data, true);
							} else if (System.currentTimeMillis() - Long.parseLong(data[2]) >= 15 * 1000) {
								setPlayerData(p, data, true);
							}
						}
						if (System.currentTimeMillis() - taskStart >= 10 * 1000) {
							int taskID = runningTasks.get(p);
							runningTasks.remove(p);
							Bukkit.getScheduler().cancelTask(taskID);
						}
					}
					
				}, 20L, 20L);
				runningTasks.put(p, task.getTaskId());
			}
		} else {
			playersInSync.add(p);
		}
	}
	
	public void onLoginFunction(Player p) {
		if (eco.getEcoMysqlHandler().hasAccount(p) == true) {
			playersOnDatabase.add(p);
			double balance = Eco.vault.getBalance(p);
			backupMoney.put(p, balance);
			Eco.vault.withdrawPlayer(p, balance);
			loginComplete.add(p);
		}
	}

}
