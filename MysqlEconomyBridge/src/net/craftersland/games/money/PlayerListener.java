package net.craftersland.games.money;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
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

		//Check if player has mysql an account first
		if (!money.getMoneyDatabaseInterface().hasAccount(event.getPlayer().getUniqueId()))
		{
			return;
		}
		//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(money, new Runnable() {

			@Override
			public void run() {
				
				//Set local balance to 0 before depositing the mysql balance
				if (Money.econ.getBalance(event.getPlayer()) > 0) 
				{
					Money.econ.withdrawPlayer(event.getPlayer(), Money.econ.getBalance(event.getPlayer()));
				}
				
				//Set mysql balance to local balance
				Money.econ.depositPlayer(event.getPlayer(), money.getMoneyDatabaseInterface().getBalance(event.getPlayer().getUniqueId()));
				
			}
		}, 20L);

	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		
		//Check if local balance is 0
		if (Money.econ.getBalance(event.getPlayer()) == 0)
		{
			return;
		}
		//Check if player has account and if no create it
		if (!money.getMoneyDatabaseInterface().hasAccount(event.getPlayer().getUniqueId()))
		{
			money.getMoneyDatabaseInterface().createAccount(event.getPlayer().getUniqueId());
		}
		//Set local balance on mysql balance
		money.getMoneyDatabaseInterface().setBalance(event.getPlayer().getUniqueId(), Money.econ.getBalance(event.getPlayer()));

	}

}
