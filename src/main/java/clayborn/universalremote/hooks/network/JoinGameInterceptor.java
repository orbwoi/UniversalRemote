package clayborn.universalremote.hooks.network;

import clayborn.universalremote.hooks.client.MinecraftProxy;
import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.SPacketJoinGame;

public class JoinGameInterceptor extends SimpleChannelInboundHandler<SPacketJoinGame> {

	private NetworkManager m_manager;

	public JoinGameInterceptor(NetworkManager manager)
	{
		m_manager = manager;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SPacketJoinGame msg) throws Exception {

        if (ctx.channel().isOpen())
        {
            try
            {

    	    	try {

    	    		NetHandlerPlayClient handler = (NetHandlerPlayClient) m_manager.getNetHandler();

    	    		Minecraft scheduler = InjectionHandler.readFieldOfType(handler, Minecraft.class);

	                if (!scheduler.isCallingFromMinecraftThread())
	                {
	                    scheduler.addScheduledTask(new Runnable()
	                    {
	                        public void run()
	                        {
	                            invoke(msg);
	                        }
	                    });
	                    throw ThreadQuickExitException.INSTANCE;
	                } else {
	                	invoke(msg);
	                }

    	    	} catch (Exception e) {

    	    		if (e instanceof ThreadQuickExitException) throw e;

    	    		Util.logger.logException("Unable to get scheduler!", e);

    	    		// we died - let vanilla take over!
    	    		ctx.fireChannelRead(msg);
    	    		return;
    	    	}



            }
            catch (ThreadQuickExitException var4)
            {
                ;
            }
        }

	}

    public void invoke(SPacketJoinGame packetIn)
    {

    	NetHandlerPlayClient handler = (NetHandlerPlayClient) m_manager.getNetHandler();

		MinecraftProxy.INSTANCE.SyncFromReal();

		try {
			InjectionHandler.writeFieldOfType(handler, MinecraftProxy.INSTANCE, Minecraft.class);
		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to hook minecraft instance via MinecraftProxy!", e);
		}

		handler.handleJoinGame(packetIn);

		MinecraftProxy.INSTANCE.SyncToReal();

		// sync the world!
		try {
			InjectionHandler.writeFieldOfType(handler, Minecraft.getMinecraft().world, WorldClient.class);
		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to hook minecraft instance via MinecraftProxy!", e);
		}

		// restore the old Minecraft if not done so already
		try {
			InjectionHandler.writeFieldOfType(handler, Minecraft.getMinecraft(), Minecraft.class);
		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to hook minecraft instance via MinecraftProxy!", e);
		}

    }

}
