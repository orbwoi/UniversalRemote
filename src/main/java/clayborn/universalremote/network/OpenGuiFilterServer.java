package clayborn.universalremote.network;

import java.util.HashMap;
import java.util.Map;

import clayborn.universalremote.registrar.Registrar;
import clayborn.universalremote.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.TypeParameterMatcher;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLMessage;

public class OpenGuiFilterServer extends ChannelOutboundHandlerAdapter {

	// singleton instance for network pipeline
	public static final OpenGuiFilterServer INSTANCE = new OpenGuiFilterServer();

	private final TypeParameterMatcher m_OpenGuiMatcher;

	private Map<EntityPlayer, RemoteGuiExtraData> m_playerData = new HashMap<EntityPlayer, RemoteGuiExtraData>();

	// Extra Data store
	public static class RemoteGuiExtraData
	{
		public int blockId;
		public NBTTagCompound updateTag;
		public NBTTagCompound readTag;
		public int x, y, z;
		public String modId;
		public int dimensionId;
		public int count = 0; // recursion detection
		public int modGuiId; // extra storage for re-routing
		public boolean rerouted = false;

		public RemoteGuiExtraData() { }

		public RemoteGuiExtraData(int iBlockId, NBTTagCompound iUpdateTag, NBTTagCompound iReadTag, BlockPos pos, String iModId, int idimensionId)
		{
			// data to send to client
			blockId = iBlockId;
			updateTag = iUpdateTag;
			readTag = iReadTag;

			// make sure this is the right block
			x = pos.getX();
			y = pos.getY();
			z = pos.getZ();
			modId = iModId;
			dimensionId = idimensionId;
		}
	}

	protected OpenGuiFilterServer() {
		m_OpenGuiMatcher = TypeParameterMatcher.get(FMLMessage.OpenGui.class);
	}

    /**
     * Returns {@code true} if the given message should be handled. If {@code false} it will be passed to the next
     * {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     */
    protected boolean acceptOutboundMessage(Object msg) throws Exception {
        return m_OpenGuiMatcher.match(msg);
    }

    protected boolean dataMatches(OpenGuiWrapper packet, RemoteGuiExtraData data) throws IllegalAccessException
    {
    	return
			packet.getModId().equals(data.modId) &&
			packet.getX() == data.x &&
			packet.getY() == data.y &&
			packet.getZ() == data.z;
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
	            	 if (m_playerData.containsKey(entityPlayerMP)) {

	            		 RemoteGuiExtraData data = m_playerData.get(entityPlayerMP);

	            		 if (data != null) {

	            			 // clear re-route flag
	            			 m_playerData.get(entityPlayerMP).rerouted = false;

	            			 if (data.count < 2)
	            			 {

				            	 sendToForge = false; // don't process it the normal way- we'll handle this.

		            			 if (this.dataMatches(imsg, data))
		            			 {

					            	 // send the custom packet!
					            	 UniversalRemotePacketHandler.INSTANCE.sendTo(
					            			 new OpenRemoteGuiMessage(imsg, data.blockId, data.updateTag, data.readTag, data.dimensionId), entityPlayerMP);

		            			 } else {

		            				 HandleDataMismatch(imsg, data, entityPlayerMP, ctx);

		            			 }

	            			 }

	            		 }

	            		 // if we aren't re-reoute clear the data!
	            		 if (!wasRerouted(entityPlayerMP))
	            		 {
		            		 // the player opened something, clear the data
		            		 m_playerData.put(entityPlayerMP, null);
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

    // MUST be call from server thread, NOT network thread!
    public void setPlayerData(World world, EntityPlayer player, BlockPos blockPosition)
	{

    	IBlockState state = world.getBlockState(blockPosition);

		int id = Block.getStateId(state);

		TileEntity tile = world.getTileEntity(blockPosition);

		NBTTagCompound tileUpdateTag = null;
		NBTTagCompound tileReadTag = null;

		if (tile != null)
		{
			tileUpdateTag = tile.getUpdateTag();
			tile.writeToNBT(tileReadTag = new NBTTagCompound());
		}

		// find the modId of the block
		ResourceLocation loc = Registrar.BLOCK_REGISTRY.getKey(state.getBlock());
		String modId = loc.getResourceDomain();

		m_playerData.put(player,
				new OpenGuiFilterServer.RemoteGuiExtraData(id, tileUpdateTag, tileReadTag, blockPosition, modId, world.provider.getDimension()));

	}

    public void clearPlayerData(EntityPlayer player)
    {
    	m_playerData.put(player, null);
    }

    private void PrepareReissueRequest(OpenGuiWrapper msg, RemoteGuiExtraData data, EntityPlayerMP player)
    {
    	World world = DimensionManager.getWorld(data.dimensionId);

    	int x, y, z;

    	try {

	    	x = msg.getX();
	    	y = msg.getY();
	    	z = msg.getZ();

	    	int count = m_playerData.get(player).count;

	    	// find the real block
			this.setPlayerData(world, player, new BlockPos(x, y, z));

	    	RemoteGuiExtraData newdata = m_playerData.get(player);

	    	// set count > 0 to prevent recursion!
	    	newdata.count = count + 1;

	    	// remember the alamo!
	    	newdata.modGuiId = msg.getModGuiId();
	    	newdata.rerouted = true;


		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to set remote gui player data for re-try!", e);

			// this is bad... we can't recover from here... we have to just drop the open gui request :(
		}

    }

    public boolean wasRerouted(EntityPlayer player)
    {
    	RemoteGuiExtraData data = m_playerData.get(player);

    	if (data != null) return data.rerouted;
    	return false;
    }

    // must be called on SERVER minecraft thread!
    public void ReissueRequest(EntityPlayer player)
    {
    	// make sure the previous instance is ALL the way closed!
    	((EntityPlayerMP)player).closeContainer();

    	RemoteGuiExtraData data = m_playerData.get(player);

    	if (data != null && data.rerouted == true)
    	{

	    	World world = DimensionManager.getWorld(data.dimensionId);

	    	// activate!
	    	player.openGui(data.modId, data.modGuiId, world, data.x, data.y, data.z);

    	}
    	else
    	{
    		Util.logger.error("ReissueRequest attempted with no rerouted player data set!");
    	}

    }

    private void HandleDataMismatch(OpenGuiWrapper msg, RemoteGuiExtraData data, EntityPlayerMP player, ChannelHandlerContext ctx)
    {
		 // sometimes multiblocks trigger UI from another position!
		 // time to re-issue with the correct information

		 // however we could be on the networking thread!

    	IThreadListener scheduler = FMLCommonHandler.instance().getWorldThread(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());

        if (!scheduler.isCallingFromMinecraftThread())
        {
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
