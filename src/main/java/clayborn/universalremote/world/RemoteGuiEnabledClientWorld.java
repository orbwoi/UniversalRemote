package clayborn.universalremote.world;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;

public class RemoteGuiEnabledClientWorld extends WorldClient {

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

	public RemoteGuiEnabledClientWorld(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension,
			EnumDifficulty difficulty, Profiler profilerIn) throws IllegalAccessException {
		super(netHandler, settings, dimension, difficulty, profilerIn);

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

		// try to get the 'real' version
		TileEntity tryget = super.getTileEntity(pos);

		// if that fails AND it is the right block, send the remote one
		if ((tryget == null || ((WorldProviderProxyClient)this.provider).hasData())
				&& m_state != null && pos.getX() == m_x && pos.getY() == m_y && pos.getZ() == m_z
				&& Util.isPrefixInCallStack(m_modPrefix))
		{
			return m_tile;
		} else {
			return tryget;
		}
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {

		// try to get the 'real' version
		IBlockState tryget = super.getBlockState(pos);

		// if that fails AND it is the right block, send the remote one
		if ((tryget == null || ((WorldProviderProxyClient)this.provider).hasData())
				&& m_state != null && pos.getX() == m_x && pos.getY() == m_y && pos.getZ() == m_z
				&& Util.isPrefixInCallStack(m_modPrefix))
		{
			return m_state;
		} else {
			return tryget;
		}

	}

}
