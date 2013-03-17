package net.smudgecraft.marwzoor;

/*import java.util.HashMap;

import org.bukkit.Material;*/

public class Identifiers 
{
	/*public static HashMap<Material, String[]> itemidentifiers = new HashMap<Material, String[]>();
	
	public static void setIdentifiers()
	{
		String[] anvil = {"anvil", "anvils"};
		itemidentifiers.put(Material.ANVIL, anvil);
		
		String[] apple = {"apple", "apples"};
		itemidentifiers.put(Material.APPLE, apple);
		
		String[] arrow = {"arrow", "arrows"};
		itemidentifiers.put(Material.ARROW, arrow);
		
		String[] baked_potato =  {"bakedpotato", "baked-potato", "baked_potato", "bakedpotatoes", "baked-potatoes", "baked_potatoes"};
		itemidentifiers.put(Material.BAKED_POTATO, baked_potato);
		
		String[] bed = {"bed", "beds"};
		itemidentifiers.put(Material.BED, bed);
		
		String[] birch_wood_stairs = {"birchstairs", "birchstair", "birchwoodstair", "birch-wood-stairs", "birchwoodstairs", "birch_wood_stairs"};
		itemidentifiers.put(Material.BIRCH_WOOD_STAIRS, birch_wood_stairs);
		
		String[] blaze_powder = {"blazepowder", "blaze-powder", "blaze_powder"};
		itemidentifiers.put(Material.BLAZE_POWDER, blaze_powder);
		
		String[] blaze_rod =  {"blazerod", "blaze_rod", "blaze-rod", "blazerods", "blaze_rods", "blaze-rods"};
		itemidentifiers.put(Material.BLAZE_ROD, blaze_rod);
		
		String[] boat = {"boat", "boats"};
		itemidentifiers.put(Material.BOAT, boat);
		
		String[] bone = {"bone", "bones"};
		itemidentifiers.put(Material.BONE, bone);
		
		String[] book = {"book", "books"};
		itemidentifiers.put(Material.BOOK, book);
		
		String[] book_and_quill = {"bookandquill", "book_and_quill", "book-and-quill"};
		itemidentifiers.put(Material.BOOK_AND_QUILL, book_and_quill);
		
		String[] bookshelf = {"bookshelf", "shelfofbooks", "bookshelves"};
		itemidentifiers.put(Material.BOOKSHELF, bookshelf);
		
		String[] bow = {"bow", "bows"};
		itemidentifiers.put(Material.BOW, bow);
		
		String[] bowl = {"bowl", "bowls"};
		itemidentifiers.put(Material.BOWL, bowl);
		
		String[] bread = {"bread"};
		itemidentifiers.put(Material.BREAD, bread);
		
		String[] brewing_stand = {"brewingstand", "brewing-stand", "brewing_stand", "brewingstands", "brewing-stands", "brewing_stands"};
		itemidentifiers.put(Material.BREWING_STAND, brewing_stand);
		
		String[] brick = {"brick", "bricks"};
		itemidentifiers.put(Material.BRICK, brick);
		
		String[] brick_stairs = {"brickstair", "brickstairs", "brick-stair", "brick-stairs", "brick_stair", "brick_stairs"};
		itemidentifiers.put(Material.BRICK_STAIRS, brick_stairs);
		
		String[] brown_mushroom = {"brownmushroom", "brownmushrooms", "brown-mushroom", "brown-mushrooms", "brown_mushroom", "brown_mushrooms"};
		itemidentifiers.put(Material.BROWN_MUSHROOM, brown_mushroom);
		
		String[] bucket = {"bucket", "buckets"};
		itemidentifiers.put(Material.BUCKET, bucket);
		
		String[] cactus = {"cactus", "cactai"};
		itemidentifiers.put(Material.CACTUS, cactus);
		
		String[] cake = {"cake", "cakes"};
		itemidentifiers.put(Material.CAKE, cake);
		
		String[] carrot = {"carrot", "carrots"};
		itemidentifiers.put(Material.CARROT, carrot);
		
		String[] cauldron = {"cauldron", "cauldrons", "cauldrai"};
		itemidentifiers.put(Material.CAULDRON, cauldron);
		
		String[] chainmail_boots = {"chainmailboots", "chain-mail-boots", "chain_mail_boots", "chainboots", "chain-boots", "chain_boots"};
		itemidentifiers.put(Material.CHAINMAIL_BOOTS, chainmail_boots);
		
		String[] chainmail_chestplate = {"chainmailchestplate", "chain-mail-chestplate", "chain_mail_chestplate", "chainchestplate", "chain-chestplate", "chain_chestplate", "chainmailchestplates", "chain-mail-chestplates", "chain_mail_chestplates", "chainchestplates", "chain-chestplates", "chain_chestplates"};
		itemidentifiers.put(Material.CHAINMAIL_CHESTPLATE, chainmail_chestplate);
		
		String[] chainmail_helmet = {"chainmailhelmet", "chainmailhelmets", "chain-mail-helmet", "chain-mail-helmets", "chain_mail_chestplate", "chain_mail_chestplates", "chainhelmet", "chainhelmets", "chain-helmet", "chain-helmets", "chain_helmet", "chain_helmets"};
		itemidentifiers.put(Material.CHAINMAIL_HELMET, chainmail_helmet);
		
		String[] chainmail_leggings = {"chainmailleggings", "chainleggings", "chain-mail-leggings", "chain-leggings", "chain_mail_leggings", "chain_leggings"};
		itemidentifiers.put(Material.CHAINMAIL_LEGGINGS, chainmail_leggings);
		
		String[] chest = {"chest", "chest"};
		itemidentifiers.put(Material.CHEST, chest);
		
		String[] clay = {"clay"};
		itemidentifiers.put(Material.CLAY, clay);
		
		String[] clay_ball = {"clayball", "clayballs", "clay-ball", "clay-balls", "clay_ball", "clay_balls"};
		itemidentifiers.put(Material.CLAY_BALL, clay_ball);
		
		String[] clay_brick = {"claybrick", "claybricks", "clay-brick", "clay-bricks", "clay_brick", "clay_bricks"};
		itemidentifiers.put(Material.CLAY_BRICK, clay_brick);
		
		String[] coal = {"coal"};
		itemidentifiers.put(Material.COAL, coal);
		
		String[] cobble_wall= {"cobblewall", "cobblewalls", "cobblefence", "cobblefences", "cobble-wall", "cobble-walls", "cobble_wall", "cobble_walls", "cobble-fence", "cobble-fences", "cobble_fence", "cobble_fences"};
		itemidentifiers.put(Material.COBBLE_WALL, cobble_wall);
		
		String[] cobblestone = {"cobblestone", "cobble", "cobblestones", "cobbles"};
		itemidentifiers.put(Material.COBBLESTONE, cobblestone);
		
		String[] cobblestone_stairs = {"cobblestonestairs", "cobblestonestair", "cobblestairs", "cobblestair", "cobblestone-stairs", "cobblestone-stair", "cobblestone_stair", "cobblestone_stairs"};
		itemidentifiers.put(Material.COBBLESTONE_STAIRS, cobblestone_stairs);
		
		String[] cocoa = {"cocoa", "cocoabeans", "cocoabean"};
		itemidentifiers.put(Material.COCOA, cocoa);
		
		String[] compass = {"compass", "compai", "compasses"};
		itemidentifiers.put(Material.COMPASS, compass);
		
		String[] cooked_beef = {"cookedbeef"}
	}
	
	public static boolean arrayContains(String[] str, String string)
	{
		boolean contains=false;
		for(String st : str)
		{
			if(st==string)
			{
				contains=true;
			}
		}
		return contains;
	}
	*/
}