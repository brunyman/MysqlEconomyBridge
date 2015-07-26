package net.craftersland.games.money.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.craftersland.games.money.Money;

public class MoneyMysqlInterface implements AccountDatabaseInterface <Double>{
	
	private Money money;
	private Connection conn;
	private String tableName = "meb_accounts";
	
	public MoneyMysqlInterface(Money money) {
		this.money = money;
	}
	
	@Override
	public boolean hasAccount(UUID player) {
		conn = money.getDatabaseManagerInterface().getConnection();
		      try {
		    	  tableName = money.getConfigurationHandler().getString("database.mysql.tableName");
		 
		        String sql = "SELECT `player_name` FROM `" + tableName + "` WHERE `player_name` = ?";
		        PreparedStatement preparedUpdateStatement = conn.prepareStatement(sql);
		        preparedUpdateStatement.setString(1, player.toString());
		        
		        
		        ResultSet result = preparedUpdateStatement.executeQuery();
		 
		        while (result.next()) {
		        	return true;
		        }
		      } catch (SQLException e) {
		        e.printStackTrace();
		      }
		      return false;
	}
	
	@Override
	public boolean createAccount(UUID player) {
		conn = money.getDatabaseManagerInterface().getConnection();
		try {
			tableName = money.getConfigurationHandler().getString("database.mysql.tableName");
			 
	        String sql = "INSERT INTO `" + tableName + "`(`player_name`, `balance`) " + "VALUES(?, ?)";
	        PreparedStatement preparedStatement = conn.prepareStatement(sql);
	        
	        preparedStatement.setString(1, player.toString());
	        preparedStatement.setString(2, "0");
	        
	        preparedStatement.executeUpdate();
	        return true;
	      } catch (SQLException e) {
	        e.printStackTrace();
	      }
		return false;
	}
	
	@Override
	public Double getBalance(UUID player) {
		conn = money.getDatabaseManagerInterface().getConnection();
		if (!hasAccount(player)) {
			createAccount(player);
		}
		
	      try {
	    	  tableName = money.getConfigurationHandler().getString("database.mysql.tableName");
	 
	        String sql = "SELECT `balance` FROM `" + tableName + "` WHERE `player_name` = ?";
	        
	        PreparedStatement preparedUpdateStatement = conn.prepareStatement(sql);
	        preparedUpdateStatement.setString(1, player.toString());
	        ResultSet result = preparedUpdateStatement.executeQuery();
	 
	        while (result.next()) {
	        	return Double.parseDouble(result.getString("balance"));
	        }
	      } catch (SQLException e) {
	        e.printStackTrace();
	      }
		return null;
	}
	
	@Override
	public boolean setBalance(UUID player, Double amount) {
		conn = money.getDatabaseManagerInterface().getConnection();
		if (!hasAccount(player)) {
			createAccount(player);
		}
		
        try {
        	tableName = money.getConfigurationHandler().getString("database.mysql.tableName");
        	
			String updateSql = "UPDATE `" + tableName + "` " + "SET `balance` = ?" + "WHERE `player_name` = ?";
			PreparedStatement preparedUpdateStatement = conn.prepareStatement(updateSql);
			preparedUpdateStatement.setString(1, amount+"");
			preparedUpdateStatement.setString(2, player.toString());
			
			preparedUpdateStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return false;
	}

}
