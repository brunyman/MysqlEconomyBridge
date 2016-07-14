package net.craftersland.games.money;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener{
	
	private Money money;
	public static Economy econ = null;
	private int delay = 1;

	public PlayerListener(Money money) {
		this.money = money;
	}
	
	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		
		delay = Integer.parseInt(money.getConfigurationHandler().getString("General.loginSyncDelay")) / 1000;
		final Player p = event.getPlayer();
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(money, new Runnable() {

			@Override
			public void run() {
				
				if (money.getDatabaseManagerInterface().checkConnection() == false) {
					Money.log.warning("Database connection issue...");
					return;
				} else if (p.isOnline() == false) {
					return;
				}
				
				//Check if player has a MySQL account first
				if (money.getMoneyDatabaseInterface().hasAccount(p.getUniqueId()) == false) {
					money.playersSync.put(p.getName(), true);
					return;
				} else {
					if (ecoReset(p) == false) {
						Money.log.warning("Error on resetting player " + p.getName() + " balance! Report the issue to plugin dev.");
						return;
					}
				}
				
				//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
				Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(money, new Runnable() {

					@Override
					public void run() {
						
						Double mysqlBalance = money.getMoneyDatabaseInterface().getBalance(p.getUniqueId());
						
						if (mysqlBalance == 0) {
							money.playersSync.put(p.getName(), true);
							return;
						} else {
							//Set mysql balance to local balance
							if (Money.econ.getBalance(p) != 0) {
								ecoReset(p);
							}
							Money.econ.depositPlayer(p, mysqlBalance);
						}
						
						if (money.getConfigurationHandler().getString("General.disableEconomyReset").matches("false")) {
							//Set mysql balance to 0
							money.getMoneyDatabaseInterface().setBalance(p.getUniqueId(), 0.0);
						}
						
						money.playersSync.put(p.getName(), true);
					}
				}, delay * 20L);
			}
			
		}, 5L);

	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		
		final Player p = event.getPlayer();
		
		if (money.playersSync.containsKey(p.getName()) == false) return;
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(money, new Runnable() {
			@Override
			public void run() {
				Double economyBalance = Money.econ.getBalance(p);
				
				if (economyBalance == 0) {
					money.playersBalance.remove(p.getUniqueId());
					money.playersSync.remove(p.getName());
				} else {
					money.getMoneyDatabaseInterface().setBalance(p.getUniqueId(), economyBalance);
					
					if (money.getConfigurationHandler().getString("General.disableEconomyReset").matches("false")) {
						Money.econ.withdrawPlayer(p, economyBalance);
					}
					money.playersBalance.remove(p.getUniqueId());
					money.playersSync.remove(p.getName());
				}
				
			}
		}, 2L);

	}
	
	private boolean ecoReset(Player p) {
		
		Double balance = Money.econ.getBalance(p);
		
		Money.econ.withdrawPlayer(p, balance);
		
		Double newBalance = Money.econ.getBalance(p);
		
		if (newBalance != 0) {
			return false;
		} else {
			return true;
		}
	}

}
