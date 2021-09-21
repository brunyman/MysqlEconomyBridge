package net.craftersland.ecobridge.events;

import net.craftersland.ecobridge.Eco;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

public class PlayerJoin implements Listener {
	
	private Eco eco;
	
	public PlayerJoin(Eco eco) {
		this.eco = eco;
	}
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLaterAsynchronously(eco, new Runnable() {

			@Override
			public void run() {
				if (event.getPlayer() != null) {
					if (event.getPlayer().isOnline()) {
						Player p = event.getPlayer();
						eco.getEcoDataHandler().onJoinFunction(p);
						syncCompleteTask(p);
					}
				}
			}
			
		}, 5L);
	}
	
	private void syncCompleteTask(final Player p) {
		if (p != null) {
			if (p.isOnline()) {
				final long startTime = System.currentTimeMillis();
				BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(eco, new Runnable() {

					@Override
					public void run() {
						if (p.isOnline()) {
							if (eco.getEcoDataHandler().isSyncComplete(p)) {
								if (eco.syncCompleteTasks.containsKey(p)) {
									int taskID = eco.syncCompleteTasks.get(p);
									eco.syncCompleteTasks.remove(p);
									Bukkit.getScheduler().cancelTask(taskID);
								}
							} else {
								if (System.currentTimeMillis() - startTime >= 10 * 1000) {
									if (eco.syncCompleteTasks.containsKey(p)) {
										int taskID = eco.syncCompleteTasks.get(p);
										eco.syncCompleteTasks.remove(p);
										Bukkit.getScheduler().cancelTask(taskID);
									}
								}
							}
						}
					}
					
				}, 5L, 20L);
				eco.syncCompleteTasks.put(p, task.getTaskId());
			}
		}
	}

}
