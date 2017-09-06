package clayborn.universalremote.world;

import java.util.IdentityHashMap;
import java.util.Map;

import clayborn.universalremote.network.OpenGuiWrapper;
import clayborn.universalremote.network.RemoteGuiMessage;
import clayborn.universalremote.network.RemoteGuiNetworkManager;
import clayborn.universalremote.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class PlayerRemoteGuiDataManagerServer {

	public static PlayerRemoteGuiDataManagerServer INSTANCE = new PlayerRemoteGuiDataManagerServer();

	protected Map<EntityPlayer, RemoteGuiPlayerData> m_playerData = new IdentityHashMap<EntityPlayer, RemoteGuiPlayerData>();

	// Extra Data store
	public static class RemoteGuiPlayerData
	{
		// basic fields
		public int blockId;
		public NBTTagCompound updateTag;
		public NBTTagCompound readTag;
		public int x, y, z;
		public Vec3d playerPos;
		public String modId;
		public int dimensionId;

		// re-routing fields
		public int count = 0; // recursion detection
		public int modGuiId; // extra storage for re-routing
		public boolean rerouted = false;

		public RemoteGuiPlayerData() { }

		public RemoteGuiPlayerData(int iBlockId, NBTTagCompound iUpdateTag, NBTTagCompound iReadTag, BlockPos pos, String iModId, int iDimensionId, Vec3d iPlayerPos)
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
			dimensionId = iDimensionId;
			playerPos = iPlayerPos;
		}
	}

	public void setPlayerData(EntityPlayer player, RemoteGuiPlayerData data)
	{
		m_playerData.put(player, data);
	}

	public RemoteGuiPlayerData getPlayerData(EntityPlayer player)
	{
		return m_playerData.get(player);
	}

	public void PrepareForRemoteActivation(WorldServer world, EntityPlayerMP player, BlockPos blockPosition, Vec3d playerPos)
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

		String modId = Util.getBlockModId(state.getBlock());

		// make sure any old entries are properly cleaned up
		CancelRemoteActivation(player);

		m_playerData.put(player,
				new RemoteGuiPlayerData(id, tileUpdateTag, tileReadTag, blockPosition, modId, world.provider.getDimension(), playerPos));

	}

	public void SendPreparePacket(EntityPlayer player, NBTTagCompound remoteTag)
	{
		RemoteGuiPlayerData data = m_playerData.get(player);

		if (data != null) {

			// send prepare packet
			OpenGuiWrapper wrap;
			try {
				wrap = new OpenGuiWrapper(-1, data.modId, -1, data.x, data.y, data.z);
				RemoteGuiMessage msg = new RemoteGuiMessage(wrap, data.blockId, data.updateTag, data.readTag, remoteTag, player.world.provider.getDimension());
				RemoteGuiNetworkManager.INSTANCE.sendPacketToPlayer(msg, (EntityPlayerMP) player);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Util.logger.logException("Unable to send prepare OpenRemoteGuiMessage packet.", e);
			}

		} else {
			Util.logger.error("Could not send prepare back because RemoteGuiPlayerData was null!");
		}
	}

	public boolean IsRetryNeeded(EntityPlayer player)
	{
    	RemoteGuiPlayerData data = m_playerData.get(player);

    	if (data != null) return data.rerouted;
    	return false;
	}

	public void Retry(EntityPlayer player)
	{
    	// make sure the previous instance is ALL the way closed!
    	((EntityPlayerMP)player).closeContainer();

    	RemoteGuiPlayerData data = m_playerData.get(player);

    	if (data != null && data.rerouted == true)
    	{

	    	World world = DimensionManager.getWorld(data.dimensionId);

	    	Container oldContainer = player.openContainer;

	    	// activate!
	    	player.openGui(data.modId, data.modGuiId, world, data.x, data.y, data.z);

	    	// did anything get opened?
	    	if (player.openContainer == oldContainer)
	    	{
	    		Util.logger.warn("Re-try did not open a container!");

	    		// nothing opened, we need to abort!
	    		m_playerData.put(player, null);

	    	}

    	}
    	else
    	{
    		Util.logger.error("ReissueRequest attempted with no rerouted player data set!");
    	}
	}

	public void CancelRemoteActivation(EntityPlayer player)
	{
		if (m_playerData.containsKey(player)) {
	    	m_playerData.put(player, null);
		}
	}

}
