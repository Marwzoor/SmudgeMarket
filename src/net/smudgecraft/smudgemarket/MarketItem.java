package net.smudgecraft.smudgemarket;

import org.bukkit.Material;

public class MarketItem 
{
	private String alias;
	private Material item;
	private int itemid;
	private int buycost;
	private int sellcost;
	private short durability=-20;
	
	public MarketItem(Material item, int buycost, int sellcost, String alias, int itemid, short durability)
	{
		if(item==null)
		{
			this.item=Material.DIRT;
		}
		else
		{
			this.item=item;
		}
		this.buycost=buycost;
		this.sellcost=sellcost;
		this.alias=alias;
		this.itemid=itemid;
		this.durability=durability;
	}
	
	public MarketItem(Material item, int buycost, int sellcost, String alias, int itemid)
	{
		if(item==null)
		{
			this.item=Material.DIRT;
		}
		else
		{
			this.item=item;
		}
		this.buycost=buycost;
		this.sellcost=sellcost;
		this.alias=alias;
		this.itemid=itemid;
	}
	
	public boolean hasDurability()
	{
		if(durability!=-20)
			return true;
		return false;
	}
	
	public short getDurability()
	{
		return this.durability;
	}
	
	public MarketItem(Material item, int buycost, int sellcost, String alias)
	{
		if(item==null)
		{
			this.item=Material.DIRT;
		}
		else
		{
			this.item=item;
		}
		this.buycost=buycost;
		this.sellcost=sellcost;
		this.alias=alias;
	}
	
	public MarketItem(Material item, int buycost, int sellcost)
	{
		if(item==null)
		{
			this.item=Material.DIRT;
		}
		else
		{
			this.item=item;
		}
		this.buycost=buycost;
		this.sellcost=sellcost;
	}
	
	public int getItemId()
	{
		return this.itemid;
	}
	
	public String getAlias()
	{
		return this.alias;
	}
	
	public String getName()
	{
		return this.alias;
	}
	
	public Material getItem()
	{
		return this.item;
	}
	
	public int getBuyCost()
	{
		return this.buycost;
	}
	
	public int getSellCost()
	{
		return this.sellcost;
	}
	
	public void setBuyCost(int cost)
	{
		this.buycost=cost;
	}
	
	public void setSellCost(int cost)
	{
		this.sellcost=cost;
	}
	
	public void setItem(Material item)
	{
		this.item=item;
	}
}
