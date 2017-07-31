package clayborn.universalremote.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class CapabilityHelper {

	public static <T> T tryGetCapability(ItemStack stack, Capability<T> capability, EnumFacing facing)
	{
        if(stack.hasCapability(capability, facing)){
            return (T)stack.getCapability(capability, facing);
        }
        return null;
	}
	
}
