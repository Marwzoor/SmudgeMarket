package net.smudgecraft.smudgemarket.events;

import net.smudgecraft.smudgemarket.Market;
import net.smudgecraft.smudgemarket.MarketItem;
import net.smudgecraft.smudgemarket.MarketPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class BuyEvent extends Event
{
  private static final HandlerList handlers = new HandlerList();
  private MarketPlayer mplayer;
  private int cost;
  private int amount;
  private MarketItem item;
  private ItemStack is;
  private Market market;

  public BuyEvent(MarketPlayer mplayer, int cost, int amount, MarketItem item, ItemStack is, Market market)
  {
    this.mplayer = mplayer;
    this.cost = cost;
    this.amount = amount;
    this.item = item;
    this.is = is;
    this.market = market;
  }

  public HandlerList getHandlers()
  {
    return handlers;
  }

  public static HandlerList getHandlerList()
  {
    return handlers;
  }

  public int getCost()
  {
    return this.cost;
  }

  public MarketPlayer getMarketPlayer()
  {
    return this.mplayer;
  }

  public int getAmount()
  {
    return this.amount;
  }

  public MarketItem getMarketItem()
  {
    return this.item;
  }

  public ItemStack getItemStack()
  {
    return this.is;
  }

  public Market getMarket()
  {
    return this.market;
  }
}