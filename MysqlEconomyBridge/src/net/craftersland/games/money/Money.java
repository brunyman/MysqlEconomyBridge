package net.craftersland.games.money;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import net.craftersland.games.money.database.DatabaseManagerMysql;
import net.craftersland.games.money.database.MoneyMysqlInterface;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Money extends JavaPlugin {
	
	public static Logger log;
	public static Economy econ = null;
	public static String pluginName = "MysqlEconomyBridge";
	public HashMap<UUID, Double> playersBalance = new HashMap<UUID, Double>();
	public HashMap<String, Boolean> playersSync = new HashMap<String, Boolean>();
	
	private static ConfigurationHandler configurationHandler;
	private static DatabaseManagerMysql dmm;
	private static MoneyMysqlInterface mmi;
	private static BackgroundTask bt;
	
	@Override
    public void onEnable(){
    	log = getLogger();   	
    	//Setup Vault for economy
        if (!setupEconomy() ) {
            log.severe("Warning! - Vault installed? If yes Economy system installed?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    	//Load Configuration
        configurationHandler = new ConfigurationHandler(this);
        //Setup Database
        dmm = new DatabaseManagerMysql(this);
        mmi = new MoneyMysqlInterface(this);
        bt = new BackgroundTask(this);
          
        //Register Listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerListener(this), this);
    	log.info(pluginName + " loaded successfully!");
	}
	
	@Override
    public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		if (dmm.getConnection() != null) {
			bt.saveDataOnShutdown();
			dmm.closeConnection();
		}
		log.info(pluginName + " is disabled!");
    }
	
	//Methods for setting up Vault
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        log.info("Using economy system: " + rsp.getProvider().getName());
        return econ != null;
    }
    
    //Getter for Database Interfaces
    public DatabaseManagerMysql getDatabaseManagerMysql() {
    	return dmm;
    }
    public ConfigurationHandler getConfigurationHandler() {
		return configurationHandler;
	}
    public MoneyMysqlInterface getMoneyMysqlInterface() {
    	return mmi;
    }
    public BackgroundTask getBackgroundTask() {
    	return bt;
    }

}
