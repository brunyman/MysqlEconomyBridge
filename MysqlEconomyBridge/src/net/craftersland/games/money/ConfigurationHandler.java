package net.craftersland.games.money;

import java.io.File;

public class ConfigurationHandler {
	
	private Money money;

	public ConfigurationHandler(Money money) {
		this.money = money;
		if (!(new File("plugins"+System.getProperty("file.separator")+"MysqlEconomyBridge"+System.getProperty("file.separator")+"config.yml").exists())) {
			Money.log.info("No config file found! Creating new one...");
			money.saveDefaultConfig();
		}
		try {
			money.getConfig().load(new File("plugins"+System.getProperty("file.separator")+"MysqlEconomyBridge"+System.getProperty("file.separator")+"config.yml"));
		} catch (Exception e) {
			Money.log.info("Could not load config file!");
			e.printStackTrace();
		}
	}
	
	public String getString(String key) {
		if (!money.getConfig().contains(key)) {
			money.getLogger().severe("Could not locate '"+key+"' in the config.yml inside of the MysqlEconomyBridge folder! (Try generating a new one by deleting the current)");
			return "errorCouldNotLocateInConfigYml:"+key;
		} else {
			if (key.toLowerCase().contains("color")) {
				return "§"+money.getConfig().getString(key);
			}
			return money.getConfig().getString(key);
		}
	}

}
