package net.craftersland.eco.bridge.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import net.craftersland.eco.bridge.Eco;

public class EcoMysqlHandler {
	
	private Eco eco;
	
	public EcoMysqlHandler(Eco eco) {
		this.eco = eco;
	}
	
	public String[] getData(Player player) {
		if (!hasAccount(player)) {
			createAccount(player);
		}
		PreparedStatement preparedUpdateStatement = null;
		ResultSet result = null;
		Connection conn = eco.getMysqlSetup().getConnection();
		if (conn != null) {
			try {
				String sql = "SELECT * FROM `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` WHERE `player_uuid` = ? LIMIT 1";
		        preparedUpdateStatement = conn.prepareStatement(sql);
		        preparedUpdateStatement.setString(1, player.getUniqueId().toString());
		        
		        result = preparedUpdateStatement.executeQuery();
		        while (result.next()) {
					return new String[]{"" + result.getDouble("money"), result.getString("sync_complete"), result.getString("last_seen")};
		        }
		    } catch (SQLException e) {
				Eco.log.warning("Error: " + e.getMessage());
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
		}
		return null;
	}
	
	public void setSyncStatus(Player player, String syncStatus) {
		PreparedStatement preparedUpdateStatement = null;
		Connection conn = eco.getMysqlSetup().getConnection();
		if (conn != null) {
			try {
				String data = "UPDATE `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` " + "SET `sync_complete` = ?" + ", `last_seen` = ?" + " WHERE `player_uuid` = ?";
				preparedUpdateStatement = conn.prepareStatement(data);
				preparedUpdateStatement.setString(1, syncStatus);
				preparedUpdateStatement.setString(2, String.valueOf(System.currentTimeMillis()));
				preparedUpdateStatement.setString(3, player.getUniqueId().toString());
				
				preparedUpdateStatement.executeUpdate();
			} catch (SQLException e) {
				Eco.log.warning("Error: " + e.getMessage());
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
		}
	}
	
	public void setData(Player player, Double money, String syncComplete) {
		if (!hasAccount(player)) {
			createAccount(player);
		}
		PreparedStatement preparedUpdateStatement = null;
		Connection conn = eco.getMysqlSetup().getConnection();
		if (conn != null) {
			try {
				String data = "UPDATE `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` " + "SET `player_name` = ?" + ", `money` = ?" + ", `sync_complete` = ?" + ", `last_seen` = ?" + " WHERE `player_uuid` = ?";
				preparedUpdateStatement = conn.prepareStatement(data);
				preparedUpdateStatement.setString(1, player.getName());
				preparedUpdateStatement.setDouble(2, money);
				preparedUpdateStatement.setString(3, syncComplete);
				preparedUpdateStatement.setString(4, String.valueOf(System.currentTimeMillis()));
				preparedUpdateStatement.setString(5, player.getUniqueId().toString());
				
				preparedUpdateStatement.executeUpdate();
			} catch (SQLException e) {
				Eco.log.warning("Error: " + e.getMessage());
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
		}
	}
	
	public void createAccount(Player player) {
		PreparedStatement preparedStatement = null;
		Connection conn = eco.getMysqlSetup().getConnection();
		if (conn != null) {
			try {
				String sql = "INSERT INTO `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "`(`player_uuid`, `player_name`, `money`, `last_seen`, `sync_complete`) " + "VALUES(?, ?, ?, ?, ?)";
		        preparedStatement = conn.prepareStatement(sql);
		        
		        preparedStatement.setString(1, player.getUniqueId().toString());
		        preparedStatement.setString(2, player.getName());
		        preparedStatement.setDouble(3, 0.0);
		        preparedStatement.setString(4, String.valueOf(System.currentTimeMillis()));
		        preparedStatement.setString(5, "true");
		        
		        preparedStatement.executeUpdate();
		      } catch (SQLException e) {
				  Eco.log.warning("Error: " + e.getMessage());
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
		}
	}
	
	public boolean hasAccount(Player player) {
		PreparedStatement preparedUpdateStatement = null;
		ResultSet result = null;
		Connection conn = eco.getMysqlSetup().getConnection();
		if (conn != null) {
			try {			
				String sql = "SELECT `player_uuid` FROM `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` WHERE `player_uuid` = ? LIMIT 1";
		        preparedUpdateStatement = conn.prepareStatement(sql);
		        preparedUpdateStatement.setString(1, player.getUniqueId().toString());
		        
		        result = preparedUpdateStatement.executeQuery();
		        while (result.next()) {
		        	return true;
		        }
		      } catch (SQLException e) {
				  Eco.log.warning("Error: " + e.getMessage());
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
		}
		return false;
	}

}
