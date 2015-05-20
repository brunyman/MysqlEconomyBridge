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
	@SuppressWarnings("unused")
	private ConfigurationHandler coHa;

	public PlayerListener(Money money) {
		this.money = money;
		this.coHa = money.getConfigurationHandler();
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onLogin(final PlayerJoinEvent event) {

		//Check if player has a MySQL account first
		if (!money.getMoneyDatabaseInterface().hasAccount(event.getPlayer().getUniqueId()))
		{
			return;
		}
		
		Double balance = Money.econ.getBalance(event.getPlayer());
		final Player p = event.getPlayer();
		
		//Set local balance to 0 before depositing the mysql balance
		if (balance > 0) 
		{
			Money.econ.withdrawPlayer(p, balance);
		}
		
		//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(money, new Runnable() {

			@Override
			public void run() {
				
				//Set mysql balance to local balance
				Money.econ.depositPlayer(p, money.getMoneyDatabaseInterface().getBalance(p.getUniqueId()));
			}
		}, 30L);

	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		
		//Check if local balance is 0
		if (Money.econ.getBalance(event.getPlayer()) == 0)
		{
			return;
		} 
		
		Double balance = Money.econ.getBalance(event.getPlayer());
		Player p = event.getPlayer();
		
		//Set local balance on mysql balance
		money.getMoneyDatabaseInterface().setBalance(p.getUniqueId(), balance);
		//The set local balance 0
		Money.econ.withdrawPlayer(p, balance);

	}

}
