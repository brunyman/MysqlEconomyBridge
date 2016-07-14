package net.craftersland.eco.bridge;

import java.io.File;

public class ConfigHandler {
	
	private Eco eco;
	
	public ConfigHandler(Eco eco) {
		this.eco = eco;
		
		//Create MysqlEconomyBridge folder
    	(new File("plugins"+System.getProperty("file.separator")+"MysqlEcoBridge")).mkdir();
		
    	
    	if (!(new File("plugins"+System.getProperty("file.separator")+"MysqlEcoBridge"+System.getProperty("file.separator")+"config.yml").exists())) {
    		Eco.log.info("No config file found! Creating new one...");
    		eco.saveDefaultConfig();
		}
		try {
			eco.getConfig().load(new File("plugins"+System.getProperty("file.separator")+"MysqlEcoBridge"+System.getProperty("file.separator")+"config.yml"));
		} catch (Exception e) {
			Eco.log.info("Could not load config file!");
			e.printStackTrace();
		}
	}
	
	public String getString(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Could not locate '"+key+"' in the config.yml inside of the MysqlEcoBridge folder! (Try generating a new one by deleting the current)");
			return "errorCouldNotLocateInConfigYml:"+key;
		} else {
			if (key.toLowerCase().contains("color")) {
				return "§"+eco.getConfig().getString(key);
			}
			return eco.getConfig().getString(key);
		}
	}
	
	public Integer getInteger(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Could not locate '"+key+"' in the config.yml inside of the MysqlEcoBridge folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return eco.getConfig().getInt(key);
		}
	}
	
	public Boolean getBoolean(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Could not locate '"+key+"' in the config.yml inside of the MysqlEcoBridge folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return eco.getConfig().getBoolean(key);
		}
	}

}
