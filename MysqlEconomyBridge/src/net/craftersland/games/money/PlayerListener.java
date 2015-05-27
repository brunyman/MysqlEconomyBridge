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
	@SuppressWarnings("unused")
	private ConfigurationHandler coHa;

	public PlayerListener(Money money) {
		this.money = money;
		this.coHa = money.getConfigurationHandler();
	}
	
	@EventHandler
	public void onLogin(final AsyncPlayerPreLoginEvent event) {

		//Check if player has a MySQL account first
		if (!money.getMoneyDatabaseInterface().hasAccount(event.getUniqueId()))
		{
			return;
		}
		
		final OfflinePlayer playerName = Bukkit.getOfflinePlayer(event.getUniqueId());
		Double economyBalance = Money.econ.getBalance(playerName);
		
		//Set local balance to 0 before depositing the mysql balance
		if (economyBalance > 0) 
		{
			Money.econ.withdrawPlayer(playerName, economyBalance);
		}
		
		//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(money, new Runnable() {

			@Override
			public void run() {
				if (Bukkit.getPlayer(event.getUniqueId()).isOnline() == false) return;
				
				Double mysqlBalance = money.getMoneyDatabaseInterface().getBalance(event.getUniqueId());
				
				//Set mysql balance to local balance
				Money.econ.depositPlayer(playerName, mysqlBalance);
				
				if (Money.econ.getBalance(playerName) != money.getMoneyDatabaseInterface().getBalance(event.getUniqueId())) {
					Double ecoBal = Money.econ.getBalance(playerName);
					
					Money.econ.withdrawPlayer(playerName, ecoBal);
					
					if (Money.econ.getBalance(playerName) == 0) {
						Money.econ.depositPlayer(playerName, mysqlBalance);
					}
				}
			}
		}, 20L);

	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		
		OfflinePlayer p = Bukkit.getPlayer(event.getPlayer().getName());
		
		//Check if local balance is 0
		if (Money.econ.getBalance(p) == 0)
		{
			return;
		} 
		
		Double economyBalance = Money.econ.getBalance(p);
		
		//Set local balance on mysql balance
		money.getMoneyDatabaseInterface().setBalance(p.getUniqueId(), economyBalance);
		//The set local balance 0
		Money.econ.withdrawPlayer(p, economyBalance);
		
		//Double check the economy left balance and set it to 0
		if (Money.econ.getBalance(p) != 0) {
			Money.econ.withdrawPlayer(p, economyBalance);
		}

	}

}
