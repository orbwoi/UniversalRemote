package clayborn.universalremote.network;

import java.util.EnumMap;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import io.netty.channel.ChannelPipeline;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.network.internal.FMLRuntimeCodec;
import net.minecraftforge.fml.relauncher.Side;

public class FMLNetworkHandlerInjector {

	@SuppressWarnings("unchecked")
	public static void preInit(FMLPreInitializationEvent event)
	{
		
		Util.logger.info("Injecting FML packet filter...");
		
		try {
			
			EnumMap<Side, FMLEmbeddedChannel> channelPair
			    = InjectionHandler.readStaticFieldOfType(FMLNetworkHandler.class, EnumMap.class);
			
			String targetName = channelPair.get(Side.SERVER).findChannelHandlerNameForType(FMLRuntimeCodec.class);
			
			ChannelPipeline pipeline = channelPair.get(Side.SERVER).pipeline();
			
			pipeline.addAfter(targetName, "universalremote_OpenGuiFilter", OpenGuiFilterServer.INSTANCE);		
	
			
		} catch (Exception e) {
			Util.logger.logException("Unable to inject into FMLNetworkHandler!", e);
		}
	
	}
	
}
