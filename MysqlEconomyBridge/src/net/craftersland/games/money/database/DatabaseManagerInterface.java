package net.craftersland.games.money.database;

import java.sql.Connection;

public interface DatabaseManagerInterface {

	public boolean setupDatabase();
	public boolean closeDatabase();
	public Connection getConnection();
}
