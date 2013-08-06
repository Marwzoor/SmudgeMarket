package net.smudgecraft.smudgemarket;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class MarketPlayers 
{
	private List<MarketPlayer> players;
	
	public MarketPlayers()
	{
		this.players= new ArrayList<MarketPlayer>();
	}
	
	public void addPlayer(Player player)
	{
		MarketPlayer mp = new MarketPlayer(player);
		this.players.add(mp);
	}
	
	public MarketPlayer getMarketPlayer(Player player)
	{
		for(MarketPlayer mp : this.players)
		{
			if(player.equals(mp.getPlayer()))
			{
				return mp;
			}
		}
		return null;
	}
	
	public void removeMarketPlayer(MarketPlayer player)
	{
		this.players.remove(player);
	}
}
