package clayborn.universalremote.proxy;

import clayborn.universalremote.registrar.ClientRegistrar;
import clayborn.universalremote.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy implements ISidedProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		Util.logger.debug("client preInit starting...");
		
		MinecraftForge.EVENT_BUS.register(new ClientRegistrar());
    	
		Util.logger.debug("client preInit complete!");
	}

	@Override
	public void init(FMLInitializationEvent event) {
		Util.logger.debug("client init starting...");
    	
		Util.logger.debug("client init complete!");
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		Util.logger.debug("client postInit starting...");
		
		Util.logger.debug("client postInit complete!");
	}

}
