package clayborn.universalremote.proxy;

import clayborn.universalremote.hooks.server.ServerInjector;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

public class ServerProxy implements ISidedProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {

	}

	@Override
	public void init(FMLInitializationEvent event) {

	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

	}

	@Override
	public void serverAboutToStart(FMLServerAboutToStartEvent event) {
		// inject server
    	ServerInjector.InjectDedicated(FMLCommonHandler.instance().getMinecraftServerInstance());
	}

	public boolean isSinglePlayer()
	{
		return false;
	}

}
