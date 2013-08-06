package net.smudgecraft.smudgemarket.events;

import net.smudgecraft.smudgemarket.Market;
import net.smudgecraft.smudgemarket.MarketItem;
import net.smudgecraft.smudgemarket.MarketPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class SellEvent extends Event
{
  private static final HandlerList handlers = new HandlerList();
  private MarketPlayer mplayer;
  private int cost;
  private int amount;
  private MarketItem item;
  private ItemStack[] is;
  private Market market;
  private boolean handSell = false;

  public SellEvent(MarketPlayer mplayer, int cost, int amount, MarketItem item, ItemStack[] is, Market market)
  {
    this.mplayer = mplayer;
    this.cost = cost;
    this.amount = amount;
    this.item = item;
    this.is = is;
    this.market = market;
  }

  public SellEvent(MarketPlayer mplayer, int cost, int amount, MarketItem item, Market market, boolean isHand)
  {
    this.mplayer = mplayer;
    this.cost = cost;
    this.amount = amount;
    this.item = item;
    this.market = market;
    this.handSell = isHand;
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

  public ItemStack[] getItemStacks()
  {
    return this.is;
  }

  public Market getMarket()
  {
    return this.market;
  }

  public boolean isHandSell()
  {
    return this.handSell;
  }
}