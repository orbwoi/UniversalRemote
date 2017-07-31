package clayborn.universalremote.items;

import clayborn.universalremote.settings.Strings;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent.Register;

public class ItemBase extends Item {
	
	public ItemBase(String name, CreativeTabs tab)
	{
		this.setUnlocalizedName(Strings.MODID + "." + name);
		this.setRegistryName(Strings.MODID, name);
		
		if (tab != null)
		{
			this.setCreativeTab(tab);
		}
	}
	
	// default behavior 
	public void register(Register<Item> event)
	{
		// register the item
		event.getRegistry().register(this);		
	}
	
	// default behavior 
	public void register(ModelRegistryEvent event)
	{
		// register the model	
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
	}
	
}
