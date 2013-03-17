package net.smudgecraft.marwzoor;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Markets 
{
	public static List<Market> markets = new ArrayList<Market>();
	
	public static void addMarket(Market market)
	{
		markets.add(market);
	}
	
	public static void removeMarket(Market market)
	{
		markets.remove(market);
	}
	
	public static Market getMarket(String name)
	{
		for(Market market : markets)
		{
			if(market.getName().equalsIgnoreCase(name))
			{
				return market;
			}
		}
		return null;
	}
	
	public static void clearMarkets()
	{
		markets.clear();
	}
	
	public static boolean isLocInMarket(Location loc)
	{
		for(Market market : markets)
		{
			if(market.containsLoc(loc))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPlayerInMarket(Player player)
	{
		MarketPlayer mp = SmudgeMarket.plugin.marketplayers.getMarketPlayer(player);
		if(mp.getMarket()!=null)
			return true;
		else
			return false;
	}
	
	public static Market getPlayersCurrentMarket(Player player)
	{
		MarketPlayer mp = SmudgeMarket.plugin.marketplayers.getMarketPlayer(player);
		return mp.getMarket();
	}
	
	public static Market getMarket(Location loc)
	{
		for(Market market : markets)
		{
			if(market.containsLoc(loc))
			{
				return market;
			}
		}
		return null;
	}
	
	public static List<Market> getMarkets()
	{
		return markets;
	}
	
	public static boolean exists(Market market)
	{
		if(markets.contains(market))
		{
			return true;
		}
		return false;
	}
	
	public static boolean exists(String name)
	{
		for(Market market : markets)
		{
			if(market.getName().equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
}
