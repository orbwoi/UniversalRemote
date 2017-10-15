package clayborn.universalremote.network;

import java.util.EnumMap;

import clayborn.universalremote.hooks.network.OpenGuiFilterServer;
import clayborn.universalremote.settings.Strings;
import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import io.netty.channel.ChannelPipeline;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.network.internal.FMLRuntimeCodec;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RemoteGuiNetworkManager {

	// singleton instance for network pipeline
	public static final RemoteGuiNetworkManager INSTANCE = new RemoteGuiNetworkManager();

	// private state

	protected OpenGuiFilterServer m_openGuiFilter;
	protected SimpleNetworkWrapper m_customPacketChannel;

	// unique id generator
	protected int nextId = 0;

	protected int getNextId()
	{
		return nextId++;
	}

	// constructor

	public RemoteGuiNetworkManager()
	{
		m_customPacketChannel = NetworkRegistry.INSTANCE.newSimpleChannel(Strings.MODID);
		m_openGuiFilter = new OpenGuiFilterServer(this);
	}

	// preInit event to add our filter to intercept OpenGui FML messages
	@SuppressWarnings("unchecked")
	public void preInit(FMLPreInitializationEvent event)
	{

		Util.logger.info("Injecting FML packet filter...");

		try {

			EnumMap<Side, FMLEmbeddedChannel> channelPair
			    = InjectionHandler.readStaticFieldOfType(FMLNetworkHandler.class, EnumMap.class);

			String targetName = channelPair.get(Side.SERVER).findChannelHandlerNameForType(FMLRuntimeCodec.class);

			ChannelPipeline pipeline = channelPair.get(Side.SERVER).pipeline();

			pipeline.addAfter(targetName, "universalremote_OpenGuiFilter", m_openGuiFilter);


		} catch (Exception e) {
			Util.logger.logException("Unable to inject into FMLNetworkHandler!", e);
		}

	}

	public void init(FMLInitializationEvent event)
	{
    	// register our packets

		m_customPacketChannel.registerMessage(
    			RemoteGuiMessage.RemoteGuiMessageHandler.class,
    			RemoteGuiMessage.class, this.getNextId(), Side.CLIENT);

	}

	public void sendPacketToPlayer(IMessage msg, EntityPlayerMP player)
	{
		m_customPacketChannel.sendTo(msg, player);
	}



}
