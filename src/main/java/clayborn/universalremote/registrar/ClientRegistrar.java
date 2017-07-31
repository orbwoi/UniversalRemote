package clayborn.universalremote.registrar;

import clayborn.universalremote.items.ItemBase;
import clayborn.universalremote.items.ItemRegistry;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientRegistrar {

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event){

    	// register all the things!
    	for (ItemBase item : ItemRegistry.Items().All)
    	{
    		item.register(event);
    	}

    }
	
}
