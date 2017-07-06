package net.craftersland.games.money;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener{
	
	private Money money;

	public PlayerListener(Money money) {
		this.money = money;
	}
	
	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		Bukkit.getScheduler().runTaskLaterAsynchronously(money, new Runnable() {

			@Override
			public void run() {
				if (p.isOnline() == false) {
					return;
				}
				//Check if player has a MySQL account first
				if (money.getMoneyMysqlInterface().hasAccount(p.getUniqueId()) == false) {
					money.playersSync.put(p.getName(), true);
					return;
				} else {
					if (ecoReset(p) == false) {
						Money.log.warning("Error on resetting player " + p.getName() + " balance! Report the issue to plugin dev.");
						return;
					}
				}
				Double mysqlBalance = money.getMoneyMysqlInterface().getBalance(p.getUniqueId());
				
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
				money.playersSync.put(p.getName(), true);
			}
			
		}, money.getConfigurationHandler().getInteger("General.loginSyncDelay") / 1000 * 20L);

	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		if (money.playersSync.containsKey(event.getPlayer().getName()) == true) {
			final Player p = event.getPlayer();
			Bukkit.getScheduler().runTaskLaterAsynchronously(money, new Runnable() {
				@Override
				public void run() {
					Double economyBalance = Money.econ.getBalance(p);
					if (economyBalance == 0) {
						money.playersBalance.remove(p.getUniqueId());
						money.playersSync.remove(p.getName());
					} else {
						money.getMoneyMysqlInterface().setBalance(p.getUniqueId(), economyBalance);
						money.playersBalance.remove(p.getUniqueId());
						money.playersSync.remove(p.getName());
					}
					
				}
			}, 2L);
		}

	}
	
	private boolean ecoReset(Player p) {
		Money.econ.withdrawPlayer(p, Money.econ.getBalance(p));
		if (Money.econ.getBalance(p) != 0) {
			return false;
		} else {
			return true;
		}
	}

}
