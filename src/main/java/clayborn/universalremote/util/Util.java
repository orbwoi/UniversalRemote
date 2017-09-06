package clayborn.universalremote.util;

import clayborn.universalremote.registrar.Registrar;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;

public class Util {

	public static Logger logger;

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

	public static String getNiceDimensionName(int dim)
	{
        // try to get a nice dimension name
		String dimName = DimensionManager.getProvider(dim).getDimensionType().getName().
				replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2").replace("_", " ").trim();

		// TODO clean up this formatting mess
		String[] dimNameWords = dimName.split(" ");

		for(int i = 0; i < dimNameWords.length; i++)
		{
			if (dimNameWords[i].length() > 1)
			{
				dimNameWords[i] = dimNameWords[i].substring(0, 1).toUpperCase() + dimNameWords[i].substring(1);
			}
		}

		return String.join(" ", dimNameWords);
	}

	public static String getClassDomainFromName(String className) {

		String[] parts = className.split("\\.");

		String prefix = "";

		int index = 0;

		for (; index < Math.min(parts.length, 3); index++)
		{
			prefix += parts[index];

			if (!parts[index].equals("net") && !parts[index].equals("com") && !parts[index].equals("org")) break;

			prefix += ".";
		}

		return prefix;

	}

	public static boolean isPrefixInCallStack(String prefix) {

		if (prefix == null || prefix.length() == 0) return false;

		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		for (StackTraceElement e : stack)
		{
			if (e.getClassName().startsWith(prefix)) return true;
		}

		return false;

	}

	public static String getBlockModId(Block block) {
		// find the modId of the block
		ResourceLocation loc = Registrar.BLOCK_REGISTRY.getKey(block);
		return loc.getResourceDomain();
	}

}
