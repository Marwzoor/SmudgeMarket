package net.smudgecraft.marwzoor;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
public class Market extends CuboidArea
{
	private String name;
	private List<MarketItem> items;
	public Market(Location corner1, Location corner2, String name, List<MarketItem> items)
	{
		super(corner1, corner2);
		this.items=items;
		this.name=name;
	}
	
	public Market(Location corner1, Location corner2, String name)
	{
		super(corner1, corner2);
		this.name=name;
		this.items = new ArrayList<MarketItem>();
		List<Material> banned = new ArrayList<Material>();
		banned.add(Material.AIR);
		banned.add(Material.BEACON);
		banned.add(Material.BED_BLOCK);
		banned.add(Material.BEDROCK);
		banned.add(Material.BREWING_STAND_ITEM);
		banned.add(Material.BURNING_FURNACE);
		banned.add(Material.CARROT_ITEM);
		banned.add(Material.CARROT_STICK);
		banned.add(Material.CAKE_BLOCK);
		banned.add(Material.CAULDRON_ITEM);
		banned.add(Material.COAL_ORE);
		banned.add(Material.COMMAND);
		banned.add(Material.CROPS);
		banned.add(Material.DEAD_BUSH);
		banned.add(Material.DIAMOND_ORE);
		banned.add(Material.DIODE_BLOCK_OFF);
		banned.add(Material.DIODE_BLOCK_ON);
		banned.add(Material.DOUBLE_STEP);
		banned.add(Material.DRAGON_EGG);
		banned.add(Material.EMERALD_ORE);
		banned.add(Material.NETHER_STAR);
		banned.add(Material.EYE_OF_ENDER);
		banned.add(Material.ENCHANTED_BOOK);
		banned.add(Material.ENDER_PORTAL);
		banned.add(Material.ENDER_PORTAL_FRAME);
		banned.add(Material.ENDER_STONE);
		banned.add(Material.FIRE);
		banned.add(Material.FIREBALL);
		banned.add(Material.FIREWORK);
		banned.add(Material.FIREWORK_CHARGE);
		banned.add(Material.FLOWER_POT_ITEM);
		banned.add(Material.GLOWING_REDSTONE_ORE);
		banned.add(Material.GOLD_ORE);
		banned.add(Material.GRASS);
		banned.add(Material.HUGE_MUSHROOM_1);
		banned.add(Material.HUGE_MUSHROOM_2);
		banned.add(Material.ICE);
		banned.add(Material.IRON_DOOR_BLOCK);
		banned.add(Material.IRON_ORE);
		banned.add(Material.LAPIS_ORE);
		banned.add(Material.LAVA);
		banned.add(Material.LAVA_BUCKET);
		banned.add(Material.LEAVES);
		banned.add(Material.LOCKED_CHEST);
		banned.add(Material.LONG_GRASS);
		banned.add(Material.MELON_STEM);
		banned.add(Material.MOB_SPAWNER);
		banned.add(Material.MONSTER_EGG);
		banned.add(Material.MONSTER_EGGS);
		banned.add(Material.PISTON_BASE);
		banned.add(Material.PISTON_EXTENSION);
		banned.add(Material.PISTON_STICKY_BASE);
		banned.add(Material.PISTON_MOVING_PIECE);
		banned.add(Material.PORTAL);
		banned.add(Material.POTATO_ITEM);
		banned.add(Material.POTION);
		banned.add(Material.POWERED_MINECART);
		banned.add(Material.REDSTONE_LAMP_OFF);
		banned.add(Material.REDSTONE_LAMP_ON);
		banned.add(Material.REDSTONE_ORE);
		banned.add(Material.REDSTONE_TORCH_OFF);
		banned.add(Material.REDSTONE_TORCH_ON);
		banned.add(Material.POTION);
		banned.add(Material.REDSTONE_WIRE);
		banned.add(Material.SIGN_POST);
		banned.add(Material.SKULL_ITEM);
		banned.add(Material.SNOW);
		banned.add(Material.SNOW_BLOCK);
		banned.add(Material.SNOW_BALL);
		banned.add(Material.SOIL);
		banned.add(Material.SPONGE);
		banned.add(Material.STATIONARY_LAVA);
		banned.add(Material.STATIONARY_WATER);
		banned.add(Material.STEP);
		banned.add(Material.VINE);
		banned.add(Material.WALL_SIGN);
		banned.add(Material.WATER);
		banned.add(Material.WATER_LILY);
		banned.add(Material.WEB);
		banned.add(Material.WOOD_DOUBLE_STEP);
		banned.add(Material.WOOD_STEP);
		banned.add(Material.WRITTEN_BOOK);
		banned.add(Material.SUGAR_CANE);
		
		for(Material mat : Material.values())
		{
			if(banned.contains(mat)==false)
			{
				MarketItem mi = new MarketItem(mat,0,0);
				items.add(mi);
			}
		}
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public List<MarketItem> getMarketItems()
	{
		return this.items;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public int getBuyCostByMaterial(Material mat)
	{
		for(MarketItem mi : items)
		{
			if(mi.getItem().equals(mat))
			{
				return mi.getBuyCost();
			}
		}
		return 0;
	}
	
	public boolean containsItem(String alias)
	{
		String al = alias.toUpperCase();
		for(MarketItem mi : items)
		{
			if(mi==null)
			{
				continue;
			}
			if(mi.getAlias()==null)
			{
				continue;
			}
			if(mi.getAlias().equalsIgnoreCase(al))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean containsItem(int itemid)
	{
		for(MarketItem mi : items)
		{
			if(mi.getItemId()==itemid)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean containsItem(Material mat)
	{
		for(MarketItem mi : items)
		{
			if(mi.getItem().equals(mat))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean containsItem(int itemid, short durability)
	{
		for(MarketItem mi : items)
		{
			if(mi.getItemId()==itemid)
			{
				if(mi.getDurability()==durability)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public int getSellCostByMaterial(Material mat)
	{
		for(MarketItem mi : items)
		{
			if(mi.getItem().equals(mat))
			{
				return mi.getSellCost();
			}
		}
		return 0;
	}
	
	public MarketItem getMarketItemById(int id)
	{
		for(MarketItem mi : items)
		{
			if(mi.getItemId()==id)
			{
				return mi;
			}
		}
		return null;
	}
	
	public MarketItem getMarketItemByIdAndDurability(int id, short durability)
	{
		for(MarketItem mi : items)
		{
			if(mi.getItemId()==id)
			{
				if(mi.getDurability()==durability)
				{
					return mi;
				}
			}
		}
		return null;
	}
	public MarketItem getMarketItemByMaterial(Material mat)
	{
		for(MarketItem mi : items)
		{
			if(mi.getItem().equals(mat))
			{
				return mi;
			}
		}
		return null;
	}
	
	public MarketItem getMarketItemByName(String name)
	{
		for(MarketItem mi : items)
		{
			if(mi==null)
			{
				continue;
			}
			if(mi.getAlias()==null)
			{
				continue;
			}
			if(mi.getAlias().equalsIgnoreCase(name))
			{
				return mi;
			}
		}
		return null;
	}
	
	public void setItems(List<MarketItem> items)
	{
		this.items=items;
	}
}
