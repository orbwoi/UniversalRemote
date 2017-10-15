package clayborn.universalremote.hooks.world;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;

public class HookedClientWorld extends WorldClient {

	// remote gui state
	private int m_x, m_y, m_z;
	private TileEntity m_tile;
	private IBlockState m_state;
	private String m_modPrefix;

	private void SetupWorldProviderProxy() throws IllegalAccessException
	{
		// did somebody else proxy the provider??
		if (!(this.provider instanceof WorldProviderProxyClient))
		{
				InjectionHandler.writeFieldOfType(
						this,
						new WorldProviderProxyClient(this.provider),
						WorldProvider.class);
		}
	}

	public HookedClientWorld(WorldClient originalWorld) throws IllegalAccessException {
		super(InjectionHandler.readFieldOfType(originalWorld, NetHandlerPlayClient.class),
				new WorldSettings(originalWorld.getWorldInfo()),
				originalWorld.provider.getDimension(), originalWorld.getDifficulty(), originalWorld.profiler);

		HookedChunkProviderClient chunkProvider = new HookedChunkProviderClient(this);

		// replace the chunk provider with our own!
		InjectionHandler.writeFieldOfType(this, chunkProvider, ChunkProviderClient.class);
		InjectionHandler.writeFieldOfType(this, chunkProvider, IChunkProvider.class);

		SetupWorldProviderProxy();

	}

	public HookedClientWorld(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension,
			EnumDifficulty difficulty, Profiler profilerIn) throws IllegalAccessException {
		super(netHandler, settings, dimension, difficulty, profilerIn);

		HookedChunkProviderClient chunkProvider = new HookedChunkProviderClient(this);

		// replace the chunk provider with our own!
		InjectionHandler.writeFieldOfType(this, chunkProvider, ChunkProviderClient.class);
		InjectionHandler.writeFieldOfType(this, chunkProvider, IChunkProvider.class);

		SetupWorldProviderProxy();

	}

	public void SetRemoteGui(IBlockState state, NBTTagCompound updateTag, NBTTagCompound readTag, int x, int y, int z, int dim)
	{
		m_x = x;
		m_y = y;
		m_z = z;

		m_state = state;
		m_tile = null;

		m_modPrefix = Util.getClassDomainFromName(state.getBlock().getClass().getName());

		// somebody else may have add their own proxy!
		try {
			SetupWorldProviderProxy();

	        ((WorldProviderProxyClient)this.provider).setData(dim, m_modPrefix);
		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to configure WorldProviderProxy!",e);
		}

		if (this.chunkProvider instanceof HookedChunkProviderClient)
		{
			((HookedChunkProviderClient)this.chunkProvider).SetRemoteGui (m_modPrefix, x >> 4, z >> 4);
		} else {
			Util.logger.error("Unable to set chunk provider's fake loaded chunk for client because ChunkProvider was not instance of RemoteEnabledChunkProviderClient! Instead was {}.", this.chunkProvider.getClass().toString());
		}

        if (state.getBlock().hasTileEntity(state)) {
        	m_tile = state.getBlock().createTileEntity(this, state);
        	if (m_tile != null)
        	{
	        	m_tile.setWorld(this);
	        	m_tile.setPos(new BlockPos(x,y,z));
	        	m_tile.handleUpdateTag(updateTag);
	        	m_tile.readFromNBT(readTag);
        	}
        }

	}

	public void ClearRemoteGui()
	{
		m_x = 0;
		m_y = 0;
		m_z = 0;

		m_state = null;
		m_tile = null;
		m_modPrefix = null;

		if (this.chunkProvider instanceof HookedChunkProviderClient)
		{
			((HookedChunkProviderClient)this.chunkProvider).ClearRemoteGui();
		} else {
			Util.logger.error("Unable to CLEAR chunk provider's fake loaded chunk for client because ChunkProvider was not instance of RemoteEnabledChunkProviderClient! Instead was {}.", this.chunkProvider.getClass().toString());
		}

		// somebody else may have add their own proxy!
		try {
			SetupWorldProviderProxy();

			((WorldProviderProxyClient)this.provider).clearData();
		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to configure WorldProviderProxy!",e);
		}

	}

	/* Modified Functions */

	@Override
	public TileEntity getTileEntity(BlockPos pos) {

		// if that fails AND it is the right block, send the remote one
		if ((!this.isBlockLoaded(pos, false) || ((WorldProviderProxyClient)this.provider).isDifferent())
			&& m_state != null && pos.getX() == m_x && pos.getY() == m_y && pos.getZ() == m_z
			&& Util.isPrefixInCallStack(m_modPrefix))
		{
			return m_tile;
		} else {
			return super.getTileEntity(pos);
		}
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {

		// if that fails AND it is the right block, send the remote one
		if ((!this.isBlockLoaded(pos, false) || ((WorldProviderProxyClient)this.provider).isDifferent())
				&& m_state != null && pos.getX() == m_x && pos.getY() == m_y && pos.getZ() == m_z
				&& Util.isPrefixInCallStack(m_modPrefix))
		{
			return m_state;
		} else {
			return super.getBlockState(pos);
		}

	}

}
