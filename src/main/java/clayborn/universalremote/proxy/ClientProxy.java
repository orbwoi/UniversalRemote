package clayborn.universalremote.proxy;

import clayborn.universalremote.hooks.client.MinecraftProxy;
import clayborn.universalremote.hooks.events.HookedClientWorldEventSync;
import clayborn.universalremote.hooks.network.VanillaPacketInterceptorInjector;
import clayborn.universalremote.hooks.server.ServerInjector;
import clayborn.universalremote.registrar.ClientRegistrar;
import clayborn.universalremote.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

public class ClientProxy implements ISidedProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		Util.logger.info("Starting client pre-initalization...");

		try {
			MinecraftProxy.INSTANCE = new MinecraftProxy(Minecraft.getMinecraft());
		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to create MinecraftProxy!", e);
		}

		MinecraftForge.EVENT_BUS.register(new ClientRegistrar());
    	MinecraftForge.EVENT_BUS.register(new VanillaPacketInterceptorInjector());
    	MinecraftForge.EVENT_BUS.register(new HookedClientWorldEventSync());

		Util.logger.info("Client pre-initalization complete!");
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
    	ServerInjector.InjectIntegrated(FMLCommonHandler.instance().getMinecraftServerInstance());
	}

	public boolean isSinglePlayer()
	{
		return true;
	}

}
