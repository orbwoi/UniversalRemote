package clayborn.universalremote.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class Util {
	
	public static org.apache.logging.log4j.Logger logger;
	
	public static boolean doesStringStartWithAnyInArray(String[] prefixList, String compare)
	{

		for(String s : prefixList)
		{
			if (compare.startsWith(s)) return true;
		}
		
		return false;
	}
	
	public static ItemStack playerAndHandToItemStack(EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = null;
		
    	// okay, find the itemstack
		if (hand == EnumHand.MAIN_HAND)
		{
			stack = player.getHeldItemMainhand();
		} 
		else if (hand == EnumHand.OFF_HAND)
		{
			stack = player.getHeldItemOffhand();
		}
		else
		{
			// uhh... what?
			Util.logger.error("Found invalid EnumHand value!");
		}
		
		return stack;
	}
	
	public static void setPlayerItemStackInHand(ItemStack stack, EntityPlayer player, EnumHand hand)
	{
		
    	// okay, find the itemstack
		if (hand == EnumHand.MAIN_HAND)
		{
			player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);
		} 
		else if (hand == EnumHand.OFF_HAND)
		{
			player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
		}
		else
		{
			// uhh... what?
			Util.logger.error("Found invalid EnumHand value!");
		}
		
	}
	
}
