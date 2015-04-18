package net.craftersland.games.money;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import net.craftersland.games.money.database.AccountDatabaseInterface;
import net.craftersland.games.money.database.DatabaseManagerInterface;
import net.craftersland.games.money.database.DatabaseManagerMysql;
import net.craftersland.games.money.database.MoneyMysqlInterface;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Money extends JavaPlugin {
	
	public static Logger log;
	public static Economy econ = null;
	public static ExecutorService execService = null;
	
	private ConfigurationHandler configurationHandler;
	private DatabaseManagerInterface databaseManager;
	private AccountDatabaseInterface<Double> moneyDatabaseInterface;
	
	@Override
    public void onEnable(){
    	log = getLogger();
    	log.info("Loading MysqlEconomyBridge "+getDescription().getVersion()+"... ");
    	
    	//Create MysqlEconomyBridge folder
    	(new File("plugins"+System.getProperty("file.separator")+"MysqlEconomyBridge")).mkdir();
    	
    	
    	//Setup Vault for economy
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled! Vault installed? If yes Economy system installed?)", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    	
    	//Load Configuration
        configurationHandler = new ConfigurationHandler(this);
        
      //Initiate Threadpool
        execService = Executors.newFixedThreadPool(Integer.parseInt(configurationHandler.getString("database.maximumThreads")));
        
      //Setup Database
        	log.info("Using MySQL as Datasource...");
        	databaseManager = new DatabaseManagerMysql(this);
        	moneyDatabaseInterface = new MoneyMysqlInterface(this);
        	
        	if (databaseManager.setupDatabase() == false)
        	{
        		getServer().getPluginManager().disablePlugin(this);
                return;
        	}
          
      //Register Listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerListener(this), this);
    	
    	log.info("MysqlEconomyBridge has been successfully loaded!");
	}
	
	@Override
    public void onDisable() {
    	log.info("MysqlEconomyBridge has been disabled");
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
    public AccountDatabaseInterface<Double> getMoneyDatabaseInterface() {
    	return moneyDatabaseInterface;
    }
    
    public ConfigurationHandler getConfigurationHandler() {
		return configurationHandler;
	}
    
    public DatabaseManagerInterface getDatabaseManagerInterface() {
		return databaseManager;
	}

}
