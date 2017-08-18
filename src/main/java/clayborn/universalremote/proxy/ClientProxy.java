package clayborn.universalremote.proxy;

import clayborn.universalremote.network.VanillaPacketInterceptorInjector;
import clayborn.universalremote.registrar.ClientRegistrar;
import clayborn.universalremote.util.Util;
import clayborn.universalremote.world.RemoteGuiEnabledClientWorldEventSync;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy implements ISidedProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		Util.logger.info("Starting client pre-initalization...");
		
		MinecraftForge.EVENT_BUS.register(new ClientRegistrar());
    	MinecraftForge.EVENT_BUS.register(new VanillaPacketInterceptorInjector());
    	MinecraftForge.EVENT_BUS.register(new RemoteGuiEnabledClientWorldEventSync());
    	
		Util.logger.info("Client pre-initalization complete!");
	}

	@Override
	public void init(FMLInitializationEvent event) {

	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

	}
	
	public boolean isSinglePlayer()
	{
		return true;
	}

}
