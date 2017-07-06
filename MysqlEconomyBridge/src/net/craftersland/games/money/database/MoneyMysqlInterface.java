package net.craftersland.games.money.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.craftersland.games.money.Money;

public class MoneyMysqlInterface {
	
	private Money money;
	
	public MoneyMysqlInterface(Money money) {
		this.money = money;
	}
	
	public boolean hasAccount(UUID player) {
		PreparedStatement preparedUpdateStatement = null;
		ResultSet result = null;
		Connection conn = money.getDatabaseManagerMysql().getConnection();
		try {		 
			String sql = "SELECT `player_name` FROM `" + money.getConfigurationHandler().getString("database.mysql.tableName") + "` WHERE `player_name` = ?";
			preparedUpdateStatement = conn.prepareStatement(sql);
			preparedUpdateStatement.setString(1, player.toString());
			result = preparedUpdateStatement.executeQuery();
			while (result.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
	    		  if (result != null) {
	    			  result.close();
	    		  }
	    		  if (preparedUpdateStatement != null) {
	    			  preparedUpdateStatement.close();
	    		  }
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
		}
		return false;
	}
	
	public boolean createAccount(UUID player) {
		PreparedStatement preparedStatement = null;
		Connection conn = money.getDatabaseManagerMysql().getConnection();
		try {			 
	        String sql = "INSERT INTO `" + money.getConfigurationHandler().getString("database.mysql.tableName") + "`(`player_name`, `balance`) " + "VALUES(?, ?)";
	        preparedStatement = conn.prepareStatement(sql);
	        preparedStatement.setString(1, player.toString());
	        preparedStatement.setString(2, "0");
	        preparedStatement.executeUpdate();
	        return true;
	      } catch (SQLException e) {
	        e.printStackTrace();
	      } finally {
	    	  try {
	    		  if (preparedStatement != null) {
	    			  preparedStatement.close();
	    		  }
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
		return false;
	}
	
	public Double getBalance(UUID player) {
		PreparedStatement preparedUpdateStatement = null;
		ResultSet result = null;
		Connection conn = money.getDatabaseManagerMysql().getConnection();
		if (!hasAccount(player)) {
			createAccount(player);
		}
	      try {
	        String sql = "SELECT `balance` FROM `" + money.getConfigurationHandler().getString("database.mysql.tableName") + "` WHERE `player_name` = ?";
	        preparedUpdateStatement = conn.prepareStatement(sql);
	        preparedUpdateStatement.setString(1, player.toString());
	        result = preparedUpdateStatement.executeQuery();
	        while (result.next()) {
	        	return Double.parseDouble(result.getString("balance"));
	        }
	      } catch (SQLException e) {
	        e.printStackTrace();
	      } finally {
	    	  try {
		    		if (result != null) {
		    			result.close();
		    		}
		    		if (preparedUpdateStatement != null) {
		    			preparedUpdateStatement.close();
		    		}
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
	      }
		return null;
	}
	
	public boolean setBalance(UUID player, Double amount) {
		PreparedStatement preparedUpdateStatement = null;
		Connection conn = money.getDatabaseManagerMysql().getConnection();
		if (!hasAccount(player)) {
			createAccount(player);
		}
        try {
			String updateSql = "UPDATE `" + money.getConfigurationHandler().getString("database.mysql.tableName") + "` " + "SET `balance` = ?" + "WHERE `player_name` = ?";
			preparedUpdateStatement = conn.prepareStatement(updateSql);
			preparedUpdateStatement.setString(1, amount + "");
			preparedUpdateStatement.setString(2, player.toString());
			preparedUpdateStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (preparedUpdateStatement != null) {
					preparedUpdateStatement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        return false;
	}

}
