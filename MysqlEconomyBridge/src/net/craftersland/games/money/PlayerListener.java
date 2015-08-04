package net.craftersland.games.money;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener{
	
	private Money money;
	public static Economy econ = null;
	private int delay = 1;

	public PlayerListener(Money money) {
		this.money = money;
	}
	
	@EventHandler
	public void onLogin(final AsyncPlayerPreLoginEvent event) {
		
		delay = Integer.parseInt(money.getConfigurationHandler().getString("General.loginSyncDelay")) / 1000;
		
		//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(money, new Runnable() {

			@Override
			public void run() {
				if (money.getDatabaseManagerInterface().checkConnection() == false) return;
				final OfflinePlayer playerName = Bukkit.getOfflinePlayer(event.getUniqueId());
				try {
					if (playerName.isOnline() == false) return;
				} catch (Exception e) {
					
				}
				//Check if player has a MySQL account first
				if (!money.getMoneyDatabaseInterface().hasAccount(event.getUniqueId())) {
					money.playersSync.put(playerName.getName(), true);
					return;
				}
				//Set local balance to 0 before depositing the mysql balance
				Double economyBalance = Money.econ.getBalance(playerName);
				if (economyBalance > 0) 
				{
					Money.econ.withdrawPlayer(playerName, economyBalance);
				}
				
				final Double mysqlBalance = money.getMoneyDatabaseInterface().getBalance(event.getUniqueId());
				
				if (money.getMoneyDatabaseInterface().getBalance(event.getUniqueId()) == 0) {
					money.playersSync.put(playerName.getName(), true);
					return;
				}
				
				//Set mysql balance to local balance
				Money.econ.depositPlayer(playerName, mysqlBalance);
				
				//Check for dupe
				Bukkit.getScheduler().runTaskLaterAsynchronously(money, new Runnable() {

					@Override
					public void run() {
						if (Money.econ.getBalance(playerName) != mysqlBalance) {
							Money.econ.withdrawPlayer(playerName, Money.econ.getBalance(playerName));
							Money.econ.depositPlayer(playerName, mysqlBalance);
						}
					}
					
				}, 5L);
				
				if (money.getConfigurationHandler().getString("General.disableEconomyReset").matches("false")) {
					//Set mysql balance to 0
					money.getMoneyDatabaseInterface().setBalance(event.getUniqueId(), 0.0);
				}
				
				money.playersSync.put(playerName.getName(), true);
			}
		}, delay * 20L + 5);

	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		
		final OfflinePlayer p = Bukkit.getPlayer(event.getPlayer().getName());
		if (money.playersSync.containsKey(p.getName()) == false) return;
		Bukkit.getScheduler().runTaskLaterAsynchronously(money, new Runnable() {
			@Override
			public void run() {
				Double economyBalance = Money.econ.getBalance(p);
				//Check if local balance is 0
				if (Money.econ.getBalance(p) == 0) {
					money.playersBalance.remove(p.getUniqueId());
					money.playersSync.remove(p.getName());
					return;
				}
				
				//Set local balance on mysql balance
				money.getMoneyDatabaseInterface().setBalance(p.getUniqueId(), economyBalance);
				
				if (money.getConfigurationHandler().getString("General.disableEconomyReset").matches("false")) {
					//The set local balance 0
					Money.econ.withdrawPlayer(p, economyBalance);
					
					//Double check the economy left balance and set it to 0
					if (Money.econ.getBalance(p) != 0) {
						Money.econ.withdrawPlayer(p, economyBalance);
					}
				}
				
				money.playersBalance.remove(p.getUniqueId());
				money.playersSync.remove(p.getName());
			}
		}, 2L);

	}

}
