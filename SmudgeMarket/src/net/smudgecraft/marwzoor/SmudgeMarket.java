package net.smudgecraft.marwzoor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SmudgeMarket extends JavaPlugin
{
	public static Permission permission = null;
	public static Economy economy = null;
	public static Chat chat = null;
	public static SmudgeMarket plugin;
	public final PlayerListener pl = new PlayerListener(this);
	public final HashMap<Player, Location> pos1 = new HashMap<Player, Location>();
	public final HashMap<Player, Location> pos2 = new HashMap<Player, Location>();
	public final HashMap<Player, Boolean> isInMarket = new HashMap<Player, Boolean>();
	public MarketPlayers marketplayers = new MarketPlayers();
	
	@Override
	public void onEnable()
	{
		plugin=this;
		setupPermissions();
		setupEconomy();
		setupChat();
		getServer().getPluginManager().registerEvents(pl, this);
		saveDefaultConfig();
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
		{
			public void run()
			{
				loadMarkets();
			}
		}, 20L);
	}
	
	@Override
	public void onDisable()
	{
		
	}
	
	private boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> permissionsProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if(permissionsProvider != null)
		{
			permission = permissionsProvider.getProvider();
		}
		return (permission != null);
	}
	private boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if(economyProvider != null)
		{
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	private boolean setupChat()
	{
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		if(chatProvider != null)
		{
			chat = chatProvider.getProvider();
		}
		return (chat != null);
	}
	
	public void loadMarkets()
	{
		FileConfiguration config = this.getConfig();
		List<String> markets = config.getStringList("Markets");
		for(String name : markets)
		{
			if(config.getString(name)!=null)
			{
			Location corner1 = new Location(getServer().getWorld(config.getString(name + ".Location.World")), config.getInt(name + ".Location.highX"), config.getInt(name + ".Location.highY"), config.getInt(name + ".Location.highZ"));
			Location corner2 = new Location(getServer().getWorld(config.getString(name + ".Location.World")), config.getInt(name + ".Location.lowX"), config.getInt(name + ".Location.lowY"), config.getInt(name + ".Location.lowZ"));
			List<MarketItem> items = new ArrayList<MarketItem>();
			if(config.getStringList(name + ".Items.List")!=null)
			{
			List<String> itemnames = config.getStringList(name + ".Items.List");
			
			for(String itemname : itemnames)
			{
				itemname = itemname.toUpperCase();
				int itemid = config.getInt(name + ".Items." + itemname + ".Id");
				if(config.contains(name + ".Items." + itemname + ".Durability"))
				{
				MarketItem mi;
				mi = new MarketItem(Material.getMaterial(itemid), config.getInt(name + ".Items." + itemname + ".BuyCost"), config.getInt(name + ".Items." + itemname + ".SellCost"), config.getString(name + ".Items." + itemname + ".Alias"), config.getInt(name + ".Items." + itemname + ".Id"), (short) config.getInt(name + ".Items." + itemname + ".Durability"));
				items.add(mi);
				}
				else
				{
				MarketItem mi;
				mi = new MarketItem(Material.getMaterial(itemid), config.getInt(name + ".Items." + itemname + ".BuyCost"), config.getInt(name + ".Items." + itemname + ".SellCost"), config.getString(name + ".Items." + itemname + ".Alias"), config.getInt(name + ".Items." + itemname + ".Id"));
				items.add(mi);
				}
			}
			Market market = new Market(corner1, corner2, name, items);
			Markets.addMarket(market);
			}
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args)
	{
		Player player = (Player) sender;
		if(commandlabel.equalsIgnoreCase("sm") && args.length>0)
		{
			if(player.isOp())
			{
			FileConfiguration config = this.getConfig();
			if(args[0].equalsIgnoreCase("create") && args.length>1)
			{
				if(pos1.containsKey(player)==false)
				{
					player.sendMessage(ChatColor.RED + "You must set two positions!");
					return true;
				}
				if(pos2.containsKey(player)==false)
				{
					player.sendMessage(ChatColor.RED + "You must set two positions!");
					return true;
				}
				String name = args[1];
				List<String> markets = config.getStringList("Markets");
				if(markets.contains(name))
				{
					player.sendMessage(ChatColor.RED + "There is already a market by that name!");
					return true;
				}
				markets.add(name);
				Market market = new Market(pos1.get(player), pos2.get(player), name);
				for(Market mark : Markets.getMarkets())
				{
					if(mark.checkCollision(market))
					{
						player.sendMessage(ChatColor.RED + "There is already a market within that area!");
						return true;
					}
				}
				Markets.addMarket(market);
				List<String> items = new ArrayList<String>();
				config.set("Markets", markets);
				config.set(name + ".Location.World", pos1.get(player).getWorld().getName());
				config.set(name + ".Location.highX", pos1.get(player).getBlockX());
				config.set(name + ".Location.highY", pos1.get(player).getBlockY());
				config.set(name + ".Location.highZ", pos1.get(player).getBlockZ());
				config.set(name + ".Location.lowX", pos2.get(player).getBlockX());
				config.set(name + ".Location.lowY", pos2.get(player).getBlockY());
				config.set(name + ".Location.lowZ", pos2.get(player).getBlockZ());
				
				config.set(name + ".Items.ANVIL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ANVIL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ANVIL.Alias", "ANVIL");
				config.set(name + ".Items.ANVIL.Id", Integer.valueOf(145));
				items.add("ANVIL");
				
				config.set(name + ".Items.APPLE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.APPLE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.APPLE.Alias", "APPLE");
				config.set(name + ".Items.APPLE.Id", Integer.valueOf(260));
				items.add("APPLE");
				
				config.set(name + ".Items.ARROW.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ARROW.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ARROW.Alias", "ARROW");
				config.set(name + ".Items.ARROW.Id", Integer.valueOf(262));
				items.add("ARROW");

				config.set(name + ".Items.BAKEDPOTATO.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BAKEDPOTATO.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BAKEDPOTATO.Alias", "BAKEDPOTATO");
				config.set(name + ".Items.BAKEDPOTATO.Id", Integer.valueOf(393));
				items.add("BAKEDPOTATO");
				
				config.set(name + ".Items.BIRCHWOODSTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHWOODSTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHWOODSTAIRS.Alias", "BIRCHWOODSTAIRS");
				config.set(name + ".Items.BIRCHWOODSTAIRS.Id", Integer.valueOf(135));
				items.add("BIRCHWOODSTAIRS");
				
				config.set(name + ".Items.BLAZEPOWDER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BLAZEPOWDER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BLAZEPOWDER.Alias", "BLAZEPOWDER");
				config.set(name + ".Items.BLAZEPOWDER.Id", Integer.valueOf(377));
				items.add("BLAZEPOWDER");
				
				config.set(name + ".Items.BLAZEROD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BLAZEROD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BLAZEROD.Alias", "BLAZEROD");
				config.set(name + ".Items.BLAZEROD.Id", Integer.valueOf(369));
				items.add("BLAZEROD");
				
				config.set(name + ".Items.BOAT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BOAT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BOAT.Alias", "BOAT");
				config.set(name + ".Items.BOAT.Id", Integer.valueOf(333));
				items.add("BOAT");
				
				config.set(name + ".Items.BONE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BONE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BONE.Alias", "BONE");
				config.set(name + ".Items.BONE.Id", Integer.valueOf(352));
				items.add("BONE");
				
				config.set(name + ".Items.BOOK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BOOK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BOOK.Alias", "BOOK");
				config.set(name + ".Items.BOOK.Id", Integer.valueOf(340));
				items.add("BOOK");
				
				config.set(name + ".Items.BOOKANDQUILL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BOOKANDQUILL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BOOKANDQUILL.Alias", "BOOKANDQUILL");
				config.set(name + ".Items.BOOKANDQUILL.Id", Integer.valueOf(386));
				items.add("BOOKANDQUILL");
				
				config.set(name + ".Items.BOOKSHELF.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BOOKSHELF.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BOOKSHELF.Alias", "BOOKSHELF");
				config.set(name + ".Items.BOOKSHELF.Id", Integer.valueOf(47));
				items.add("BOOKSHELF");
				
				config.set(name + ".Items.BOW.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BOW.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BOW.Alias", "BOW");
				config.set(name + ".Items.BOW.Id", Integer.valueOf(261));
				items.add("BOW");
				
				config.set(name + ".Items.BOWL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BOWL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BOWL.Alias", "BOWL");
				config.set(name + ".Items.BOWL.Id", Integer.valueOf(281));
				items.add("BOWL");
				
				config.set(name + ".Items.BREAD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BREAD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BREAD.Alias", "BREAD");
				config.set(name + ".Items.BREAD.Id", Integer.valueOf(297));
				items.add("BREAD");
				
				config.set(name + ".Items.BREWINGSTAND.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BREWINGSTAND.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BREWINGSTAND.Alias", "BREWINGSTAND");
				config.set(name + ".Items.BREWINGSTAND.Id", Integer.valueOf(117));
				items.add("BREWINGSTAND");
				
				config.set(name + ".Items.BRICK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BRICK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BRICK.Alias", "BRICK");
				config.set(name + ".Items.BRICK.Id", Integer.valueOf(45));
				items.add("BRICK");
				
				config.set(name + ".Items.BRICKSTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BRICKSTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BRICKSTAIRS.Alias", "BRICKSTAIRS");
				config.set(name + ".Items.BRICKSTAIRS.Id", Integer.valueOf(108));
				items.add("BRICKSTAIRS");
				
				config.set(name + ".Items.BROWNMUSHROOM.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BROWNMUSHROOM.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BROWNMUSHROOM.Alias", "BROWNMUSHROOM");
				config.set(name + ".Items.BROWNMUSHROOM.Id", Integer.valueOf(39));
				items.add("BROWNMUSHROOM");
				
				config.set(name + ".Items.BUCKET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BUCKET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BUCKET.Alias", "BUCKET");
				config.set(name + ".Items.BUCKET.Id", Integer.valueOf(325));
				items.add("BUCKET");
				
				config.set(name + ".Items.CACTUS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CACTUS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CACTUS.Alias", "CACTUS");
				config.set(name + ".Items.CACTUS.Id", Integer.valueOf(81));
				items.add("CACTUS");
				
				config.set(name + ".Items.CAKE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CAKE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CAKE.Alias", "CAKE");
				config.set(name + ".Items.CAKE.Id", Integer.valueOf(354));
				items.add("CAKE");
				
				config.set(name + ".Items.CARROT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CARROT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CARROT.Alias", "CARROT");
				config.set(name + ".Items.CARROT.Id", Integer.valueOf(391));
				
				config.set(name + ".Items.CAULDRON.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CAULDRON.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CAULDRON.Alias", "CAULDRON");
				config.set(name + ".Items.CAULDRON.Id", Integer.valueOf(380));
				items.add("CAULDRON");
				
				config.set(name + ".Items.CHAINMAILBOOTS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CHAINMAILBOOTS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CHAINMAILBOOTS.Alias", "CHAINMAILBOOTS");
				config.set(name + ".Items.CHAINMAILBOOTS.Id", Integer.valueOf(305));
				items.add("CHAINMAILBOOTS");
				
				config.set(name + ".Items.CHAINMAILCHESTPLATE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CHAINMAILCHESTPLATE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CHAINMAILCHESTPLATE.Alias", "CHAINMAILCHESTPLATE");
				config.set(name + ".Items.CHAINMAILCHESTPLATE.Id", Integer.valueOf(303));
				items.add("CHAINMAILCHESTPLATE");
				
				config.set(name + ".Items.CHAINMAILHELMET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CHAINMAILHELMET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CHAINMAILHELMET.Alias", "CHAINMAILHELMET");
				config.set(name + ".Items.CHAINMAILHELMET.Id", Integer.valueOf(302));
				items.add("CHAINMAILHELMET");
				
				config.set(name + ".Items.CHAINMAILLEGGINGS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CHAINMAILLEGGINGS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CHAINMAILLEGGINGS.Alias", "CHAINMAILLEGGINGS");
				config.set(name + ".Items.CHAINMAILLEGGINGS.Id", Integer.valueOf(304));
				items.add("CHAINMAILLEGGINGS");
				
				config.set(name + ".Items.CHEST.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CHEST.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CHEST.Alias", "CHEST");
				config.set(name + ".Items.CHEST.Id", Integer.valueOf(54));
				items.add("CHEST");
				
				config.set(name + ".Items.CLAY.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CLAY.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CLAY.Alias", "CLAY");
				config.set(name + ".Items.CLAY.Id", Integer.valueOf(82));
				items.add("CLAY");

				config.set(name + ".Items.CLAYBALL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CLAYBALL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CLAYBALL.Alias", "CLAYBALL");
				config.set(name + ".Items.CLAYBALL.Id", Integer.valueOf(337));
				items.add("CLAYBALL");
				
				config.set(name + ".Items.CLAYBRICK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CLAYBRICK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CLAYBRICK.Alias", "CLAYBRICK");
				config.set(name + ".Items.CLAYBRICK.Id", Integer.valueOf(336));
				items.add("CLAYBRICK");
				
				config.set(name + ".Items.COAL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COAL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COAL.Alias", "COAL");
				config.set(name + ".Items.COAL.Id", Integer.valueOf(263));
				items.add("COAL");
				
				config.set(name + ".Items.COBBLEWALL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COBBLEWALL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COBBLEWALL.Alias", "COBBLEWALL");
				config.set(name + ".Items.COBBLEWALL.Id", Integer.valueOf(139));
				items.add("COBBLEWALL");
				
				config.set(name + ".Items.COBBLESTONE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COBBLESTONE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COBBLESTONE.Alias", "COBBLESTONE");
				config.set(name + ".Items.COBBLESTONE.Id", Integer.valueOf(4));
				items.add("COBBLESTONE");
				
				config.set(name + ".Items.COBBLESTONESTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COBBLESTONESTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COBBLESTONESTAIRS.Alias", "COBBLESTONESTAIRS");
				config.set(name + ".Items.COBBLESTONESTAIRS.Id", Integer.valueOf(67));
				items.add("COBBLESTONESTAIRS");
				
				config.set(name + ".Items.ROSERED.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ROSERED.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ROSERED.Alias", "ROSERED");
				config.set(name + ".Items.ROSERED.Id", Integer.valueOf(351));
				config.set(name + ".Items.ROSERED.Durability", Integer.valueOf(1));
				items.add("ROSERED");
				
				config.set(name + ".Items.CACTUSGREEN.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CACTUSGREEN.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CACTUSGREEN.Alias", "CACTUSGREEN");
				config.set(name + ".Items.CACTUSGREEN.Id", Integer.valueOf(351));
				config.set(name + ".Items.CACTUSGREEN.Durability", Integer.valueOf(2));
				items.add("CACTUSGREEN");
				
				config.set(name + ".Items.COCOA.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COCOA.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COCOA.Alias", "COCOA");
				config.set(name + ".Items.COCOA.Id", Integer.valueOf(351));
				config.set(name + ".Items.COCOA.Durability", Integer.valueOf(3));
				items.add("COCOA");
				
				config.set(name + ".Items.LAPISLAZULI.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LAPISLAZULI.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LAPISLAZULI.Alias", "LAPISLAZULI");
				config.set(name + ".Items.LAPISLAZULI.Id", Integer.valueOf(351));
				config.set(name + ".Items.LAPISLAZULI.Durability", Integer.valueOf(4));
				items.add("LAPISLAZULI");
				
				config.set(name + ".Items.PURPLEDYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PURPLEDYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PURPLEDYE.Alias", "PURPLEDYE");
				config.set(name + ".Items.PURPLEDYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.PURPLEDYE.Durability", Integer.valueOf(5));
				items.add("PURPLEDYE");
				
				config.set(name + ".Items.CYANDYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CYANDYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CYANDYE.Alias", "CYANDYE");
				config.set(name + ".Items.CYANDYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.CYANDYE.Durability", Integer.valueOf(6));
				items.add("CYANDYE");
				
				config.set(name + ".Items.LIGHTGRAYDYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTGRAYDYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTGRAYDYE.Alias", "LIGHTGRAYDYE");
				config.set(name + ".Items.LIGHTGRAYDYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.LIGHTGRAYDYE.Durability", Integer.valueOf(7));
				items.add("LIGHTGRAYDYE");
				
				config.set(name + ".Items.GRAYDYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GRAYDYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GRAYDYE.Alias", "LIGHTGRAYDYE");
				config.set(name + ".Items.GRAYDYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.GRAYDYE.Durability", Integer.valueOf(8));
				items.add("GRAYDYE");
				
				config.set(name + ".Items.PINKDYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PINKDYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PINKDYE.Alias", "PINKDYE");
				config.set(name + ".Items.PINKDYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.PINKDYE.Durability", Integer.valueOf(9));
				items.add("PINKDYE");
				
				config.set(name + ".Items.LIMEDYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LIMEDYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LIMEDYE.Alias", "LIMEDYE");
				config.set(name + ".Items.LIMEDYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.LIMEDYE.Durability", Integer.valueOf(10));
				items.add("LIMEDYE");
				
				config.set(name + ".Items.DANDELIONYELLOW.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DANDELIONYELLOW.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DANDELIONYELLOW.Alias", "DANDELIONYELLOW");
				config.set(name + ".Items.DANDELIONYELLOW.Id", Integer.valueOf(351));
				config.set(name + ".Items.DANDELIONYELLOW.Durability", Integer.valueOf(11));
				items.add("DANDELIONYELLOW");
				
				config.set(name + ".Items.LIGHTBLUEDYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTBLUEDYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTBLUEDYE.Alias", "LIGHTBLUEDYE");
				config.set(name + ".Items.LIGHTBLUEDYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.LIGHTBLUEDYE.Durability", Integer.valueOf(12));
				items.add("LIGHTBLUEDYE");
				
				config.set(name + ".Items.MAGENTADYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MAGENTADYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MAGENTADYE.Alias", "MAGENTADYE");
				config.set(name + ".Items.MAGENTADYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.MAGENTADYE.Durability", Integer.valueOf(13));
				items.add("MAGENTADYE");
				
				config.set(name + ".Items.ORANGEDYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ORANGEDYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ORANGEDYE.Alias", "ORANGEDYE");
				config.set(name + ".Items.ORANGEDYE.Id", Integer.valueOf(351));
				config.set(name + ".Items.ORANGEDYE.Durability", Integer.valueOf(14));
				items.add("ORANGEDYE");
				
				config.set(name + ".Items.BONEMEAL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BONEMEAL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BONEMEAL.Alias", "BONEMEAL");
				config.set(name + ".Items.BONEMEAL.Id", Integer.valueOf(351));
				config.set(name + ".Items.BONEMEAL.Durability", Integer.valueOf(15));
				items.add("BONEMEAL");
				
				config.set(name + ".Items.COMPASS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COMPASS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COMPASS.Alias", "COMPASS");
				config.set(name + ".Items.COMPASS.Id", Integer.valueOf(345));
				items.add("COMPASS");
				
				config.set(name + ".Items.COOKEDBEEF.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COOKEDBEEF.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COOKEDBEEF.Alias", "COOKEDBEEF");
				config.set(name + ".Items.COOKEDBEEF.Id", Integer.valueOf(364));
				items.add("COOKEDBEEF");
				
				config.set(name + ".Items.COOKEDCHICKEN.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COOKEDCHICKEN.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COOKEDCHICKEN.Alias", "COOKEDCHICKEN");
				config.set(name + ".Items.COOKEDCHICKEN.Id", Integer.valueOf(366));
				items.add("COOKEDCHICKEN");
				
				config.set(name + ".Items.COOKEDFISH.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COOKEDFISH.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COOKEDFISH.Alias", "COOKEDFISH");
				config.set(name + ".Items.COOKEDFISH.Id", Integer.valueOf(350));
				items.add("COOKEDFISH");
				
				config.set(name + ".Items.COOKIE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.COOKIE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.COOKIE.Alias", "COOKIE");
				config.set(name + ".Items.COOKIE.Id", Integer.valueOf(357));
				items.add("COOKIE");
				
				config.set(name + ".Items.DETECTORRAIL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DETECTORRAIL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DETECTORRAIL.Alias", "DETECTORRAIL");
				config.set(name + ".Items.DETECTORRAIL.Id", Integer.valueOf(28));
				items.add("DETECTORRAIL");
				
				config.set(name + ".Items.DIAMOND.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMOND.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMOND.Alias", "DIAMOND");
				config.set(name + ".Items.DIAMOND.Id", Integer.valueOf(264));
				items.add("DIAMOND");
				
				config.set(name + ".Items.DIAMONDAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDAXE.Alias", "DIAMONDAXE");
				config.set(name + ".Items.DIAMONDAXE.Id", Integer.valueOf(279));
				items.add("DIAMONDAXE");
				
				config.set(name + ".Items.DIAMONDBLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDBLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDBLOCK.Alias", "DIAMONDBLOCK");
				config.set(name + ".Items.DIAMONDBLOCK.Id", Integer.valueOf(57));
				items.add("DIAMONDBLOCK");
				
				config.set(name + ".Items.DIAMONDBOOTS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDBOOTS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDBOOTS.Alias", "DIAMONDBOOTS");
				config.set(name + ".Items.DIAMONDBOOTS.Id", Integer.valueOf(313));
				items.add("DIAMONDBOOTS");
				
				config.set(name + ".Items.DIAMONDCHESTPLATE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDCHESTPLATE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDCHESTPLATE.Alias", "DIAMONDCHESTPLATE");
				config.set(name + ".Items.DIAMONDCHESTPLATE.Id", Integer.valueOf(311));
				items.add("DIAMONDCHESTPLATE");
				
				config.set(name + ".Items.DIAMONDHELMET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDHELMET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDHELMET.Alias", "DIAMONDHELMET");
				config.set(name + ".Items.DIAMONDHELMET.Id", Integer.valueOf(310));
				items.add("DIAMONDHELMET");
				
				config.set(name + ".Items.DIAMONDHOE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDHOE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDHOE.Alias", "DIAMONDHOE");
				config.set(name + ".Items.DIAMONDHOE.Id", Integer.valueOf(293));
				items.add("DIAMONDHOE");
				
				config.set(name + ".Items.DIAMONDLEGGINGS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDLEGGINGS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDLEGGINGS.Alias", "DIAMONDLEGGINGS");
				config.set(name + ".Items.DIAMONDLEGGINGS.Id", Integer.valueOf(312));
				items.add("DIAMONDLEGGINGS");
				
				config.set(name + ".Items.DIAMONDPICKAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDPICKAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDPICKAXE.Alias", "DIAMONDPICKAXE");
				config.set(name + ".Items.DIAMONDPICKAXE.Id", Integer.valueOf(278));
				items.add("DIAMONDPICKAXE");
				
				config.set(name + ".Items.DIAMONDSPADE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDSPADE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDSPADE.Alias", "DIAMONDSPADE");
				config.set(name + ".Items.DIAMONDSPADE.Id", Integer.valueOf(277));
				items.add("DIAMONDSPADE");
				
				config.set(name + ".Items.DIAMONDSWORD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDSWORD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIAMONDSWORD.Alias", "DIAMONDSWORD");
				config.set(name + ".Items.DIAMONDSWORD.Id", Integer.valueOf(276));
				items.add("DIAMONDSWORD");
				
				config.set(name + ".Items.REDSTONEREPEATER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.REDSTONEREPEATER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.REDSTONEREPEATER.Alias", "REDSTONEREPEATER");
				config.set(name + ".Items.REDSTONEREPEATER.Id", Integer.valueOf(356));
				items.add("REDSTONEREPEATER");
				
				config.set(name + ".Items.DIRT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DIRT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DIRT.Alias", "DIRT");
				config.set(name + ".Items.DIRT.Id", Integer.valueOf(3));
				items.add("DIRT");
				
				config.set(name + ".Items.DISPENSER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DISPENSER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DISPENSER.Alias", "DISPENSER");
				config.set(name + ".Items.DISPENSER.Id", Integer.valueOf(23));
				items.add("DISPENSER");

				config.set(name + ".Items.EGG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.EGG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.EGG.Alias", "EGG");
				config.set(name + ".Items.EGG.Id", Integer.valueOf(344));
				items.add("EGG");
				
				config.set(name + ".Items.EMERALD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.EMERALD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.EMERALD.Alias", "EMERALD");
				config.set(name + ".Items.EMERALD.Id", Integer.valueOf(388));
				items.add("EMERALD");
				
				config.set(name + ".Items.EMERALDBLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.EMERALDBLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.EMERALDBLOCK.Alias", "EMERALDBLOCK");
				config.set(name + ".Items.EMERALDBLOCK.Id", Integer.valueOf(133));
				items.add("EMERALDBLOCK");
				
				config.set(name + ".Items.EMPTYMAP.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.EMPTYMAP.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.EMPTYMAP.Alias", "EMPTYMAP");
				config.set(name + ".Items.EMPTYMAP.Id", Integer.valueOf(395));
				items.add("EMPTYMAP");
				
				config.set(name + ".Items.ENCHANTMENTTABLE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ENCHANTMENTTABLE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ENCHANTMENTTABLE.Alias", "ENCHANTMENTTABLE");
				config.set(name + ".Items.ENCHANTMENTTABLE.Id", Integer.valueOf(116));
				items.add("ENCHANTMENTTABLE");
				
				config.set(name + ".Items.ENDERCHEST.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ENDERCHEST.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ENDERCHEST.Alias", "ENDERCHEST");
				config.set(name + ".Items.ENDERCHEST.Id", Integer.valueOf(130));
				items.add("ENDERCHEST");
				
				config.set(name + ".Items.ENDERPEARL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ENDERPEARL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ENDERPEARL.Alias", "ENDERPEARL");
				config.set(name + ".Items.ENDERPEARL.Id", Integer.valueOf(368));
				items.add("ENDERPEARL");
				
				config.set(name + ".Items.FEATHER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FEATHER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FEATHER.Alias", "FEATHER");
				config.set(name + ".Items.FEATHER.Id", Integer.valueOf(288));
				items.add("FEATHER");
				
				config.set(name + ".Items.FENCE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FENCE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FENCE.Alias", "FENCE");
				config.set(name + ".Items.FENCE.Id", Integer.valueOf(85));
				items.add("FENCE");
				
				config.set(name + ".Items.FENCEGATE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FENCEGATE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FENCEGATE.Alias", "FENCEGATE");
				config.set(name + ".Items.FENCEGATE.Id", Integer.valueOf(107));
				items.add("FENCEGATE");
				
				config.set(name + ".Items.FERMENTEDSPIDEREYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FERMENTEDSPIDEREYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FERMENTEDSPIDEREYE.Alias", "FERMENTEDSPIDEREYE");
				config.set(name + ".Items.FERMENTEDSPIDEREYE.Id", Integer.valueOf(376));
				items.add("FERMENTEDSPIDEREYE");
				
				config.set(name + ".Items.FISHINGROD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FISHINGROD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FISHINGROD.Alias", "FISHINGROD");
				config.set(name + ".Items.FISHINGROD.Id", Integer.valueOf(346));
				items.add("FISHINGROD");
				
				config.set(name + ".Items.FLINT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FLINT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FLINT.Alias", "FLINT");
				config.set(name + ".Items.FLINT.Id", Integer.valueOf(318));
				items.add("FLINT");
				
				config.set(name + ".Items.FLOWERPOT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FLOWERPOT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FLOWERPOT.Alias", "FLOWERPOT");
				config.set(name + ".Items.FLOWERPOT.Id", Integer.valueOf(390));
				items.add("FLOWERPOT");
				
				config.set(name + ".Items.FURNACE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FURNACE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FURNACE.Alias", "FURNACE");
				config.set(name + ".Items.FURNACE.Id", Integer.valueOf(61));
				items.add("FURNACE");
				
				config.set(name + ".Items.GHASTTEAR.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GHASTTEAR.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GHASTTEAR.Alias", "GHASTTEAR");
				config.set(name + ".Items.GHASTTEAR.Id", Integer.valueOf(370));
				items.add("GHASTTEAR");
				
				config.set(name + ".Items.GLASS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GLASS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GLASS.Alias", "GLASS");
				config.set(name + ".Items.GLASS.Id", Integer.valueOf(20));
				items.add("GLASS");
				
				config.set(name + ".Items.GLASSBOTTLE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GLASSBOTTLE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GLASSBOTTLE.Alias", "GLASSBOTTLE");
				config.set(name + ".Items.GLASSBOTTLE.Id", Integer.valueOf(374));
				items.add("GLASSBOTTLE");
				
				config.set(name + ".Items.GLOWSTONE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GLOWSTONE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GLOWSTONE.Alias", "GLOWSTONE");
				config.set(name + ".Items.GLOWSTONE.Id", Integer.valueOf(89));
				items.add("GLOWSTONE");
				
				config.set(name + ".Items.GLOWSTONEDUST.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GLOWSTONEDUST.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GLOWSTONEDUST.Alias", "GLOWSTONEDUST");
				config.set(name + ".Items.GLOWSTONEDUST.Id", Integer.valueOf(348));
				items.add("GLOWSTONEDUST");
				
				config.set(name + ".Items.GOLDAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDAXE.Alias", "GOLDAXE");
				config.set(name + ".Items.GOLDAXE.Id", Integer.valueOf(286));
				items.add("GOLDAXE");
				
				config.set(name + ".Items.GOLDBLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDBLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDBLOCK.Alias", "GOLDBLOCK");
				config.set(name + ".Items.GOLDBLOCK.Id", Integer.valueOf(41));
				items.add("GOLDBLOCK");
				
				config.set(name + ".Items.GOLDBOOTS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDBOOTS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDBOOTS.Alias", "GOLDBOOTS");
				config.set(name + ".Items.GOLDBOOTS.Id", Integer.valueOf(317));
				items.add("GOLDBOOTS");
				
				config.set(name + ".Items.GOLDHOE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDHOE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDHOE.Alias", "GOLDHOE");
				config.set(name + ".Items.GOLDHOE.Id", Integer.valueOf(294));
				items.add("GOLDHOE");
				
				config.set(name + ".Items.GOLDCHESTPLATE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDCHESTPLATE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDCHESTPLATE.Alias", "GOLDCHESTPLATE");
				config.set(name + ".Items.GOLDCHESTPLATE.Id", Integer.valueOf(315));
				items.add("GOLDCHESTPLATE");
				
				config.set(name + ".Items.GOLDHELMET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDHELMET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDHELMET.Alias", "GOLDHELMET");
				config.set(name + ".Items.GOLDHELMET.Id", Integer.valueOf(314));
				items.add("GOLDHELMET");

				config.set(name + ".Items.GOLDINGOT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDINGOT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDINGOT.Alias", "GOLDINGOT");
				config.set(name + ".Items.GOLDINGOT.Id", Integer.valueOf(266));
				items.add("GOLDINGOT");
				
				config.set(name + ".Items.GOLDLEGGINGS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDLEGGINGS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDLEGGINGS.Alias", "GOLDLEGGINGS");
				config.set(name + ".Items.GOLDLEGGINGS.Id", Integer.valueOf(316));
				items.add("GOLDLEGGINGS");
				
				config.set(name + ".Items.GOLDNUGGET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDNUGGET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDNUGGET.Alias", "GOLDNUGGET");
				config.set(name + ".Items.GOLDNUGGET.Id", Integer.valueOf(371));
				items.add("GOLDNUGGET");
				
				config.set(name + ".Items.GOLDPICKAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDPICKAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDPICKAXE.Alias", "GOLDPICKAXE");
				config.set(name + ".Items.GOLDPICKAXE.Id", Integer.valueOf(285));
				items.add("GOLDPICKAXE");
				
				config.set(name + ".Items.GOLDRECORD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDRECORD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDRECORD.Alias", "GOLDRECORD");
				config.set(name + ".Items.GOLDRECORD.Id", Integer.valueOf(2256));
				items.add("GOLDRECORD");
				
				config.set(name + ".Items.GOLDSPADE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDSPADE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDSPADE.Alias", "GOLDSPADE");
				config.set(name + ".Items.GOLDSPADE.Id", Integer.valueOf(284));
				items.add("GOLDSPADE");
				
				config.set(name + ".Items.GOLDSWORD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDSWORD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDSWORD.Alias", "GOLDSWORD");
				config.set(name + ".Items.GOLDSWORD.Id", Integer.valueOf(283));
				items.add("GOLDSWORD");
				
				config.set(name + ".Items.GOLDENAPPLE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDENAPPLE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDENAPPLE.Alias", "GOLDENAPPLE");
				config.set(name + ".Items.GOLDENAPPLE.Id", Integer.valueOf(322));
				items.add("GOLDENAPPLE");
				
				config.set(name + ".Items.GOLDENCARROT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDENCARROT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GOLDENCARROT.Alias", "GOLDENCARROT");
				config.set(name + ".Items.GOLDENCARROT.Id", Integer.valueOf(396));
				items.add("GOLDECARROT");
				
				config.set(name + ".Items.GRAVEL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GRAVEL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GRAVEL.Alias", "GRAVEL");
				config.set(name + ".Items.GRAVEL.Id", Integer.valueOf(13));
				items.add("GRAVEL");
				
				config.set(name + ".Items.GREENRECORD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GREENRECORD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GREENRECORD.Alias", "GREENRECORD");
				config.set(name + ".Items.GREENRECORD.Id", Integer.valueOf(2257));
				items.add("GREENRECORD");
				
				config.set(name + ".Items.GRILLEDPORK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GRILLEDPORK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GRILLEDPORK.Alias", "COOKEDPORKCHOP");
				config.set(name + ".Items.GRILLEDPORK.Id", Integer.valueOf(320));
				items.add("GRILLEDPORK");
				
				config.set(name + ".Items.INKSACK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.INKSACK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.INKSACK.Alias", "INKSACK");
				config.set(name + ".Items.INKSACK.Id", Integer.valueOf(351));
				items.add("INKSACK");
				
				config.set(name + ".Items.IRONAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONAXE.Alias", "IRONAXE");
				config.set(name + ".Items.IRONAXE.Id", Integer.valueOf(258));
				items.add("IRONAXE");
				
				config.set(name + ".Items.IRONBLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONBLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONBLOCK.Alias", "IRONBLOCK");
				config.set(name + ".Items.IRONBLOCK.Id", Integer.valueOf(42));
				items.add("IRONBLOCK");
				
				config.set(name + ".Items.IRONBOOTS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONBOOTS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONBOOTS.Alias", "IRONBOOTS");
				config.set(name + ".Items.IRONBOOTS.Id", Integer.valueOf(309));
				items.add("IRONBOOTS");
				
				config.set(name + ".Items.IRONCHESTPLATE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONCHESTPLATE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONCHESTPLATE.Alias", "IRONCHESTPLATE");
				config.set(name + ".Items.IRONCHESTPLATE.Id", Integer.valueOf(307));
				items.add("IRONCHESTPLATE");
				
				config.set(name + ".Items.IRONDOOR.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONDOOR.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONDOOR.Alias", "IRONDOOR");
				config.set(name + ".Items.IRONDOOR.Id", Integer.valueOf(330));
				items.add("IRONDOOR");
				
				config.set(name + ".Items.IRONFENCE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONFENCE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONFENCE.Alias", "IRONBARS");
				config.set(name + ".Items.IRONFENCE.Id", Integer.valueOf(101));
				items.add("IRONFENCE");
				
				config.set(name + ".Items.IRONHELMET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONHELMET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONHELMET.Alias", "IRONHELMET");
				config.set(name + ".Items.IRONHELMET.Id", Integer.valueOf(306));
				items.add("IRONHELMET");
				
				config.set(name + ".Items.IRONHOE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONHOE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONHOE.Alias", "IRONHOE");
				config.set(name + ".Items.IRONHOE.Id", Integer.valueOf(292));
				items.add("IRONHOE");
				
				config.set(name + ".Items.IRONINGOT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONINGOT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONINGOT.Alias", "IRONINGOT");
				config.set(name + ".Items.IRONINGOT.Id", Integer.valueOf(265));
				items.add("IRONINGOT");
				
				config.set(name + ".Items.IRONLEGGINGS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONLEGGINGS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONLEGGINGS.Alias", "IRONLEGGINGS");
				config.set(name + ".Items.IRONLEGGINGS.Id", Integer.valueOf(308));
				items.add("IRONLEGGINGS");
				
				config.set(name + ".Items.IRONPICKAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONPICKAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONPICKAXE.Alias", "IRONPICKAXE");
				config.set(name + ".Items.IRONPICKAXE.Id", Integer.valueOf(257));
				items.add("IRONPICKAXE");
				
				config.set(name + ".Items.IRONSHOVEL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONSHOVEL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONSHOVEL.Alias", "IRONSHOVEL");
				config.set(name + ".Items.IRONSHOVEL.Id", Integer.valueOf(256));
				items.add("IRONSHOVEL");
				
				config.set(name + ".Items.IRONSWORD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONSWORD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.IRONSWORD.Alias", "IRONSWORD");
				config.set(name + ".Items.IRONSWORD.Id", Integer.valueOf(267));
				items.add("IRONSWORD");
				
				config.set(name + ".Items.ITEMFRAME.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ITEMFRAME.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ITEMFRAME.Alias", "ITEMFRAME");
				config.set(name + ".Items.ITEMFRAME.Id", Integer.valueOf(389));
				items.add("ITEMFRAME");
				
				config.set(name + ".Items.JACKOLANTERN.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.JACKOLANTERN.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.JACKOLANTERN.Alias", "JACKOLANTERN");
				config.set(name + ".Items.JACKOLANTERN.Id", Integer.valueOf(91));
				items.add("JACKOLANTERN");
				
				config.set(name + ".Items.JUKEBOX.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.JUKEBOX.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.JUKEBOX.Alias", "JUKEBOX");
				config.set(name + ".Items.JUKEBOX.Id", Integer.valueOf(84));
				items.add("JUKEBOX");
				
				config.set(name + ".Items.JUNGLEWOODSTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLEWOODSTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLEWOODSTAIRS.Alias", "JUNGLEWOODSTAIRS");
				config.set(name + ".Items.JUNGLEWOODSTAIRS.Id", Integer.valueOf(136));
				items.add("JUNGLEWOODSTAIRS");

				config.set(name + ".Items.LADDER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LADDER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LADDER.Alias", "LADDER");
				config.set(name + ".Items.LADDER.Id", Integer.valueOf(65));
				items.add("LADDER");
				
				config.set(name + ".Items.LAPISBLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LAPISBLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LAPISBLOCK.Alias", "LAPISBLOCK");
				config.set(name + ".Items.LAPISBLOCK.Id", Integer.valueOf(22));
				items.add("LAPISBLOCK");
				
				config.set(name + ".Items.LAPISLAZULIBLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LAPISLAZULIBLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LAPISLAZULIBLOCK.Alias", "LAPISBLOCK");
				config.set(name + ".Items.LAPISLAZULIBLOCK.Id", Integer.valueOf(22));
				items.add("LAPISLAZULIBLOCK");
				
				config.set(name + ".Items.LEATHER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHER.Alias", "LEATHER");
				config.set(name + ".Items.LEATHER.Id", Integer.valueOf(334));
				items.add("LEATHER");
				
				config.set(name + ".Items.LEATHERBOOTS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHERBOOTS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHERBOOTS.Alias", "LEATHERBOOTS");
				config.set(name + ".Items.LEATHERBOOTS.Id", Integer.valueOf(301));
				items.add("LEATHERBOOTS");
				
				config.set(name + ".Items.LEATHERCHESTPLATE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHERCHESTPLATE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHERCHESTPLATE.Alias", "LEATHERCHESTPLATE");
				config.set(name + ".Items.LEATHERCHESTPLATE.Id", Integer.valueOf(299));
				items.add("LEATHERCHESTPLATE");
				
				config.set(name + ".Items.LEATHERHELMET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHERHELMET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHERHELMET.Alias", "LEATHERHELMET");
				config.set(name + ".Items.LEATHERHELMET.Id", Integer.valueOf(298));
				items.add("LEATHERHELMET");
				
				config.set(name + ".Items.LEATHERLEGGINGS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHERLEGGINGS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LEATHERLEGGINGS.Alias", "LEATHERLEGGINGS");
				config.set(name + ".Items.LEATHERLEGGINGS.Id", Integer.valueOf(300));
				items.add("LEATHERLEGGINGS");
				
				config.set(name + ".Items.LEVER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LEVER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LEVER.Alias", "LEVER");
				config.set(name + ".Items.LEVER.Id", Integer.valueOf(69));
				items.add("LEVER");
				
				config.set(name + ".Items.WOOD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOOD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOOD.Alias", "WOOD");
				config.set(name + ".Items.WOOD.Id", Integer.valueOf(5));
				items.add("WOOD");
				
				config.set(name + ".Items.BIRCHLOG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHLOG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHLOG.Alias", "BIRCHLOG");
				config.set(name + ".Items.BIRCHLOG.Id", Integer.valueOf(17));
				config.set(name + ".Items.BIRCHLOG.Durability", Integer.valueOf(2));
				items.add("BIRCHLOG");
				
				config.set(name + ".Items.SPRUCELOG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCELOG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCELOG.Alias", "SPRUCELOG");
				config.set(name + ".Items.SPRUCELOG.Id", Integer.valueOf(17));
				config.set(name + ".Items.SPRUCELOG.Durability", Integer.valueOf(1));
				items.add("SPRUCELOG");
				
				config.set(name + ".Items.REDWOODLOG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.REDWOODLOG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.REDWOODLOG.Alias", "REDWOODLOG");
				config.set(name + ".Items.REDWOODLOG.Id", Integer.valueOf(17));
				config.set(name + ".Items.REDWOODLOG.Durability", Integer.valueOf(1));
				items.add("REDWOODLOG");
				
				config.set(name + ".Items.LOG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LOG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LOG.Alias", "LOG");
				config.set(name + ".Items.LOG.Id", Integer.valueOf(17));
				items.add("LOG");
				
				config.set(name + ".Items.OAKLOG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKLOG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKLOG.Alias", "OAKLOG");
				config.set(name + ".Items.OAKLOG.Id", Integer.valueOf(17));
				items.add("OAKLOG");
				
				config.set(name + ".Items.OAKWOODLOG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKWOODLOG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKWOODLOG.Alias", "OAKWOODLOG");
				config.set(name + ".Items.OAKWOODLOG.Id", Integer.valueOf(17));
				items.add("OAKWOODLOG");
				
				config.set(name + ".Items.JUNGLELOG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLELOG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLELOG.Alias", "JUNGLELOG");
				config.set(name + ".Items.JUNGLELOG.Id", Integer.valueOf(17));
				config.set(name + ".Items.JUNGLELOG.Durability", Integer.valueOf(3));
				items.add("JUNGLELOG");
				
				config.set(name + ".Items.JUNGLEWOODLOG.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLEWOODLOG.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLEWOODLOG.Alias", "JUNGLEWOODLOG");
				config.set(name + ".Items.JUNGLEWOODLOG.Id", Integer.valueOf(17));
				config.set(name + ".Items.JUNGLEWOODLOG.Durability", Integer.valueOf(3));
				items.add("JUNGLEWOODLOG");
				
				config.set(name + ".Items.WOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOOL.Alias", "WOOL");
				config.set(name + ".Items.WOOL.Id", Integer.valueOf(35));
				items.add("WOOL");
				
				config.set(name + ".Items.WHITEWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WHITEWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WHITEWOOL.Alias", "WHITEWOOL");
				config.set(name + ".Items.WHITEWOOL.Id", Integer.valueOf(35));
				items.add("WHITEWOOL");
				
				config.set(name + ".Items.ORANGEWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ORANGEWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ORANGEWOOL.Alias", "ORANGEWOOL");
				config.set(name + ".Items.ORANGEWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.ORANGEWOOL.Durability", Integer.valueOf(1));
				items.add("ORANGEWOOL");
				
				config.set(name + ".Items.MAGENTAWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MAGENTAWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MAGENTAWOOL.Alias", "MAGENTAWOOL");
				config.set(name + ".Items.MAGENTAWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.MAGENTAWOOL.Durability", Integer.valueOf(2));
				items.add("MAGENTAWOOL");
				
				config.set(name + ".Items.LIGHTBLUEWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTBLUEWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTBLUEWOOL.Alias", "LIGHTBLUEWOOL");
				config.set(name + ".Items.LIGHTBLUEWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.LIGHTBLUEWOOL.Durability", Integer.valueOf(3));
				items.add("LIGHTBLUEWOOL");
				
				config.set(name + ".Items.YELLOWWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.YELLOWWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.YELLOWWOOL.Alias", "YELLOWWOOL");
				config.set(name + ".Items.YELLOWWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.YELLOWWOOL.Durability", Integer.valueOf(4));
				items.add("YELLOWWOOL");
				
				config.set(name + ".Items.LIGHTGREENWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTGREENWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTGREENWOOL.Alias", "LIGHTGREENWOOL");
				config.set(name + ".Items.LIGHTGREENWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.LIGHTGREENWOOL.Durability", Integer.valueOf(5));
				items.add("LIGHTGREENWOOL");
				
				config.set(name + ".Items.PINKWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PINKWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PINKWOOL.Alias", "ORANGEWOOL");
				config.set(name + ".Items.PINKWOOL.Id", Integer.valueOf(6));
				config.set(name + ".Items.PINKWOOL.Durability", Integer.valueOf(6));
				items.add("PINKWOOL");
				
				config.set(name + ".Items.GRAYWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GRAYWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GRAYWOOL.Alias", "GRAYWOOL");
				config.set(name + ".Items.GRAYWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.GRAYWOOL.Durability", Integer.valueOf(7));
				items.add("GRAYWOOL");
				
				config.set(name + ".Items.LIGHTGRAYWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTGRAYWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.LIGHTGRAYWOOL.Alias", "LIGHTGRAYWOOL");
				config.set(name + ".Items.LIGHTGRAYWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.LIGHTGRAYWOOL.Durability", Integer.valueOf(8));
				items.add("LIGHTGRAYWOOL");
				
				config.set(name + ".Items.CYANWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CYANWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CYANWOOL.Alias", "CYANWOOL");
				config.set(name + ".Items.CYANWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.CYANWOOL.Durability", Integer.valueOf(9));
				items.add("CYANWOOL");
				
				config.set(name + ".Items.PURPLEWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PURPLEWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PURPLEWOOL.Alias", "PURPLEWOOL");
				config.set(name + ".Items.PURPLEWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.PURPLEWOOL.Durability", Integer.valueOf(10));
				items.add("PURPLEWOOL");
				
				config.set(name + ".Items.BLUEWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BLUEWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BLUEWOOL.Alias", "BLUEWOOL");
				config.set(name + ".Items.BLUEWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.BLUEWOOL.Durability", Integer.valueOf(11));
				items.add("BLUEWOOL");
				
				config.set(name + ".Items.BROWNWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BROWNWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BROWNWOOL.Alias", "BROWNWOOL");
				config.set(name + ".Items.BROWNWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.BROWNWOOL.Durability", Integer.valueOf(12));
				items.add("BROWNWOOL");
				
				config.set(name + ".Items.DARKGREENWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DARKGREENWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DARKGREENWOOL.Alias", "DARKGREENWOOL");
				config.set(name + ".Items.DARKGREENWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.DARKGREENWOOL.Durability", Integer.valueOf(13));
				items.add("DARGREENWOOL");
				
				config.set(name + ".Items.REDWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.REDWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.REDWOOL.Alias", "REDWOOL");
				config.set(name + ".Items.REDWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.REDWOOL.Durability", Integer.valueOf(14));
				items.add("REDWOOL");
				
				config.set(name + ".Items.BLACKWOOL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BLACKWOOL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BLACKWOOL.Alias", "BLACKWOOL");
				config.set(name + ".Items.BLACKWOOL.Id", Integer.valueOf(35));
				config.set(name + ".Items.BLACKWOOL.Durability", Integer.valueOf(15));
				items.add("BLACKWOOL");
				
				config.set(name + ".Items.MAGMACREAM.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MAGMACREAM.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MAGMACREAM.Alias", "MAGMACREAM");
				config.set(name + ".Items.MAGMACREAM.Id", Integer.valueOf(378));
				items.add("MAGMACREAM");
				
				config.set(name + ".Items.MAP.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MAP.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MAP.Alias", "MAP");
				config.set(name + ".Items.MAP.Id", Integer.valueOf(358));
				items.add("MAP");
				
				config.set(name + ".Items.MELON.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MELON.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MELON.Alias", "MELON");
				config.set(name + ".Items.MELON.Id", Integer.valueOf(360));
				items.add("MELON");
				
				config.set(name + ".Items.MELONBLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MELONBLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MELONBLOCK.Alias", "MELONBLOCK");
				config.set(name + ".Items.MELONBLOCK.Id", Integer.valueOf(103));
				items.add("MELONBLOCK");
				
				config.set(name + ".Items.MELONSEEDS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MELONSEEDS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MELONSEEDS.Alias", "MELONSEEDS");
				config.set(name + ".Items.MELONSEEDS.Id", Integer.valueOf(362));
				items.add("MELONSEEDS");
				
				config.set(name + ".Items.MILKBUCKET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MILKBUCKET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MILKBUCKET.Alias", "MILKBUCKET");
				config.set(name + ".Items.MILKBUCKET.Id", Integer.valueOf(335));
				items.add("MILKBUCKET");
				
				config.set(name + ".Items.MINECART.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MINECART.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MINECART.Alias", "MINECART");
				config.set(name + ".Items.MINECART.Id", Integer.valueOf(328));
				items.add("MINECART");
				
				config.set(name + ".Items.MOSSYCOBBLESTONE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MOSSYCOBBLESTONE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MOSSYCOBBLESTONE.Alias", "MOSSYCOBBLESTONE");
				config.set(name + ".Items.MOSSYCOBBLESTONE.Id", Integer.valueOf(48));
				items.add("MOSSYCOBBLESTONE");
				
				config.set(name + ".Items.MUSHROOMSOUP.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MUSHROOMSOUP.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MUSHROOMSOUP.Alias", "MUSHROOMSOUP");
				config.set(name + ".Items.MUSHROOMSOUP.Id", Integer.valueOf(282));
				items.add("MUSHROOMSOUP");
				
				config.set(name + ".Items.MYCELIUM.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MYCELIUM.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MYCELIUM.Alias", "MYCELIUM");
				config.set(name + ".Items.MYCELIUM.Id", Integer.valueOf(110));
				items.add("MYCELIUM");
				
				config.set(name + ".Items.NETHERBRICK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERBRICK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERBRICK.Alias", "NETHERBRICK");
				config.set(name + ".Items.NETHERBRICK.Id", Integer.valueOf(112));
				items.add("NETHERBRICK");
				
				config.set(name + ".Items.NETHERBRICKSTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERBRICKSTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERBRICKSTAIRS.Alias", "NETHERBRICKSTAIRS");
				config.set(name + ".Items.NETHERBRICKSTAIRS.Id", Integer.valueOf(114));
				items.add("NETHERBRICKSTAIRS");
				
				config.set(name + ".Items.NETHERBRICKFENCE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERBRICKFENCE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERBRICKFENCE.Alias", "NETHERBRICKFENCE");
				config.set(name + ".Items.NETHERBRICKFENCE.Id", Integer.valueOf(113));
				items.add("NETHERBRICKFENCE");
				
				config.set(name + ".Items.NETHERFENCE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERFENCE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERFENCE.Alias", "NETHERFENCE");
				config.set(name + ".Items.NETHERFENCE.Id", Integer.valueOf(113));
				items.add("NETHERFENCE");
				
				config.set(name + ".Items.NOTEBLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.NOTEBLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.NOTEBLOCK.Alias", "NOTEBLOCK");
				config.set(name + ".Items.NOTEBLOCK.Id", Integer.valueOf(25));
				items.add("NOTEBLOCK");
				
				config.set(name + ".Items.NETHERWART.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERWART.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.NETHERWART.Alias", "NETHERWART");
				config.set(name + ".Items.NETHERWART.Id", Integer.valueOf(372));
				items.add("NETHERWART");
				
				config.set(name + ".Items.OBSIDIAN.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.OBSIDIAN.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.OBSIDIAN.Alias", "OBSIDIAN");
				config.set(name + ".Items.OBSIDIAN.Id", Integer.valueOf(49));
				items.add("OBSIDIAN");
				
				config.set(name + ".Items.PAINTING.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PAINTING.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PAINTING.Alias", "PAINTING");
				config.set(name + ".Items.PAINTING.Id", Integer.valueOf(321));
				items.add("PAINTING");
				
				config.set(name + ".Items.PAPER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PAPER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PAPER.Alias", "PAPER");
				config.set(name + ".Items.PAPER.Id", Integer.valueOf(339));
				items.add("PAPER");

				config.set(name + ".Items.RAWPORKCHOP.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.RAWPORKCHOP.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.RAWPORKCHOP.Alias", "RAWPORKCHOP");
				config.set(name + ".Items.RAWPORKCHOP.Id", Integer.valueOf(319));
				items.add("RAWPORKCHOP");
				
				config.set(name + ".Items.POTATO.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.POTATO.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.POTATO.Alias", "POTATO");
				config.set(name + ".Items.POTATO.Id", Integer.valueOf(392));
				items.add("POTATO");
				
				config.set(name + ".Items.POWEREDMINECART.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.POWEREDMINECART.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.POWEREDMINECART.Alias", "POWEREDMINECART");
				config.set(name + ".Items.POWEREDMINECART.Id", Integer.valueOf(343));
				items.add("POWEREDMINECART");
				
				config.set(name + ".Items.PUMPKINSEEDS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PUMPKINSEEDS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PUMPKINSEEDS.Alias", "PUMPKINSEEDS");
				config.set(name + ".Items.PUMPKINSEEDS.Id", Integer.valueOf(361));
				items.add("PUMPKINSEEDS");
				
				config.set(name + ".Items.PUMPKIN.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PUMPKIN.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PUMPKIN.Alias", "PUMPKIN");
				config.set(name + ".Items.PUMPKIN.Id", Integer.valueOf(86));
				items.add("PUMPKIN");
				
				config.set(name + ".Items.PUMPKINPIE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PUMPKINPIE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PUMPKINPIE.Alias", "PUMPKINPIE");
				config.set(name + ".Items.PUMPKINPIE.Id", Integer.valueOf(400));
				items.add("PUMPKINPIE");
				
				config.set(name + ".Items.RAILS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.RAILS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.RAILS.Alias", "RAILS");
				config.set(name + ".Items.RAILS.Id", Integer.valueOf(66));
				items.add("RAILS");
				
				config.set(name + ".Items.RAWBEEF.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.RAWBEEF.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.RAWBEEF.Alias", "BEEF");
				config.set(name + ".Items.RAWBEEF.Id", Integer.valueOf(363));
				items.add("RAWBEEF");
				
				config.set(name + ".Items.RAWCHICKEN.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.RAWCHICKEN.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.RAWCHICKEN.Alias", "RAWCHICKEN");
				config.set(name + ".Items.RAWCHICKEN.Id", Integer.valueOf(365));
				items.add("RAWCHICKEN");
				
				config.set(name + ".Items.RAWFISH.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.RAWFISH.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.RAWFISH.Alias", "RAWFISH");
				config.set(name + ".Items.RAWFISH.Id", Integer.valueOf(349));
				items.add("RAWFISH");
				
				config.set(name + ".Items.13DISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.13DISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.13DISC.Alias", "13DISC");
				config.set(name + ".Items.13DISC.Id", Integer.valueOf(2256));
				items.add("13DISC");
				
				config.set(name + ".Items.CATDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CATDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CATDISC.Alias", "CATDISC");
				config.set(name + ".Items.CATDISC.Id", Integer.valueOf(2257));
				items.add("CATDISC");
				
				config.set(name + ".Items.BLOCKSDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BLOCKSDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BLOCKSDISC.Alias", "BLOCKSDISC");
				config.set(name + ".Items.BLOCKSDISC.Id", Integer.valueOf(2258));
				items.add("BLOCKSDISC");
				
				config.set(name + ".Items.CHIRPDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CHIRPDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CHIRPDISC.Alias", "CHIRPDISC");
				config.set(name + ".Items.CHIRPDISC.Id", Integer.valueOf(2259));
				items.add("CHIRPDISC");
				
				config.set(name + ".Items.FARDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.FARDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.FARDISC.Alias", "FARDISC");
				config.set(name + ".Items.FARDISC.Id", Integer.valueOf(2260));
				items.add("FARDISC");
				
				config.set(name + ".Items.MALLDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MALLDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MALLDISC.Alias", "MALLDISC");
				config.set(name + ".Items.MALLDISC.Id", Integer.valueOf(2261));
				items.add("MALLDISC");
				
				config.set(name + ".Items.MELLOHIDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.MELLOHIDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.MELLOHIDISC.Alias", "MELLOHIDISC");
				config.set(name + ".Items.MELLOHIDISC.Id", Integer.valueOf(2262));
				items.add("MELLOHIDISC");
				
				config.set(name + ".Items.STALDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STALDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STALDISC.Alias", "STALDISC");
				config.set(name + ".Items.STALDISC.Id", Integer.valueOf(2263));
				items.add("STALDISC");
				
				config.set(name + ".Items.STRADDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STRADDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STRADDISC.Alias", "STRADDISC");
				config.set(name + ".Items.STRADDISC.Id", Integer.valueOf(2264));
				items.add("STRADDISC");
				
				config.set(name + ".Items.WARDDISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WARDDISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WARDDISC.Alias", "WARDDISC");
				config.set(name + ".Items.WARDDISC.Id", Integer.valueOf(2265));
				items.add("WARDDISC");
				
				config.set(name + ".Items.11DISC.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.11DISC.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.11DISC.Alias", "11DISC");
				config.set(name + ".Items.11DISC.Id", Integer.valueOf(2266));
				items.add("11DISC");
				
				config.set(name + ".Items.REDMUSHROOM.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.REDMUSHROOM.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.REDMUSHROOM.Alias", "REDMUSHROOM");
				config.set(name + ".Items.REDMUSHROOM.Id", Integer.valueOf(40));
				items.add("REDMUSHROOM");
				
				config.set(name + ".Items.ROSE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ROSE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ROSE.Alias", "ROSE");
				config.set(name + ".Items.ROSE.Id", Integer.valueOf(38));
				items.add("ROSE");
				
				config.set(name + ".Items.REDSTONE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.REDSTONE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.REDSTONE.Alias", "REDSTONE");
				config.set(name + ".Items.REDSTONE.Id", Integer.valueOf(331));
				items.add("REDSTONE");
				
				config.set(name + ".Items.ROTTENFLESH.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ROTTENFLESH.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ROTTENFLESH.Alias", "ROTTENFLESH");
				config.set(name + ".Items.ROTTENFLESH.Id", Integer.valueOf(367));
				items.add("ROTTENFLESH");
				
				config.set(name + ".Items.SADDLE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SADDLE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SADDLE.Alias", "SADDLE");
				config.set(name + ".Items.SADDLE.Id", Integer.valueOf(329));
				items.add("SADDLE");
				
				config.set(name + ".Items.SAND.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SAND.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SAND.Alias", "SAND");
				config.set(name + ".Items.SAND.Id", Integer.valueOf(12));
				items.add("SAND");

				config.set(name + ".Items.SANDSTONE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SANDSTONE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SANDSTONE.Alias", "SANDSTONE");
				config.set(name + ".Items.SANDSTONE.Id", Integer.valueOf(24));
				items.add("SANDSTONE");
				
				config.set(name + ".Items.SAPLING.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SAPLING.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SAPLING.Alias", "SAPLING");
				config.set(name + ".Items.SAPLING.Id", Integer.valueOf(6));
				items.add("SAPLING");
				
				config.set(name + ".Items.OAKSAPLING.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKSAPLING.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKSAPLING.Alias", "OAKSAPLING");
				config.set(name + ".Items.OAKSAPLING.Id", Integer.valueOf(6));
				items.add("OAKSAPLING");
				
				config.set(name + ".Items.SPRUCESAPLING.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCESAPLING.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCESAPLING.Alias", "SPRUCESAPLING");
				config.set(name + ".Items.SPRUCESAPLING.Id", Integer.valueOf(6));
				config.set(name + ".Items.SPRUCESAPLING.Durability", Integer.valueOf(1));
				items.add("SPRUCESAPLING");
				
				config.set(name + ".Items.REDWOODSAPLING.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.REDWOODSAPLING.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.REDWOODSAPLING.Alias", "REDWOODSAPLING");
				config.set(name + ".Items.REDWOODSAPLING.Id", Integer.valueOf(6));
				config.set(name + ".Items.REDWOODSAPLING.Durability", Integer.valueOf(1));
				items.add("REDWOODSAPLING");
				
				config.set(name + ".Items.BIRCHSAPLING.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHSAPLING.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHSAPLING.Alias", "BIRCHSAPLING");
				config.set(name + ".Items.BIRCHSAPLING.Id", Integer.valueOf(6));
				config.set(name + ".Items.BIRCHSAPLING.Durability", Integer.valueOf(2));
				items.add("BIRCHSAPLING");
				
				config.set(name + ".Items.JUNGLESAPLING.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLESAPLING.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLESAPLING.Alias", "JUNGLESAPLING");
				config.set(name + ".Items.JUNGLESAPLING.Id", Integer.valueOf(6));
				config.set(name + ".Items.JUNGLESAPLING.Durability", Integer.valueOf(3));
				items.add("JUNGLESAPLING");
				
				config.set(name + ".Items.SEEDS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SEEDS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SEEDS.Alias", "SEEDS");
				config.set(name + ".Items.SEEDS.Id", Integer.valueOf(295));
				items.add("SEEDS");
				
				config.set(name + ".Items.WHEATSEEDS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WHEATSEEDS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WHEATSEEDS.Alias", "WHEATSEEDS");
				config.set(name + ".Items.WHEATSEEDS.Id", Integer.valueOf(295));
				items.add("WHEATSEEDS");
				
				config.set(name + ".Items.SHEARS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SHEARS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SHEARS.Alias", "SHEARS");
				config.set(name + ".Items.SHEARS.Id", Integer.valueOf(359));
				items.add("SHEARS");
				
				config.set(name + ".Items.SIGN.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SIGN.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SIGN.Alias", "SIGN");
				config.set(name + ".Items.SIGN.Id", Integer.valueOf(323));
				items.add("SIGN");
				
				config.set(name + ".Items.SKELETONHEAD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SKELETONHEAD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SKELETONHEAD.Alias", "SKELETONHEAD");
				config.set(name + ".Items.SKELETONHEAD.Id", Integer.valueOf(397));
				items.add("SKELETONHEAD");
				
				config.set(name + ".Items.WITHERHEAD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WITHERHEAD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WITHERHEAD.Alias", "WITHERHEAD");
				config.set(name + ".Items.WITHERHEAD.Id", Integer.valueOf(397));
				config.set(name + ".Items.WITHERHEAD.Durability", Integer.valueOf(1));
				items.add("WITHERHEAD");
				
				config.set(name + ".Items.ZOMBIEHEAD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.ZOMBIEHEAD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.ZOMBIEHEAD.Alias", "ZOMBIEHEAD");
				config.set(name + ".Items.ZOMBIEHEAD.Id", Integer.valueOf(397));
				config.set(name + ".Items.ZOMBIEHEAD.Durability", Integer.valueOf(2));
				items.add("ZOMBIEHEAD");
				
				config.set(name + ".Items.HUMANHEAD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.HUMANHEAD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.HUMANHEAD.Alias", "HUMANHEAD");
				config.set(name + ".Items.HUMANHEAD.Id", Integer.valueOf(397));
				config.set(name + ".Items.HUMANHEAD.Durability", Integer.valueOf(3));
				items.add("HUMANHEAD");
				
				config.set(name + ".Items.CREEPERHEAD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CREEPERHEAD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CREEPERHEAD.Alias", "CREEPERHEAD");
				config.set(name + ".Items.CREEPERHEAD.Id", Integer.valueOf(397));
				config.set(name + ".Items.CREEPERHEAD.Durability", Integer.valueOf(4));
				items.add("CREEPERHEAD");
				
				config.set(name + ".Items.STONEBRICK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEBRICK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEBRICK.Alias", "STONEBRICK");
				config.set(name + ".Items.STONEBRICK.Id", Integer.valueOf(98));
				items.add("STONEBRICK");
				
				config.set(name + ".Items.STONEBRICKSTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEBRICKSTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEBRICKSTAIRS.Alias", "STONEBRICKSTAIRS");
				config.set(name + ".Items.STONEBRICKSTAIRS.Id", Integer.valueOf(109));
				items.add("STONEBRICKSTAIRS");
				
				config.set(name + ".Items.SOULSAND.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SOULSAND.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SOULSAND.Alias", "SOULSAND");
				config.set(name + ".Items.SOULSAND.Id", Integer.valueOf(88));
				items.add("SOULSAND");
				
				config.set(name + ".Items.SLIMEBALL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SLIMEBALL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SLIMEBALL.Alias", "SLIMEBALL");
				config.set(name + ".Items.SLIMEBALL.Id", Integer.valueOf(341));
				items.add("SLIMEBALL");
				
				config.set(name + ".Items.GLISTERINGMELON.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GLISTERINGMELON.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GLISTERINGMELON.Alias", "GLISTERINGMELON");
				config.set(name + ".Items.GLISTERINGMELON.Id", Integer.valueOf(382));
				items.add("GLISTERINGMELON");
				
				config.set(name + ".Items.SPIDEREYE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SPIDEREYE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SPIDEREYE.Alias", "SPIDEREYE");
				config.set(name + ".Items.SPIDEREYE.Id", Integer.valueOf(375));
				items.add("SPIDEREYE");
				
				config.set(name + ".Items.SPRUCEWOODSTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCEWOODSTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCEWOODSTAIRS.Alias", "SPRUCEWOODSTAIRS");
				config.set(name + ".Items.SPRUCEWOODSTAIRS.Id", Integer.valueOf(134));
				items.add("SPRUCEWOODSTAIRS");
				
				config.set(name + ".Items.STICK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STICK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STICK.Alias", "STICK");
				config.set(name + ".Items.STICK.Id", Integer.valueOf(280));
				items.add("STICK");
				
				config.set(name + ".Items.STONE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONE.Alias", "STONE");
				config.set(name + ".Items.STONE.Id", Integer.valueOf(1));
				items.add("STONE");
				
				config.set(name + ".Items.STONEAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEAXE.Alias", "STONEAXE");
				config.set(name + ".Items.STONEAXE.Id", Integer.valueOf(275));
				items.add("STONEAXE");
				
				config.set(name + ".Items.STONEBUTTON.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEBUTTON.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEBUTTON.Alias", "STONEBUTTON");
				config.set(name + ".Items.STONEBUTTON.Id", Integer.valueOf(77));
				items.add("STONEBUTTON");
				
				config.set(name + ".Items.STONEHOE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEHOE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEHOE.Alias", "STONEHOE");
				config.set(name + ".Items.STONEHOE.Id", Integer.valueOf(291));
				items.add("STONEHOE");
				
				config.set(name + ".Items.STONEPICKAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEPICKAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEPICKAXE.Alias", "STONEPICKAXE");
				config.set(name + ".Items.STONEPICKAXE.Id", Integer.valueOf(274));
				items.add("STONEPICKAXE");
				
				config.set(name + ".Items.STONEPRESSUREPLATE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEPRESSUREPLATE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONEPRESSUREPLATE.Alias", "STONEPRESSUREPLATE");
				config.set(name + ".Items.STONEPRESSUREPLATE.Id", Integer.valueOf(70));
				items.add("STONEPRESSUREPLATE");
				
				config.set(name + ".Items.STONESHOVEL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONESHOVEL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONESHOVEL.Alias", "STONESHOVEL");
				config.set(name + ".Items.STONESHOVEL.Id", Integer.valueOf(273));
				items.add("STONESHOVEL");
				
				config.set(name + ".Items.STONESWORD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STONESWORD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STONESWORD.Alias", "STONESWORD");
				config.set(name + ".Items.STONESWORD.Id", Integer.valueOf(272));
				items.add("STONESWORD");
				
				config.set(name + ".Items.STORAGEMINECART.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STORAGEMINECART.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STORAGEMINECART.Alias", "STORAGEMINECART");
				config.set(name + ".Items.STORAGEMINECART.Id", Integer.valueOf(342));
				items.add("STORAGEMINECART");
				
				config.set(name + ".Items.STRING.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.STRING.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.STRING.Alias", "STRING");
				config.set(name + ".Items.STRING.Id", Integer.valueOf(287));
				items.add("STRING");
				
				config.set(name + ".Items.SUGAR.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SUGAR.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SUGAR.Alias", "SUGAR");
				config.set(name + ".Items.SUGAR.Id", Integer.valueOf(353));
				items.add("SUGAR");
				
				config.set(name + ".Items.SUGARCANE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SUGARCANE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SUGARCANE.Alias", "SUGARCANE");
				config.set(name + ".Items.SUGARCANE.Id", Integer.valueOf(338));
				items.add("SUGARCANE");
				
				config.set(name + ".Items.SULPHUR.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SULPHUR.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SULPHUR.Alias", "SULPHUR");
				config.set(name + ".Items.SULPHUR.Id", Integer.valueOf(289));
				items.add("SULPHUR");
				
				config.set(name + ".Items.GUNPOWDER.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GUNPOWDER.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GUNPOWDER.Alias", "GUNPOWDER");
				config.set(name + ".Items.GUNPOWDER.Id", Integer.valueOf(289));
				items.add("GUNPOWDER");
				
				config.set(name + ".Items.GLASSPANE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.GLASSPANE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.GLASSPANE.Alias", "GLASSPANE");
				config.set(name + ".Items.GLASSPANE.Id", Integer.valueOf(102));
				items.add("GLASSPANE");
				
				config.set(name + ".Items.TNT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.TNT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.TNT.Alias", "TNT");
				config.set(name + ".Items.TNT.Id", Integer.valueOf(46));
				items.add("TNT");
				
				config.set(name + ".Items.TORCH.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.TORCH.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.TORCH.Alias", "TORCH");
				config.set(name + ".Items.TORCH.Id", Integer.valueOf(50));
				items.add("TORCH");
				
				config.set(name + ".Items.TRAPDOOR.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.TRAPDOOR.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.TRAPDOOR.Alias", "TRAPDOOR");
				config.set(name + ".Items.TRAPDOOR.Id", Integer.valueOf(96));
				items.add("TRAPDOOR");
				
				config.set(name + ".Items.TRIPWIREHOOK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.TRIPWIREHOOK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.TRIPWIREHOOK.Alias", "TRIPWIREHOOK");
				config.set(name + ".Items.TRIPWIREHOOK.Id", Integer.valueOf(131));
				items.add("TRIPWIREHOOK");
				
				config.set(name + ".Items.CLOCK.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.CLOCK.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.CLOCK.Alias", "CLOCK");
				config.set(name + ".Items.CLOCK.Id", Integer.valueOf(347));
				items.add("CLOCK");
				
				config.set(name + ".Items.WATERBUCKET.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WATERBUCKET.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WATERBUCKET.Alias", "WATERBUCKET");
				config.set(name + ".Items.WATERBUCKET.Id", Integer.valueOf(326));
				items.add("WATERBUCKET");
				
				config.set(name + ".Items.WHEAT.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WHEAT.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WHEAT.Alias", "WHEAT");
				config.set(name + ".Items.WHEAT.Id", Integer.valueOf(296));
				items.add("WHEAT");
				
				config.set(name + ".Items.PLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.PLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.PLANKS.Alias", "PLANKS");
				config.set(name + ".Items.PLANKS.Id", Integer.valueOf(5));
				items.add("PLANKS");
				
				config.set(name + ".Items.OAKPLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKPLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKPLANKS.Alias", "OAKPLANKS");
				config.set(name + ".Items.OAKPLANKS.Id", Integer.valueOf(5));
				items.add("OAKPLANKS");
				
				config.set(name + ".Items.REDWOODPLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.REDWOODPLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.REDWOODPLANKS.Alias", "REDWOODPLANKS");
				config.set(name + ".Items.REDWOODPLANKS.Id", Integer.valueOf(5));
				config.set(name + ".Items.REDWOODPLANKS.Durability", Integer.valueOf(1));
				items.add("REDWOODPLANKS");
				
				config.set(name + ".Items.SPRUCEPLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCEPLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCEPLANKS.Alias", "SPRUCEPLANKS");
				config.set(name + ".Items.SPRUCEPLANKS.Id", Integer.valueOf(5));
				config.set(name + ".Items.SPRUCEPLANKS.Durability", Integer.valueOf(1));
				items.add("SPRUCEPLANKS");
				
				config.set(name + ".Items.SPRUCEWOODPLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCEWOODPLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.SPRUCEWOODPLANKS.Alias", "SPRUCEWOODPLANKS");
				config.set(name + ".Items.SPRUCEWOODPLANKS.Id", Integer.valueOf(5));
				config.set(name + ".Items.SPRUCEWOODPLANKS.Durability", Integer.valueOf(1));
				items.add("SPRUCEWOODPLANKS");
				
				config.set(name + ".Items.BIRCHPLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHPLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHPLANKS.Alias", "BIRCHPLANKS");
				config.set(name + ".Items.BIRCHPLANKS.Id", Integer.valueOf(5));
				config.set(name + ".Items.BIRCHPLANKS.Durability", Integer.valueOf(2));
				items.add("BIRCHPLANKS");
				
				config.set(name + ".Items.BIRCHWOODPLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHWOODPLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.BIRCHWOODPLANKS.Alias", "BIRCHWOODPLANKS");
				config.set(name + ".Items.BIRCHWOODPLANKS.Id", Integer.valueOf(5));
				config.set(name + ".Items.BIRCHWOODPLANKS.Durability", Integer.valueOf(2));
				items.add("BIRCHWOODPLANKS");
				
				config.set(name + ".Items.JUNGLEPLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLEPLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLEPLANKS.Alias", "JUNGLEPLANKS");
				config.set(name + ".Items.JUNGLEPLANKS.Id", Integer.valueOf(5));
				config.set(name + ".Items.JUNGLEPLANKS.Durability", Integer.valueOf(3));
				items.add("JUNGLEPLANKS");
				
				config.set(name + ".Items.JUNGLEWOODPLANKS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLEWOODPLANKS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.JUNGLEWOODPLANKS.Alias", "JUNGLEWOODPLANKS");
				config.set(name + ".Items.JUNGLEWOODPLANKS.Id", Integer.valueOf(5));
				config.set(name + ".Items.JUNGLEWOODPLANKS.Durability", Integer.valueOf(3));
				items.add("JUNGLEWOODPLANKS");
				
				config.set(name + ".Items.WOODENAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENAXE.Alias", "WOODENAXE");
				config.set(name + ".Items.WOODENAXE.Id", Integer.valueOf(271));
				items.add("WOODENAXE");
				
				config.set(name + ".Items.WOODENBUTTON.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENBUTTON.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENBUTTON.Alias", "WOODENBUTTON");
				config.set(name + ".Items.WOODENBUTTON.Id", Integer.valueOf(143));
				items.add("WOODENBUTTON");
				
				config.set(name + ".Items.WOODENDOOR.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENDOOR.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENDOOR.Alias", "WOODENDOOR");
				config.set(name + ".Items.WOODENDOOR.Id", Integer.valueOf(324));
				items.add("WOODENDOOR");
				
				config.set(name + ".Items.WOODENHOE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENHOE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENHOE.Alias", "WOODENHOE");
				config.set(name + ".Items.WOODENHOE.Id", Integer.valueOf(5));
				config.set(name + ".Items.WOODENHOE.Durability", Integer.valueOf(290));
				items.add("WOODENHOE");
				
				config.set(name + ".Items.WOODENPICKAXE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENPICKAXE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENPICKAXE.Alias", "WOODENPICKAXE");
				config.set(name + ".Items.WOODENPICKAXE.Id", Integer.valueOf(270));
				items.add("WOODENPICKAXE");
				
				config.set(name + ".Items.WOODENPRESSUREPLATE.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENPRESSUREPLATE.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENPRESSUREPLATE.Alias", "WOODENPRESSUREPLATE");
				config.set(name + ".Items.WOODENPRESSUREPLATE.Id", Integer.valueOf(72));
				items.add("WOODENPRESSUREPLATE");
				
				config.set(name + ".Items.WOODENSHOVEL.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENSHOVEL.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENSHOVEL.Alias", "WOODENSHOVEL");
				config.set(name + ".Items.WOODENSHOVEL.Id", Integer.valueOf(269));
				items.add("WOODENSHOVEL");
				
				config.set(name + ".Items.WOODENSTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENSTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENSTAIRS.Alias", "WOODENSTAIRS");
				config.set(name + ".Items.WOODENSTAIRS.Id", Integer.valueOf(53));
				items.add("WOODENSTAIRS");
				
				config.set(name + ".Items.OAKWOODSTAIRS.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKWOODSTAIRS.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.OAKWOODSTAIRS.Alias", "OAKWOODSTAIRS");
				config.set(name + ".Items.OAKWOODSTAIRS.Id", Integer.valueOf(53));
				items.add("OAKWOODSTAIRS");
				
				config.set(name + ".Items.WOODENSWORD.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENSWORD.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WOODENSWORD.Alias", "268");
				config.set(name + ".Items.WOODENSWORD.Id", Integer.valueOf(271));
				items.add("WOODENAXE");
				
				config.set(name + ".Items.WORKBENCH.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.WORKBENCH.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.WORKBENCH.Alias", "WORKBENCH");
				config.set(name + ".Items.WORKBENCH.Id", Integer.valueOf(58));
				items.add("WORKBENCH");
				
				config.set(name + ".Items.DANDELION.BuyCost", Integer.valueOf(0));
				config.set(name + ".Items.DANDELION.SellCost", Integer.valueOf(0));
				config.set(name + ".Items.DANDELION.Alias", "DANDELION");
				config.set(name + ".Items.DANDELION.Id", Integer.valueOf(37));
				items.add("DANDELION");
				
				config.set(name + ".Items.List", items);
				player.sendMessage(ChatColor.GOLD + "Market created!");
				this.saveConfig();
				return true;
			}
			else if(args[0].equalsIgnoreCase("marketlist"))
			{
				player.sendMessage(ChatColor.GREEN + "=== List of Markets ===");
				for(Market market : Markets.getMarkets())
				{
					player.sendMessage(ChatColor.AQUA + market.getName());
				}
			}
			else if(args[0].equalsIgnoreCase("reload"))
			{
				Markets.clearMarkets();
				loadMarkets();
				player.sendMessage(ChatColor.GOLD + "SmudgeMarket Config Reloaded!");
			}
			else if(args[0].equalsIgnoreCase("marketitems"))
			{
				if(Markets.isPlayerInMarket(player)==false)
				{
					player.sendMessage(ChatColor.RED + "You have to be in a market to do that!");
				}
				else
				{
				Market market = Markets.getPlayersCurrentMarket(player);
				player.sendMessage(ChatColor.GREEN + "=== List of MarketItems for: " + ChatColor.AQUA + market.getName() + ChatColor.GREEN + " ===");
				for(MarketItem mi : market.getMarketItems())
				{
					player.sendMessage(ChatColor.AQUA + mi.getAlias());
				}
				}
			}
			else if(args[0].equalsIgnoreCase("durability"))
			{
				ItemStack is = player.getItemInHand();
				int durability = is.getDurability();
				int maxdurability = is.getType().getMaxDurability();
				
				player.sendMessage("Current Durability: " + durability);
				player.sendMessage("MaxDurability: " + maxdurability);
			}
			else if(args[0].equalsIgnoreCase("tp") && args.length==2)
			{
				if(Markets.exists(args[1]))
				{
					Market market = Markets.getMarket(args[1]);
					Location tploc = market.getRandomLocation();
					player.teleport(tploc);
				}
			}
			else if(args[0].equalsIgnoreCase("iteminfo") && args.length>1)
			{
				String itemname = args[1].toUpperCase();
				if(Markets.isPlayerInMarket(player))
				{
					Market market = Markets.getPlayersCurrentMarket(player);
					if(market.containsItem(itemname))
					{
						MarketItem mi = market.getMarketItemByName(itemname);
						player.sendMessage(ChatColor.GREEN + "MarketItemName: " + ChatColor.AQUA + mi.getAlias());
						player.sendMessage(ChatColor.GREEN + "BuyCost: " + ChatColor.AQUA + mi.getBuyCost());
						player.sendMessage(ChatColor.GREEN + "SellCost: " + ChatColor.AQUA + mi.getSellCost());
						player.sendMessage(ChatColor.GREEN + "Durability: " + ChatColor.AQUA + mi.getDurability());
						player.sendMessage(ChatColor.GREEN + "MarketItemId: " + ChatColor.AQUA + mi.getItemId());
					}
					else
					{
						player.sendMessage(ChatColor.RED + "There is no item by that name!");
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + "You have to be in a market to do that!");
				}
			}
		}
		}
		else if(commandlabel.equalsIgnoreCase("market"))
		{
			if(args.length==0)
			{
				player.sendMessage(ChatColor.GOLD + "===========" + ChatColor.YELLOW + " Market Help " + ChatColor.GOLD + "===========");
				player.sendMessage(ChatColor.GOLD + "/buy <item> <amount>" + ChatColor.YELLOW + " - Buy a specific amount of items from a market");
				player.sendMessage(ChatColor.GOLD + "/sell <item> <amount>" + ChatColor.YELLOW + " - Sell a specific amount of items to a market");
				player.sendMessage(ChatColor.GOLD + "/sellhand <amount>" + ChatColor.YELLOW + " - Sell as specific amount of the item in your hand to a market");
				player.sendMessage(ChatColor.GOLD + "/price <item>" + ChatColor.YELLOW + " - Gives you the buy and sell price of that specific item");
				player.sendMessage(ChatColor.GOLD + "/market help" + ChatColor.YELLOW + " - Gives you this help menu");
				player.sendMessage(ChatColor.GOLD + "=================================");
			}
			else if(args.length==1 && args[0].equalsIgnoreCase("help"))
			{
				player.sendMessage(ChatColor.GOLD + "===========" + ChatColor.YELLOW + " Market Help " + ChatColor.GOLD + "===========");
				player.sendMessage(ChatColor.GOLD + "/buy <item> <amount>" + ChatColor.YELLOW + " - Buy a specific amount of items from a market");
				player.sendMessage(ChatColor.GOLD + "/sell <item> <amount>" + ChatColor.YELLOW + " - Sell a specific amount of items to a market");
				player.sendMessage(ChatColor.GOLD + "/sellhand <amount>" + ChatColor.YELLOW + " - Sell as specific amount of the item in your hand to a market");
				player.sendMessage(ChatColor.GOLD + "/price <item>" + ChatColor.YELLOW + " - Gives you the buy and sell price of that specific item");
				player.sendMessage(ChatColor.GOLD + "/market help" + ChatColor.YELLOW + " - Gives you this help menu");
				player.sendMessage(ChatColor.GOLD + "=================================");
			}
		}
		else if(commandlabel.equalsIgnoreCase("sell") && args.length>0)
		{
			MarketPlayer mp = marketplayers.getMarketPlayer(player);
			if(mp.getMarket()!=null)
			{
				Market market = mp.getMarket();
				if(player.hasPermission("SmudgeMarket.Market." + market.getName().toLowerCase()))
				{
				if(isNumber(args[0]))
				{
					if(args.length>1)
					{
						int itemid = Integer.parseInt(args[0]);
						if(market.containsItem(itemid))
						{
							MarketItem mi = market.getMarketItemById(itemid);
							
							if(mi.getSellCost()==-1)
							{
								player.sendMessage(ChatColor.RED + "This market does not buy the item by the id: " + ChatColor.AQUA + args[0]);
							}
							else
							{
								
							if(isNumber(args[1]))
							{
								int amount = Integer.parseInt(args[1]);
								ItemStack is = new ItemStack(Material.getMaterial(mi.getItemId()), amount);
								if(canBreak(is.getTypeId()))
								{
									PlayerInventory pi = player.getInventory();
									int totalcost=0;
									int number=0;
									if(containsAtLeast(pi, Material.getMaterial(mi.getItemId()), amount))
									{
									List<ItemStack> remove = new ArrayList<ItemStack>();
									for(ItemStack items : pi.getContents())
									{
										if(items!=null)
										{
										if(number==amount)
										{
											continue;
										}
										if(items.getTypeId()==mi.getItemId())
										{
											totalcost += sellCostOfTool(mi.getSellCost(), items);
											remove.add(items);
											number++;
										}
										}
									}
									ItemStack[] rem = new ItemStack[remove.size()];
									rem = remove.toArray(rem);
									HashMap<Integer, ItemStack> left = pi.removeItem(rem);
									if(left.isEmpty())
									{
									economy.depositPlayer(player.getName(), totalcost);
									player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + totalcost + " Smudges.");
									}
									else
									{
									player.sendMessage(ChatColor.RED + "Error selling items");
									pi.addItem(rem);
									}
									}
									else
									{
										player.sendMessage(ChatColor.RED + "You don't have " + ChatColor.AQUA + amount + " " + mi.getAlias());	
									}
								}
								else
								{
								int cost = amount*mi.getSellCost();
								PlayerInventory pi = player.getInventory();
								if(containsAtLeast(pi, Material.getMaterial(mi.getItemId()), amount))
								{
									pi.removeItem(is);
									economy.depositPlayer(player.getName(), cost);
									player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
								}
								else
								{
									player.sendMessage(ChatColor.RED + "You don't have " + ChatColor.AQUA + amount + " " + mi.getAlias());
								}
								}
							}
							}
					}
				}
			}
			else
			{
				if(args.length>1)
				{
				String itemname = args[0].toUpperCase();
				if(market.containsItem(itemname))
				{
					MarketItem mi = market.getMarketItemByName(itemname);
						
					if(mi.getSellCost()==-1)
					{
						player.sendMessage(ChatColor.RED + "This market does not buy the item by the name: " + ChatColor.AQUA + args[0]);
					}
					else
					{
					if(isNumber(args[1]))
					{
						int amount = Integer.parseInt(args[1]);
						if(hasHex(mi))
						{
						short durability = mi.getDurability();
						ItemStack is = new ItemStack(Material.getMaterial(mi.getItemId()), amount, durability);
						if(canBreak(is.getTypeId()))
						{
						PlayerInventory pi = player.getInventory();
						int totalcost=0;
						int number=0;
						if(containsAtLeast(pi, Material.getMaterial(mi.getItemId()), amount))
						{
						List<ItemStack> remove = new ArrayList<ItemStack>();
						for(ItemStack items : pi.getContents())
						{
							if(items!=null)
							{
							if(number==amount)
							{
								continue;
							}
							if(items.getTypeId()==mi.getItemId())
							{
								totalcost += sellCostOfTool(mi.getSellCost(), items);
								remove.add(items);
								number++;
							}
							}
						}
						ItemStack[] rem = new ItemStack[remove.size()];
						rem = remove.toArray(rem);
						HashMap<Integer, ItemStack> left = pi.removeItem(rem);
						if(left.isEmpty())
						{
						economy.depositPlayer(player.getName(), totalcost);
						player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + itemname + ChatColor.GOLD + " for " + ChatColor.YELLOW + totalcost + " Smudges.");
						}
						else
						{
							player.sendMessage(ChatColor.RED + "Error selling items");
							pi.addItem(rem);
						}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You don't have " + ChatColor.AQUA + amount + " " + mi.getAlias());
						}
						}
						else
						{
						int cost = amount*mi.getSellCost();
						PlayerInventory pi = player.getInventory();
						if(containsAtLeast(pi, Material.getMaterial(mi.getItemId()), amount))
						{
							pi.remove(is);
							economy.depositPlayer(player.getName(), cost);
							player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + itemname + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You don't have " + ChatColor.AQUA + amount + " " + mi.getAlias());
						}
						}
						}
						else
						{
						ItemStack is = new ItemStack(Material.getMaterial(mi.getItemId()), amount);
						if(canBreak(is.getTypeId()))
						{
						PlayerInventory pi = player.getInventory();
						int totalcost=0;
						int number=0;
						if(containsAtLeast(pi, Material.getMaterial(mi.getItemId()), amount))
						{
						List<ItemStack> remove = new ArrayList<ItemStack>();
						for(ItemStack items : pi.getContents())
						{
							if(number==amount)
							{
								continue;
							}
							if(items!=null)
							{
							if(items.getTypeId()==mi.getItemId())
							{
								totalcost += sellCostOfTool(mi.getSellCost(), items);
								remove.add(items);
								number++;
							}
							}
						}
						ItemStack[] rem = new ItemStack[remove.size()];
						rem = remove.toArray(rem);
						HashMap<Integer, ItemStack> left = pi.removeItem(rem);
						if(left.isEmpty())
						{
						economy.depositPlayer(player.getName(), totalcost);
						player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + itemname + ChatColor.GOLD + " for " + ChatColor.YELLOW + totalcost + " Smudges.");
						}
						else
						{
							player.sendMessage(ChatColor.RED + "Error selling items");
							pi.addItem(rem);
						}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You don't have " + ChatColor.AQUA + amount + " " + mi.getAlias());
						}
						}
						else
						{
						int cost = amount*mi.getSellCost();
						PlayerInventory pi = player.getInventory();
						if(pi.containsAtLeast(is, amount))
						{
							pi.removeItem(is);
							economy.depositPlayer(player.getName(), cost);
							player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + itemname + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You don't have " + ChatColor.AQUA + amount + " " + mi.getAlias());
						}
						}
					}
					}
				}
				}
				else
				{
					player.sendMessage(ChatColor.RED + "This market does not buy the item by the name: " + ChatColor.AQUA + args[0]);
				}
				}
				else if(args.length>2)
				{
					player.sendMessage(ChatColor.RED + "You have entered to many arguments!");
					player.sendMessage(ChatColor.RED + "Command usage: " + ChatColor.AQUA + "/buy <item> <amount>");
				}
				else if(args.length==1)
				{
					player.sendMessage(ChatColor.RED + "You have to enter amount!");
					player.sendMessage(ChatColor.RED + "Command usage: " + ChatColor.AQUA + "/buy <item> <amount>");
				}
			}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You don't have permission to buy or sell at other races' markets!" + "\n Please use your own race's market to buy and sell items.");
			}
		}
		else
		{
		player.sendMessage(ChatColor.RED + "You have to be in a market to trade items!");
		}
		}
		else if(commandlabel.equalsIgnoreCase("buy") && args.length>0)
		{
			MarketPlayer mp = marketplayers.getMarketPlayer(player);
			if(mp.getMarket()!=null)
			{
				Market market = mp.getMarket();
				if(player.hasPermission("SmudgeMarket.Market." + market.getName().toLowerCase()))
				{
				if(isNumber(args[0]))
				{
					if(args.length>1)
					{
					int itemid = Integer.parseInt(args[0]);
					if(market.containsItem(itemid))
					{
						MarketItem mi = market.getMarketItemById(itemid);
						if(isNumber(args[1]))
						{
							int amount = Integer.parseInt(args[1]);
							ItemStack is = new ItemStack(Material.getMaterial(mi.getItemId()), amount);
							int cost = mi.getBuyCost()*amount;
							
							if(economy.getBalance(player.getName())>cost)
							{
								economy.withdrawPlayer(player.getName(), cost);
								player.sendMessage(ChatColor.GOLD + "You have bought " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
								giveItemStack(is, player);
							}
							else
							{
								player.sendMessage(ChatColor.RED + "You don't have  " + ChatColor.AQUA + cost + " Smudges " + ChatColor.RED + "to buy " + ChatColor.AQUA + amount + " " + mi.getAlias());
							}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You can't enter a string: " + ChatColor.AQUA + "\"" + args[1] + "\"" + ChatColor.RED  + " for amount");
							player.sendMessage(ChatColor.RED + "Command usage: " + ChatColor.AQUA + "/buy <item> <amount>");
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "This market does not sell the item by the id: " + ChatColor.AQUA + args[0]);
					}
					}
					else if(args.length>2)
					{
						player.sendMessage(ChatColor.RED + "You have entered to many arguments!");
						player.sendMessage(ChatColor.RED + "Command usage: " + ChatColor.AQUA + "/buy <item> <amount>");
					}
					else if(args.length==1)
					{
						player.sendMessage(ChatColor.RED + "You have to enter amount!");
						player.sendMessage(ChatColor.RED + "Command usage: " + ChatColor.AQUA + "/buy <item> <amount>");
					}
				}
				else
				{
					if(args.length>1)
					{
					String itemname = args[0].toUpperCase();
					if(market.containsItem(itemname))
					{
						MarketItem mi = market.getMarketItemByName(itemname);
						if(isNumber(args[1]))
						{
							if(hasHex(mi))
							{
							int amount = Integer.parseInt(args[1]);
							int cost = mi.getBuyCost()*amount;
							short dur = mi.getDurability();
							ItemStack is = new ItemStack(Material.getMaterial(mi.getItemId()), amount, dur);
							if(hasSpace(is, player))
							{
							if(economy.getBalance(player.getName())>cost)
							{
								economy.withdrawPlayer(player.getName(), cost);
								player.sendMessage(ChatColor.GOLD + "You have bought " + ChatColor.YELLOW + amount + " " + itemname + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
								giveItemStack(is, player);
							}
							else
							{
								player.sendMessage(ChatColor.RED + "You don't have  " + ChatColor.AQUA + cost + " Smudges " + ChatColor.RED + "to buy " + ChatColor.AQUA + amount + " " + mi.getAlias());
							}
							}
							else
							{
								player.sendMessage(ChatColor.RED + "You don't have enough space to buy that!");
							}
							}
							else
							{
							int amount = Integer.parseInt(args[1]);
							int cost = mi.getBuyCost()*amount;
							ItemStack is = new ItemStack(Material.getMaterial(mi.getItemId()), amount);
							if(hasSpace(is, player))
							{
							if(economy.getBalance(player.getName())>cost)
							{
								economy.withdrawPlayer(player.getName(), cost);
								player.sendMessage(ChatColor.GOLD + "You have bought " + ChatColor.YELLOW + amount + " " + itemname + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
								giveItemStack(is, player);
							}
							else
							{
								player.sendMessage(ChatColor.RED + "You don't have  " + ChatColor.AQUA + cost + " Smudges " + ChatColor.RED + "to buy " + ChatColor.AQUA + amount + " " + mi.getAlias());
							}
							}
							else
							{
								player.sendMessage(ChatColor.RED + "You don't have enough space to buy that!");
							}
							}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You can't enter a string: " + ChatColor.AQUA + "\"" + args[1] + "\"" + ChatColor.RED  + " for amount");
							player.sendMessage(ChatColor.RED + "Command usage: " + ChatColor.AQUA + "/buy <item> <amount>");
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "This market does not sell the item by the name: " + ChatColor.AQUA + args[0]);
					}
					}
					else if(args.length>2)
					{
						player.sendMessage(ChatColor.RED + "You have entered to many arguments!");
						player.sendMessage(ChatColor.RED + "Command usage: " + ChatColor.AQUA + "/buy <item> <amount>");
					}
					else if(args.length==1)
					{
						player.sendMessage(ChatColor.RED + "You have to enter amount!");
						player.sendMessage(ChatColor.RED + "Command usage: " + ChatColor.AQUA + "/buy <item> <amount>");
					}
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You don't have permission to buy or sell at other races' markets!" + "\n Please use your own race's market to buy and sell items.");	
			}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You have to be in a market to trade items!");
			}
		}
		else if(commandlabel.equalsIgnoreCase("price") && args.length>0)
		{
			if(isNumber(args[0]))
			{
				int itemid = Integer.parseInt(args[0]);
				MarketPlayer mp = marketplayers.getMarketPlayer(player);
				if(mp!=null)
				{
					Market market = mp.getMarket();
					if(market!=null)
					{
						if(market.containsItem(itemid))
						{
							MarketItem mi = market.getMarketItemById(itemid);
							player.sendMessage(ChatColor.GOLD + "=========== " + ChatColor.YELLOW + mi.getAlias() + " Prices " + ChatColor.GOLD + "===========");
							player.sendMessage(ChatColor.GOLD + "Buy Price: " + ChatColor.YELLOW + mi.getBuyCost());
							player.sendMessage(ChatColor.GOLD + "Sell Price: " + ChatColor.YELLOW + mi.getSellCost());
							String lastline = "=============================";
							for(int i=0; i<mi.getAlias().length();++i)
							{
								lastline+="=";
							}
							player.sendMessage(ChatColor.GOLD + lastline);
						}
						else
						{
							player.sendMessage(ChatColor.RED + "This market does not contain an item by the id " + ChatColor.AQUA + args[0]);
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "You have to be in a market to check prices!\n" + "Because the prices are local and differ from market to market!");
					}
				}
			}
			else
			{
				String itemname = args[0].toUpperCase();
				MarketPlayer mp = marketplayers.getMarketPlayer(player);
				if(mp!=null)
				{
					Market market = mp.getMarket();
					if(market!=null)
					{
						if(market.containsItem(itemname))
						{
							MarketItem mi = market.getMarketItemByName(itemname);
							player.sendMessage(ChatColor.GOLD + "=========== " + ChatColor.YELLOW + mi.getAlias() + " Prices " + ChatColor.GOLD + "===========");
							player.sendMessage(ChatColor.GOLD + "Buy Price: " + ChatColor.YELLOW + mi.getBuyCost());
							player.sendMessage(ChatColor.GOLD + "Sell Price: " + ChatColor.YELLOW + mi.getSellCost());
							String lastline = "=============================";
							for(int i=0; i<mi.getAlias().length();++i)
							{
								lastline+="=";
							}
							player.sendMessage(ChatColor.GOLD + lastline);
						}
						else
						{
							player.sendMessage(ChatColor.RED + "This market does not contain an item by the name " + ChatColor.AQUA + args[0]);
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "You have to be in a market to check prices!\n" + "Because the prices are local and differ from market to market!");
					}
				}
			}
		}
		else if(commandlabel.equalsIgnoreCase("sellhand"))
		{
			MarketPlayer mp = marketplayers.getMarketPlayer(player);
			if(mp!=null)
			{
			Market market = mp.getMarket();
			if(market!=null)
			{
			if(player.hasPermission("SmudgeMarket.Market." + market.getName().toLowerCase()))
			{
					PlayerInventory pi = player.getInventory();
					if(pi.getItemInHand()!=null)
					{
					if(pi.getItemInHand().getType()!=null && pi.getItemInHand().getType()!=Material.AIR && pi.getItemInHand().getAmount()!=0)
					{
					ItemStack is = pi.getItemInHand();
					int amount = is.getAmount();
					if(market.containsItem(is.getTypeId(), is.getDurability()))
					{
						MarketItem mi = market.getMarketItemByIdAndDurability(is.getTypeId(), is.getDurability());
						if(canBreak(is.getTypeId()))
						{
							int cost=sellCostOfTool(mi.getSellCost(), is);
							pi.setItemInHand(new ItemStack(Material.AIR));
							economy.depositPlayer(player.getName(), cost);
							player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
						}
						else
						{
							int cost = mi.getSellCost() * amount;
							pi.setItemInHand(new ItemStack(Material.AIR));
							economy.depositPlayer(player.getName(), cost);
							player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
						}
					}
					else if(market.containsItem(is.getTypeId()))
					{
						MarketItem mi = market.getMarketItemById(is.getTypeId());
						if(canBreak(is.getTypeId()))
						{
							int cost=sellCostOfTool(mi.getSellCost(), is);
							economy.depositPlayer(player.getName(), cost);
							pi.setItemInHand(new ItemStack(Material.AIR));
							player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
						}
						else
						{
							int cost = mi.getSellCost() * amount;
							pi.setItemInHand(new ItemStack(Material.AIR));
							economy.depositPlayer(player.getName(), cost);
							player.sendMessage(ChatColor.GOLD + "You have sold " + ChatColor.YELLOW + amount + " " + mi.getAlias() + ChatColor.GOLD + " for " + ChatColor.YELLOW + cost + " Smudges.");
						}
					}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "I am sorry, but you can't sell your hand.");
					}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "I am sorry, but you can't sell your hand.");
					}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You don't have permission to buy or sell at other races' markets!" + "\n Please use your own race's market to buy and sell items.");
			}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You have to be in a market to sell items!");
			}
			}
		}
		else if(commandlabel.equalsIgnoreCase("setbuyprice") && args.length>0)
		{
			if(player.hasPermission("SmudgeMarket.admin"))
			{
			MarketPlayer mp = marketplayers.getMarketPlayer(player);
			if(mp!=null)
			{
				if(mp.getMarket()!=null)
				{
					Market market = mp.getMarket();
					if(isNumber(args[1]))
					{
						int itemid = Integer.parseInt(args[1]);
						MarketItem mi = market.getMarketItemById(itemid);
						if(mi!=null)
						{
						if(args.length>2)
						{
						if(isNumber(args[2]))
						{
							int price = Integer.parseInt(args[2]);
							if(price>=-1)
							{
							mi.setBuyCost(price);
							market.getMarketItems().remove(market.getMarketItemById(itemid));
							market.getMarketItems().add(mi);
							this.getConfig().set(market.getName() + ".Items." + mi.getAlias() + ".BuyCost", price);
							saveConfig();
							player.sendMessage(ChatColor.GREEN + "Buycost of " + ChatColor.AQUA + mi.getAlias() + ChatColor.GREEN + " in market " + ChatColor.AQUA + market.getName() + ChatColor.GREEN + " has been set to " + ChatColor.AQUA + price);
							}
							else
							{
							player.sendMessage(ChatColor.RED + "You can't enter a negative price unless it is -1!");
							}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "The price can't be " + ChatColor.AQUA + args[2]);
						}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You have to enter price!");
						}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "There is no item by the id: " + ChatColor.AQUA + itemid);
						}
					}
					else
					{
					String itemname = args[1].toUpperCase();
					MarketItem mi = market.getMarketItemByName(itemname);
					if(mi!=null)
					{
					if(args.length>2)
					{
					if(isNumber(args[2]))
					{
						int price = Integer.parseInt(args[2]);
						if(price>=-1)
						{
						mi.setBuyCost(price);
						market.getMarketItems().remove(market.getMarketItemByName(itemname));
						market.getMarketItems().add(mi);
						this.getConfig().set(market.getName() + ".Items." + mi.getAlias() + ".BuyCost", price);
						saveConfig();
						player.sendMessage(ChatColor.GREEN + "Buycost of " + ChatColor.AQUA + mi.getAlias() + ChatColor.GREEN + " in market " + ChatColor.AQUA + market.getName() + ChatColor.GREEN + " has been set to " + ChatColor.AQUA + price);
						}
						else
						{
						player.sendMessage(ChatColor.RED + "You can't enter a negative price unless it is -1");
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "The price can't be " + ChatColor.AQUA + args[2]);
					}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "You have to enter price!");
					}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "There is no item by the name: " + ChatColor.AQUA + itemname);
					}
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + "You have to be in a market to do that!");
				}
			}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "I am just going to ignore that you just tried to do that.");
			}
		}
		else if(commandlabel.equalsIgnoreCase("setsellprice") && args.length>0)
		{
			if(player.hasPermission("SmudgeMarket.admin"))
			{
			MarketPlayer mp = marketplayers.getMarketPlayer(player);
			if(mp!=null)
			{
				if(mp.getMarket()!=null)
				{
					Market market = mp.getMarket();
					if(isNumber(args[1]))
					{
						int itemid = Integer.parseInt(args[1]);
						MarketItem mi = market.getMarketItemById(itemid);
						if(mi!=null)
						{
						if(args.length>2)
						{
						if(isNumber(args[2]))
						{
							int price = Integer.parseInt(args[2]);
							if(price>=-1)
							{
							mi.setSellCost(price);
							market.getMarketItems().remove(market.getMarketItemById(itemid));
							market.getMarketItems().add(mi);
							this.getConfig().set(market.getName() + ".Items." + mi.getAlias() + ".SellCost", price);
							saveConfig();
							player.sendMessage(ChatColor.GREEN + "Sellcost of " + ChatColor.AQUA + mi.getAlias() + ChatColor.GREEN + " in market " + ChatColor.AQUA + market.getName() + ChatColor.GREEN + " has been set to " + ChatColor.AQUA + price);
							}
							else
							{
							player.sendMessage(ChatColor.RED + "You can't enter a negative price unless it is -1!");
							}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "The price can't be " + ChatColor.AQUA + args[2]);
						}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "You have to enter price!");
						}
						}
						else
						{
							player.sendMessage(ChatColor.RED + "There is no item by the id: " + ChatColor.AQUA + itemid);
						}
					}
					else
					{
					String itemname = args[1].toUpperCase();
					MarketItem mi = market.getMarketItemByName(itemname);
					if(mi!=null)
					{
					if(args.length>2)
					{
					if(isNumber(args[2]))
					{
						int price = Integer.parseInt(args[2]);
						if(price>=-1)
						{
						mi.setSellCost(price);
						market.getMarketItems().remove(market.getMarketItemByName(itemname));
						market.getMarketItems().add(mi);
						this.getConfig().set(market.getName() + ".Items." + mi.getAlias() + ".SellCost", price);
						saveConfig();
						player.sendMessage(ChatColor.GREEN + "Sellcost of " + ChatColor.AQUA + mi.getAlias() + ChatColor.GREEN + " in market " + ChatColor.AQUA + market.getName() + ChatColor.GREEN + " has been set to " + ChatColor.AQUA + price);
						}
						else
						{
						player.sendMessage(ChatColor.RED + "You can't enter a negative price unless it is -1");
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "The price can't be " + ChatColor.AQUA + args[2]);
					}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "You have to enter price!");
					}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "There is no item by the name: " + ChatColor.AQUA + itemname);
					}
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + "You have to be in a market to do that!");
				}
			}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "I am just going to ignore that you just tried to do that.");
			}
		}
		return true;
	}
	
	public boolean isNumber(String string)
	{
		boolean parsable=true;
		try
		{
			Integer.parseInt(string);
		}
		catch(NumberFormatException e)
		{
			parsable=false;
		}
		return parsable;
	}
	
	public void giveItemStack(ItemStack is, Player player)
	{
		int size = is.getAmount();
		int maxsize = is.getMaxStackSize();
		int stacks;
		PlayerInventory pi = player.getInventory();
		if(maxsize>=size)
		{
			player.getInventory().addItem(is);
		}
		else
		{
			stacks = size/maxsize;
			  if(size%maxsize==0)
			   {
			    for(int i=0;i<stacks;++i)
			    {
			     pi.addItem(new ItemStack(is.getType(), is.getMaxStackSize()));
			    }
			   }
			   else
			   {
			    for(int i=0;i<stacks;++i)
			    {
			     pi.addItem(new ItemStack(is.getType(), is.getMaxStackSize()));
			    }
			    pi.addItem(new ItemStack(is.getType(), size%maxsize));
			   }
		}
	}
	
	public boolean hasSpace(ItemStack is, Player player)
	{
		int size = is.getAmount();
		int maxsize = is.getMaxStackSize();
		int slots=0;
		int stacks;
		
		for(ItemStack items : player.getInventory().getContents())
		{
			if(items==null || items.getType().equals(Material.AIR))
				++slots;
		}
		
		if(slots<=0)
		{
			return false;
		}
		
		
		if(size<=maxsize)
		{
			return true;
		}
		else
		{
			stacks = size/maxsize;
			if(maxsize%size!=0)
				stacks++;
		}
		
		if(slots>=stacks)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	/*
	public boolean getDurability(ItemStack is, String alias)
	{
		if()
	}*/
	
	public int sellCostOfTool(int cost, ItemStack is)
	{
		float durability = Material.getMaterial(is.getTypeId()).getMaxDurability()-is.getDurability();
		float percentage = durability/Material.getMaterial(is.getTypeId()).getMaxDurability();
		float finalcost = cost*percentage;
		int fincost = (int) finalcost;
		return fincost;
	}
	/*
	public short getMaxDurability(int itemid)
	{
		switch(itemid)
		{
		case 256: return Material.IRON_SPADE.getMaxDurability();
		case 269: return Material.WOOD_SPADE.getMaxDurability();
		case 273: return Material.STONE_SPADE.getMaxDurability();
		case 277: return Material.DIAMOND_SPADE.getMaxDurability();
		case 284: return Material.GOLD_SPADE.getMaxDurability();
		case 267: return Material.IRON_SWORD.getMaxDurability();
		case 268: return Material.WOOD_SWORD.getMaxDurability();
		case 272: return Material.STONE_SWORD.getMaxDurability();
		case 276: return Material.DIAMOND_SWORD.getMaxDurability();
		case 283: return Material.GOLD_SWORD.getMaxDurability();
		case 290: return Material.WOOD_HOE.getMaxDurability();
		case 291: return Material.STONE_HOE.getMaxDurability();
		case 292: return Material.IRON_HOE.getMaxDurability();
		case 293: return Material.DIAMOND_HOE.getMaxDurability();
		case 294: return Material.GOLD_HOE.getMaxDurability();
		case 257: return Material.IRON_PICKAXE.getMaxDurability();
		case 258: return Material.IRON_AXE.getMaxDurability();
		case 270: return Material.WOOD_PICKAXE.getMaxDurability();
		case 271: return Material.WOOD_AXE.getMaxDurability();
		case 274: return Material.STONE_PICKAXE.getMaxDurability();
		case 275: return Material.STONE_AXE.getMaxDurability();
		case 278: return Material.DIAMOND_PICKAXE.getMaxDurability();
		case 279: return Material.DIAMOND_AXE.getMaxDurability();
		case 285: return Material.GOLD_PICKAXE.getMaxDurability();
		case 286: return Material.GOLD_AXE.getMaxDurability();
		case 298: return Material.LEATHER_HELMET.getMaxDurability();
		case 302: return Material.CHAINMAIL_HELMET.getMaxDurability();
		case 306: return Material.IRON_HELMET.getMaxDurability();
		case 310: return Material.DIAMOND_HELMET.getMaxDurability();
		case 314: return Material.GOLD_HELMET.getMaxDurability();
		case 299: return true;
		case 303: return true;
		case 307: return true;
		case 311: return true;
		case 315: return true;
		case 300: return true;
		case 304: return true;
		case 308: return true;
		case 312: return true;
		case 316: return true;
		case 301: return true;
		case 305: return true;
		case 309: return true;
		case 313: return true;
		case 317: return true;
		default: return 0;
		}
	}*/
	
	public boolean hasHex(MarketItem mi)
	{
		if(mi.hasDurability())
			return true;
		return false;
	}
	
	
	
	public boolean canBreak(int itemid)
	{
		switch(itemid)
		{
		case 256: return true;
		case 269: return true;
		case 273: return true;
		case 277: return true;
		case 284: return true;
		case 267: return true;
		case 268: return true;
		case 272: return true;
		case 276: return true;
		case 283: return true;
		case 290: return true;
		case 291: return true;
		case 292: return true;
		case 293: return true;
		case 294: return true;
		case 257: return true;
		case 258: return true;
		case 270: return true;
		case 271: return true;
		case 274: return true;
		case 275: return true;
		case 278: return true;
		case 279: return true;
		case 285: return true;
		case 286: return true;
		case 298: return true;
		case 302: return true;
		case 306: return true;
		case 310: return true;
		case 314: return true;
		case 299: return true;
		case 303: return true;
		case 307: return true;
		case 311: return true;
		case 315: return true;
		case 300: return true;
		case 304: return true;
		case 308: return true;
		case 312: return true;
		case 316: return true;
		case 301: return true;
		case 305: return true;
		case 309: return true;
		case 313: return true;
		case 317: return true;
		
		default: return false;
		}
	}
	
	public boolean containsAtLeast(PlayerInventory pi, Material mat, int amount)
	{
		int number=0;
		for(ItemStack is : pi.getContents())
		{
			if(is!=null)
			{
			if(is.getType().equals(mat))
			{
				for(int i=0;i<is.getAmount();++i)
				{
				++number;
				}
			}
			}
		}
		if(number>=amount)
			return true;
		return false;
	}
	
	public boolean containsAtLeast(PlayerInventory pi, Material mat, int amount, short durability)
	{
		int number=0;
		for(ItemStack is : pi.getContents())
		{
			if(is!=null)
			{
				if(is.getType().equals(mat))
				{
					if(is.getDurability()==durability)
					{
						for(int i=0;i<is.getAmount();++i)
						{
						++number;
						}
					}
				}
			}
		}
		if(number>=amount)
			return true;
		return false;
	}
}
