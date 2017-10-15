package clayborn.universalremote.hooks.network;

import clayborn.universalremote.hooks.events.PlayerRemoteGuiDataManagerServer;
import clayborn.universalremote.hooks.events.PlayerRemoteGuiDataManagerServer.RemoteGuiPlayerData;
import clayborn.universalremote.network.RemoteGuiMessage;
import clayborn.universalremote.network.RemoteGuiNetworkManager;
import clayborn.universalremote.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.TypeParameterMatcher;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLMessage;

public class OpenGuiFilterServer extends ChannelOutboundHandlerAdapter {

	protected final TypeParameterMatcher m_OpenGuiMatcher;
	protected RemoteGuiNetworkManager m_manager;

	public OpenGuiFilterServer(RemoteGuiNetworkManager manager) {
		m_OpenGuiMatcher = TypeParameterMatcher.get(FMLMessage.OpenGui.class);
		m_manager = manager;
	}

    /**
     * Returns {@code true} if the given message should be handled. If {@code false} it will be passed to the next
     * {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     */
    protected boolean acceptOutboundMessage(Object msg) throws Exception {
        return m_OpenGuiMatcher.match(msg);
    }

    protected boolean dataMatches(OpenGuiWrapper packet, RemoteGuiPlayerData data) throws IllegalAccessException
    {
    	return
			packet.getModId().equals(data.modId) &&

			// it should match the tile...
			(packet.getX() == data.x &&
			packet.getY() == data.y &&
			packet.getZ() == data.z) ||

			// or the player!
			(packet.getX() == (int)data.playerPos.x &&
			packet.getY() == (int)data.playerPos.y &&
			packet.getZ() == (int)data.playerPos.z);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
    {

        try {

        	boolean sendToForge = true;

            if (acceptOutboundMessage(msg)) {

            	 Object playerObject =
                ctx.channel().attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).get();

            	 if (playerObject instanceof EntityPlayerMP)
            	 {

	            	 EntityPlayerMP entityPlayerMP = (EntityPlayerMP)playerObject;

	            	 OpenGuiWrapper imsg = new OpenGuiWrapper((FMLMessage.OpenGui) msg);

	            	 // does it match the player's last remote usage?
	                 RemoteGuiPlayerData data = PlayerRemoteGuiDataManagerServer.INSTANCE.getPlayerData(entityPlayerMP);

            		 if (data != null) {

            			 // clear re-route flag
            			 data.rerouted = false;

            			 if (data.count < 2)
            			 {

			            	 sendToForge = false; // don't process it the normal way- we'll handle this.

	            			 if (this.dataMatches(imsg, data))
	            			 {

				            	 // send the custom packet!
	            				 m_manager.sendPacketToPlayer(
				            			 new RemoteGuiMessage(imsg, data.blockId, data.updateTag, data.readTag, null, data.dimensionId), entityPlayerMP);

	            			 } else {

	            				 HandleDataMismatch(imsg, data, entityPlayerMP, ctx);

	            			 }

	            			 // Refresh our data to see if something changed
	            			 data = PlayerRemoteGuiDataManagerServer.INSTANCE.getPlayerData(entityPlayerMP);

		            		 // if we aren't re-reoute clear the data!
		            		 if (!data.rerouted)
		            		 {
			            		 // the player opened something, clear the data
		            			 PlayerRemoteGuiDataManagerServer.INSTANCE.CancelRemoteActivation(entityPlayerMP);
		            		 }

            			 } else {

            				 Util.logger.warn("Ran out of re-tries attempting to covert OpenGui to RemoteGuiMessage!");

            				 // clear the data since we are giving up...
	            			 PlayerRemoteGuiDataManagerServer.INSTANCE.CancelRemoteActivation(entityPlayerMP);

            				 // we got stuck in a loop, send to native...
	            			 sendToForge = true;

            			 }


	            	 }

            	 }

            }

            if (sendToForge)
            {

                ctx.write(msg, promise);

            }

        } catch (Throwable t) {
        	Util.logger.logException("OpenGUI filter died", t);
        } finally { }

    }

    private void PrepareReissueRequest(OpenGuiWrapper msg, RemoteGuiPlayerData data, EntityPlayerMP player)
    {

    	WorldServer world = DimensionManager.getWorld(data.dimensionId);

    	try {


	    	int x = msg.getX();
	    	int y = msg.getY();
	    	int z = msg.getZ();

    		BlockPos newPos = new BlockPos(x, y, z);
    		String newModId = Util.getBlockModId(world.getBlockState(newPos).getBlock());

	    	// are we on the same mod?
	    	if (!data.modId.equals(newModId))
	    	{
	    		Util.logger.warn("ModId changed from {} to {} in re-try!", data.modId, newModId);

	    		// uh ho, it's a different mod - use native GUI
	    		data.count = Integer.MAX_VALUE;
	    		data.rerouted = true;

	    		PlayerRemoteGuiDataManagerServer.INSTANCE.setPlayerData(player, data);

	    	}
	    	else
	    	{

		    	// find the real block
		    	PlayerRemoteGuiDataManagerServer.INSTANCE.PrepareForRemoteActivation(world, player, newPos, data.playerPos);

		    	RemoteGuiPlayerData newdata = PlayerRemoteGuiDataManagerServer.INSTANCE.getPlayerData(player);

		    	if (data.count < Integer.MAX_VALUE)
		    	{
			    	// set count > 0 to prevent recursion!
			    	newdata.count = data.count + 1;
		    	}

		    	// remember the alamo!
		    	newdata.modGuiId = msg.getModGuiId();
		    	newdata.rerouted = true;

	    	}


		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to set remote gui player data for re-try!", e);

			// this is bad... we can't recover from here... we have to just drop the open gui request :(
		}

    }

    private void HandleDataMismatch(OpenGuiWrapper msg, RemoteGuiPlayerData data, EntityPlayerMP player, ChannelHandlerContext ctx)
    {
		 // sometimes multiblocks trigger UI from another position!
		 // time to re-issue with the correct information

		 // however we could be on the networking thread!

    	IThreadListener scheduler = FMLCommonHandler.instance().getWorldThread(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());

        if (!scheduler.isCallingFromMinecraftThread())
        {
        	Util.logger.warn("HandleDataMismatch scheduling PrepareReissueRequest!");
            scheduler.addScheduledTask(new Runnable()
            {
                @Override
				public void run()
                {
                	PrepareReissueRequest(msg, data, player);
                }
            });

        } else {

        	PrepareReissueRequest(msg, data, player);

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log.error("OpenGuiHandler exception", cause);
        super.exceptionCaught(ctx, cause);
    }
}
