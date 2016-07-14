package net.craftersland.eco.bridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.craftersland.eco.bridge.database.EcoMysqlHandler;
import net.craftersland.eco.bridge.database.MysqlSetup;
import net.craftersland.eco.bridge.events.PlayerDisconnect;
import net.craftersland.eco.bridge.events.PlayerJoin;
import net.craftersland.eco.bridge.events.PlayerLogin;
import net.craftersland.eco.bridge.events.handlers.EcoDataHandler;
import net.craftersland.eco.bridge.utils.DataSaveTask;
import net.craftersland.eco.bridge.utils.EcoUpdateTask;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Eco extends JavaPlugin {
	
	public static Logger log;
	public static Economy vault = null;
	public boolean isDisabling = false;
	public Map<Player, Integer> syncCompleteTasks = new HashMap<Player, Integer>();
	
	private ConfigHandler configHandler;
	private MysqlSetup mysqlSetup;
	private EcoMysqlHandler ecoMysqlHandler;
	private EcoDataHandler edH;
	private DataSaveTask dst;
	private EcoUpdateTask eut;
	private boolean isEnabled = false;
	
	@Override
    public void onEnable() {
		log = getLogger();
    	log.info("Loading MysqlEcoBridge v"+getDescription().getVersion()+"... ");
    	
    	//Setup Vault for economy
        if (setupEconomy() == false) {
            log.severe("Warning! - Vault installed? If yes Economy system installed?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    	
    	//Load Configuration
        configHandler = new ConfigHandler(this);
        //Setup MySQL
        mysqlSetup = new MysqlSetup(this);
        ecoMysqlHandler = new EcoMysqlHandler(this);
        edH = new EcoDataHandler(this);
        dst = new DataSaveTask(this);
        eut = new EcoUpdateTask(this);
        
        if (mysqlSetup.getConnection() == null)
    	{
    		getServer().getPluginManager().disablePlugin(this);
            return;
    	}
        
        //Register Listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerLogin(this), this);
    	pm.registerEvents(new PlayerJoin(this), this);
    	pm.registerEvents(new PlayerDisconnect(this), this);
    	
    	isEnabled = true;
    	log.info("MysqlEcoBridge has been successfully loaded!");
	}
	
	@Override
    public void onDisable() {
		isDisabling = true;
		if (isEnabled == true) {
			onShutDownDataSave();
			getMysqlSetup().closeConnection();
		} else if (getMysqlSetup().getConnection() != null) {
			log.info("Closing MySQL connection...");
			getMysqlSetup().closeConnection();
		}
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		log.info("MysqlEcoBridge has been disabled");
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vault = rsp.getProvider();
        log.info("Using economy system: " + rsp.getProvider().getName());
        return vault != null;
    }
	
	public ConfigHandler getConfigHandler() {
		return configHandler;
	}
	public MysqlSetup getMysqlSetup() {
		return mysqlSetup;
	}
	public EcoMysqlHandler getEcoMysqlHandler() {
		return ecoMysqlHandler;
	}
	public EcoDataHandler getEcoDataHandler() {
		return edH;
	}
	public DataSaveTask getDataSaveTask() {
		return dst;
	}
	public EcoUpdateTask getEcoUpdateTask() {
		return eut;
	}
	
	private void onShutDownDataSave() {
		Eco.log.info("Saving online players data...");
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		
		for (Player p : onlinePlayers) {
			if (p.isOnline() == true) {
				getEcoDataHandler().onDataSaveFunction(p, true, "true");
			}
		}
		Eco.log.info("Data save complete for " + onlinePlayers.size() + " players.");
	}

}
