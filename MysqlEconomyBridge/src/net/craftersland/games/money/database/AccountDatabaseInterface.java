package net.craftersland.games.money.database;

import java.util.UUID;

public interface AccountDatabaseInterface<X> {

	
	//Accountmethods
	public boolean hasAccount(UUID player);
	public boolean createAccount(UUID player);
	public X getBalance(UUID player);
	public boolean setBalance(UUID player, X amount);
	public boolean addToAccount(UUID player, X amount);
	public boolean removeFromAccount(UUID player, X amount);
	public UUID[] getAccounts();

}
