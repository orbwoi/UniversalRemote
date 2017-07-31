package clayborn.universalremote.registrar;

import clayborn.universalremote.items.ItemBase;
import clayborn.universalremote.items.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Registrar {
	@SubscribeEvent
    public void onBlockRegistry(Register<Block> event) {
		//TODO
    }

    @SubscribeEvent
    public void onItemRegistry(Register<Item> event) {
    	
    	// register all the things!
    	for (ItemBase item : ItemRegistry.Items().All)
    	{
    		item.register(event);
    	}
    	
    }

    @SubscribeEvent
    public void onCraftingRegistry(Register<IRecipe> event) {
    	//TODO
    }
}
