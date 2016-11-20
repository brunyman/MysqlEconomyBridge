package net.craftersland.eco.bridge.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import net.craftersland.eco.bridge.Eco;

public class PlayerDisconnect implements Listener {
	
	private Eco eco;
	
	public PlayerDisconnect(Eco eco) {
		this.eco = eco;
	}
	
	@EventHandler
	public void onDisconnect(final PlayerQuitEvent event) {
		Bukkit.getScheduler().runTaskLaterAsynchronously(eco, new Runnable() {

			@Override
			public void run() {
				if (event.getPlayer() != null) {
					Player p = event.getPlayer();
					cleanup(p);
					eco.getEcoDataHandler().onDataSaveFunction(p, true, "true", false);
				}
			}
			
		}, 1L);
	}
	
	private void cleanup(Player p) {
		if (eco.syncCompleteTasks.containsKey(p) == true) {
			Bukkit.getScheduler().cancelTask(eco.syncCompleteTasks.get(p));
			eco.syncCompleteTasks.remove(p);
		}
	}

}
