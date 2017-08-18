package clayborn.universalremote.network;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import clayborn.universalremote.world.RemoteGuiEnabledClientWorld;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.world.WorldSettings;

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
		try {
			
	    	NetHandlerPlayClient handler = (NetHandlerPlayClient) m_manager.getNetHandler();
	    	
	    	Minecraft gameController = InjectionHandler.readFieldOfType(handler, Minecraft.class);  
        
	        // gameController.playerController
	        gameController.playerController = new PlayerControllerMP(gameController, handler);
	        
	        // clientWorldController
	        WorldClient clientWorldController = 
	        		new RemoteGuiEnabledClientWorld(handler, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), net.minecraftforge.fml.common.network.handshake.NetworkDispatcher.get(handler.getNetworkManager()).getOverrideDimension(packetIn), packetIn.getDifficulty(), gameController.mcProfiler);
	        
	        InjectionHandler.writeFieldOfType(handler, clientWorldController, WorldClient.class);
	        
	        // other settings
	        gameController.gameSettings.difficulty = packetIn.getDifficulty();
	        
	        gameController.loadWorld(clientWorldController);
	        
	        gameController.player.dimension = packetIn.getDimension();
	        gameController.displayGuiScreen(new GuiDownloadTerrain(handler));
	        gameController.player.setEntityId(packetIn.getPlayerId());
	        
	        handler.currentServerMaxPlayers = packetIn.getMaxPlayers();
	        
	        gameController.player.setReducedDebug(packetIn.isReducedDebugInfo());
	        gameController.playerController.setGameType(packetIn.getGameType());
	        gameController.gameSettings.sendSettingsToServer();	        
	        
	        handler.getNetworkManager().sendPacket(new CPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
        
		} catch (IllegalAccessException e) {
			
			Util.logger.logException("Unable to process SPacketJoinGame!", e);
			
		}
		
    }

}
