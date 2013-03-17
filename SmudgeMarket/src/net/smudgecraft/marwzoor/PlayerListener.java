package net.smudgecraft.marwzoor;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener
{
	public static SmudgeMarket plugin;
	
	public PlayerListener(SmudgeMarket instance)
	{
		plugin=instance;
	}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(event.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			if(player.getItemInHand().getType().equals(Material.STICK))
			{
			Location loc = event.getClickedBlock().getLocation();
			plugin.pos2.put(player, loc);
			player.sendMessage(ChatColor.GOLD + "Location 1 set.");
			event.setCancelled(true);
			}
		}
		else if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			if(player.getItemInHand().getType().equals(Material.STICK))
			{
			Location loc = event.getClickedBlock().getLocation();
			plugin.pos1.put(player, loc);
			player.sendMessage(ChatColor.GOLD + "Location 2 set.");
			event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		
		Location loc = player.getLocation();
		
		MarketPlayer mp = plugin.marketplayers.getMarketPlayer(player);
		
		if(mp.getMarket()!=null)
		{
			if(Markets.isLocInMarket(loc)==false)
			{
				mp.removeMarket();
				player.sendMessage(ChatColor.GOLD + "You have left the market area!");
			}
		}
		
		else
		{
			if(Markets.isLocInMarket(loc))
			{
				Market market = Markets.getMarket(loc);
				mp.setMarket(market);
				player.sendMessage(ChatColor.GOLD + "You have entered " + ChatColor.YELLOW + market.getName() + ChatColor.GOLD + " market area");
				player.sendMessage(ChatColor.GOLD + "Write: " + ChatColor.YELLOW + "/market help " + ChatColor.GOLD + "for commands.");
			}
		}
	}
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		
		Location loc = player.getLocation();
		
		plugin.marketplayers.addPlayer(player);
		
		MarketPlayer mp = plugin.marketplayers.getMarketPlayer(player);
		
		if(Markets.isLocInMarket(loc))
		{
			mp.setMarket(Markets.getMarket(loc));
		}
	}
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		MarketPlayer mp = plugin.marketplayers.getMarketPlayer(player);
		plugin.marketplayers.removeMarketPlayer(mp);
	}
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent event)
	{
		Location loc = event.getTo();
		Player player = event.getPlayer();
		MarketPlayer mp = plugin.marketplayers.getMarketPlayer(event.getPlayer());
		if(mp!=null)
		{
		if(mp.getMarket()!=null)
		{
			if(Markets.isLocInMarket(loc)==true)
			{
				Market market = Markets.getMarket(loc);
				
				if(market!=mp.getMarket())
				{
					mp.setMarket(market);
					player.sendMessage(ChatColor.GOLD + "You have entered " + ChatColor.YELLOW + market.getName() + ChatColor.GOLD + " market area");
					player.sendMessage(ChatColor.GOLD + "Write: " + ChatColor.YELLOW + "/market help " + ChatColor.GOLD + "for commands.");
				}
			}
			else
			{
				mp.removeMarket();
				player.sendMessage(ChatColor.GOLD + "You have left the market area!");
			}
		}
		else
		{
			if(Markets.isLocInMarket(loc)==true)
			{
				Market market = Markets.getMarket(loc);
				
				mp.setMarket(market);
				player.sendMessage(ChatColor.GOLD + "You have entered " + ChatColor.YELLOW + market.getName() + ChatColor.GOLD + " market area");
				player.sendMessage(ChatColor.GOLD + "Write: " + ChatColor.YELLOW + "/market help " + ChatColor.GOLD + "for commands.");
			}
		}
		}
		else
		{
			plugin.marketplayers.addPlayer(player);
			
			MarketPlayer marketp = plugin.marketplayers.getMarketPlayer(player);
			
			if(Markets.isLocInMarket(loc))
			{
				marketp.setMarket(Markets.getMarket(loc));
			}
		}
	}
}
