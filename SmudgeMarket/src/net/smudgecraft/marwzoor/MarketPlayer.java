package net.smudgecraft.marwzoor;

import org.bukkit.entity.Player;

public class MarketPlayer 
{
	private Player player;
	private Market market;
	public MarketPlayer(Player player)
	{
		this.player=player;
		this.market=null;
	}
	
	public Market getMarket()
	{
		return this.market;
	}
	
	public Player getPlayer()
	{
		return this.player;
	}
	
	public void setMarket(Market market)
	{
		this.market=market;
	}
	
	public void removeMarket()
	{
		this.market=null;
	}
}
