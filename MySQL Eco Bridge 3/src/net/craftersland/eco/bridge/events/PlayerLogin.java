package net.craftersland.eco.bridge.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import net.craftersland.eco.bridge.Eco;

public class PlayerLogin implements Listener {
	
	private Eco eco;
	
	public PlayerLogin(Eco eco) {
		this.eco = eco;
	}
	
	@EventHandler
	public void onLogin(final PlayerLoginEvent event) {
		if (eco.isDisabling == false) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(eco, new Runnable() {

				@Override
				public void run() {
					if (event.getResult().toString().equals("ALLOWED")) {
						if (event.getPlayer() != null) {
							Player p = event.getPlayer();
							if (p.isOnline() == true) {
								eco.getEcoDataHandler().onLoginFunction(p);
							}
						}
					}
				}
				
			}, 1L);
		}
	}

}
