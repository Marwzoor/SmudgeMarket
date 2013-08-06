package net.smudgecraft.smudgemarket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import net.smudgecraft.smudgemarket.events.BuyEvent;
import net.smudgecraft.smudgemarket.events.SellEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerListener
  implements Listener
{
  public static SmudgeMarket plugin;

  public PlayerListener(SmudgeMarket instance)
  {
    plugin = instance;
  }

  @EventHandler
  public void onBuyEvent(BuyEvent event) {
    MarketPlayer mp = event.getMarketPlayer();
    int cost = event.getCost();
    int amount = event.getAmount();
    MarketItem mi = event.getMarketItem();
    Market market = event.getMarket();

    SmudgeMarket.economy.withdrawPlayer(mp.getPlayer().getName(), cost);
    mp.getPlayer().sendMessage(ChatColor.GOLD + "You have bought " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
    plugin.giveItemStack(event.getItemStack(), mp.getPlayer());

    String log = "";

    log = log + mp.getPlayer().getName() + " bought " + amount + " " + mi.getAlias().toLowerCase() + " for " + cost + " Smudges in market " + market.getName() + ".";

    if (plugin.logging)
    {
      writeToLog(log);
    }
  }

  @EventHandler
  public void onSellEvent(SellEvent event)
  {
    if (event.isHandSell())
    {
      MarketPlayer mp = event.getMarketPlayer();
      int cost = event.getCost();
      int amount = event.getAmount();
      MarketItem mi = event.getMarketItem();
      Market market = event.getMarket();

      SmudgeMarket.economy.depositPlayer(mp.getPlayer().getName(), cost);
      mp.getPlayer().sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
      mp.getPlayer().setItemInHand(new ItemStack(Material.AIR));

      String log = "";

      log = log + mp.getPlayer().getName() + " sold " + amount + " " + mi.getAlias().toLowerCase() + " for " + cost + " Smudges in market \"" + market.getName() + "\".";

      if (plugin.logging)
      {
        writeToLog(log);
      }
    }
    else
    {
      MarketPlayer mp = event.getMarketPlayer();
      int cost = event.getCost();
      int amount = event.getAmount();
      MarketItem mi = event.getMarketItem();
      Market market = event.getMarket();
      ItemStack[] remove = event.getItemStacks();

      SmudgeMarket.economy.depositPlayer(mp.getPlayer().getName(), cost);
      mp.getPlayer().sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
      mp.getPlayer().getInventory().removeItem(remove);

      String log = "";

      log = log + mp.getPlayer().getName() + " sold " + amount + " " + mi.getAlias().toLowerCase() + " for " + cost + " Smudges in market \"" + market.getName() + "\".";

      if (plugin.logging)
      {
        writeToLog(log);
      }
    }
  }

  @EventHandler
  public void onPlayerInteractEvent(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
    {
      if (player.getItemInHand().getType().equals(Material.STICK))
      {
        Location loc = event.getClickedBlock().getLocation();
        plugin.pos2.put(player, loc);
        player.sendMessage(ChatColor.GOLD + "[SmudgeMarket]: " + ChatColor.YELLOW + "Location 1 set");
        event.setCancelled(true);
      }
    }
    else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
    {
      if (player.getItemInHand().getType().equals(Material.STICK))
      {
        Location loc = event.getClickedBlock().getLocation();
        plugin.pos1.put(player, loc);
        player.sendMessage(ChatColor.GOLD + "[SmudgeMarket]: " + ChatColor.YELLOW + "Location 2 set");
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority=EventPriority.MONITOR)
  public void onPlayerMoveEvent(PlayerMoveEvent event) {
    Player player = event.getPlayer();

    Location loc = player.getLocation();

    MarketPlayer mp = plugin.marketplayers.getMarketPlayer(player);

    if (mp.getMarket() != null)
    {
      if (!Markets.isLocInMarket(loc))
      {
        mp.removeMarket();
        player.sendMessage(ChatColor.GOLD + "You have left the market area!");
      }

    }
    else if (Markets.isLocInMarket(loc))
    {
      Market market = Markets.getMarket(loc);
      mp.setMarket(market);
      player.sendMessage(ChatColor.GOLD + "You have entered " + ChatColor.YELLOW + market.getName() + ChatColor.GOLD + " market area");
      player.sendMessage(ChatColor.GOLD + "Write: " + ChatColor.YELLOW + "/market help " + ChatColor.GOLD + "for commands.");
    }
  }

  @EventHandler
  public void onPlayerJoinEvent(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    Location loc = player.getLocation();

    plugin.marketplayers.addPlayer(player);

    MarketPlayer mp = plugin.marketplayers.getMarketPlayer(player);

    if (Markets.isLocInMarket(loc))
    {
      mp.setMarket(Markets.getMarket(loc));
    }
  }

  @EventHandler
  public void onPlayerQuitEvent(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    MarketPlayer mp = plugin.marketplayers.getMarketPlayer(player);
    plugin.marketplayers.removeMarketPlayer(mp);
  }
  @EventHandler
  public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
    Location loc = event.getTo();
    Player player = event.getPlayer();
    MarketPlayer mp = plugin.marketplayers.getMarketPlayer(event.getPlayer());
    if (mp != null)
    {
      if (mp.getMarket() != null)
      {
        if (Markets.isLocInMarket(loc))
        {
          Market market = Markets.getMarket(loc);

          if (market != mp.getMarket())
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
      else if (Markets.isLocInMarket(loc))
      {
        Market market = Markets.getMarket(loc);

        mp.setMarket(market);
        player.sendMessage(ChatColor.GOLD + "You have entered " + ChatColor.YELLOW + market.getName() + ChatColor.GOLD + " market area");
        player.sendMessage(ChatColor.GOLD + "Write: " + ChatColor.YELLOW + "/market help " + ChatColor.GOLD + "for commands.");
      }

    }
    else
    {
      plugin.marketplayers.addPlayer(player);

      MarketPlayer marketp = plugin.marketplayers.getMarketPlayer(player);

      if (Markets.isLocInMarket(loc))
      {
        marketp.setMarket(Markets.getMarket(loc));
      }
    }
  }

  public void writeToLog(String line)
  {
    Writer writer = null;

    BufferedReader reader = null;

    TimeZone tz = TimeZone.getTimeZone("GMT+01");

    Calendar cal = Calendar.getInstance(tz);

    int h = cal.get(11);

    int m = cal.get(12);

    int d = cal.get(5);

    int M = cal.get(2);

    String month = "";

    String day = "";

    String hour = "";

    String minute = "";

    if ((M >= 0) && (M <= 9))
    {
      month = "0" + M;
    }
    else
    {
      month = M;
    }

    if ((d >= 0) && (d <= 9))
    {
      day = "0" + d;
    }
    else
    {
      day = d;
    }

    if ((m >= 0) && (m <= 9))
    {
      minute = "0" + m;
    }
    else
    {
      minute = m;
    }

    if ((h >= 0) && (h <= 9))
    {
      hour = "0" + h;
    }
    else
    {
      hour = h;
    }

    String text = "[" + day + "-" + month + "-" + cal.get(1) + " | " + hour + ":" + minute + "] ";

    text = text + line;

    label590: 
    try { File log = new File(plugin.getDataFolder() + "/log.txt");

      if (!log.exists())
      {
        log.createNewFile();
      }

      reader = new BufferedReader(new FileReader(log));

      writer = new BufferedWriter(new FileWriter(log, true));

      if (!line.isEmpty())
      {
        if (reader.readLine() == null)
        {
          writer.append(text); break label590;
        }

        writer.append(System.getProperty("line.separator") + text);
      }

    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      try
      {
        if (writer != null)
        {
          writer.close();
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      try
      {
        if (writer != null)
        {
          writer.close();
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    finally
    {
      try
      {
        if (writer != null)
        {
          writer.close();
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}