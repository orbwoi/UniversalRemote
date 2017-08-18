package clayborn.universalremote.network;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import clayborn.universalremote.world.RemoteGuiEnabledClientWorld;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.WorldSettings;

public class RespawnInterceptor extends SimpleChannelInboundHandler<SPacketRespawn> {

	private NetworkManager m_manager;
	
	public RespawnInterceptor(NetworkManager manager)
	{
		m_manager = manager;
	}	
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SPacketRespawn msg) throws Exception {
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
	
	public void invoke(SPacketRespawn packetIn)
    {
		try {
			
	    	NetHandlerPlayClient handler = (NetHandlerPlayClient) m_manager.getNetHandler();
	    	
	    	Minecraft gameController = InjectionHandler.readFieldOfType(handler, Minecraft.class);   

	        if (packetIn.getDimensionID() != gameController.player.dimension)
	        {
	        	// this also writes "hasStatistics" but it doesn't seem to be used anywhere
	        	InjectionHandler.writeAllFieldsOfType(handler, true, boolean.class);
	        	
	        	// clientWorldController Old
	        	WorldClient clientWorldController = InjectionHandler.readFieldOfType(handler, WorldClient.class);
	        	
	            Scoreboard scoreboard = clientWorldController.getScoreboard();	            
	            
	            // clientWorldController New
	            clientWorldController = 
	            		new RemoteGuiEnabledClientWorld(handler, new WorldSettings(0L, packetIn.getGameType(), false, gameController.world.getWorldInfo().isHardcoreModeEnabled(), packetIn.getWorldType()), packetIn.getDimensionID(), packetIn.getDifficulty(), gameController.mcProfiler);
	            
	            InjectionHandler.writeFieldOfType(handler, clientWorldController, WorldClient.class);
	            	            
	            // other settings
	            clientWorldController.setWorldScoreboard(scoreboard);
	            gameController.loadWorld(clientWorldController);
	            gameController.player.dimension = packetIn.getDimensionID();
	            gameController.displayGuiScreen(new GuiDownloadTerrain(handler));
	        }

	        // other settings
	        gameController.setDimensionAndSpawnPlayer(packetIn.getDimensionID());
	        gameController.playerController.setGameType(packetIn.getGameType());

        
		} catch (IllegalAccessException e) {
			
			Util.logger.logException("Unable to process SPacketJoinGame!", e);
			
		}
		
    }

}
