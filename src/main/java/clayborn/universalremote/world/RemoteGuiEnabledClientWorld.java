package clayborn.universalremote.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

public class RemoteGuiEnabledClientWorld extends WorldClient {

	// remote guid state
	private int m_x, m_y, m_z;	
	private TileEntity m_tile;
	private IBlockState m_state;
	private int m_origDim;
	
	public RemoteGuiEnabledClientWorld(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension,
			EnumDifficulty difficulty, Profiler profilerIn) {
		super(netHandler, settings, dimension, difficulty, profilerIn);
		
		m_origDim = dimension;
	}
	
	public void SetRemoteGui(IBlockState state, NBTTagCompound tag, int x, int y, int z, int dim)
	{
		m_x = x;
		m_y = y;
		m_z = z;
		
		m_state = state;
		m_tile = null;
		
        if (state.getBlock().hasTileEntity(state)) {
        	m_tile = state.getBlock().createTileEntity(this, state);
        	m_tile.setWorld(this);
        	m_tile.setPos(new BlockPos(x,y,z));
        	m_tile.handleUpdateTag(tag);
        }
        
        this.provider.setDimension(dim);
	}
	
	public void ClearRemoteGui()
	{
		m_x = 0;
		m_y = 0;
		m_z = 0;
		
		m_state = null;
		m_tile = null;
		
		this.provider.setDimension(m_origDim);
	}
	
	/* Modified Functions */	

	@Override
	public TileEntity getTileEntity(BlockPos pos) {	
		
		// if m_state is null, no remote gui is enabled
		if (m_state != null && pos.getX() == m_x && pos.getY() == m_y && pos.getZ() == m_z)
		{				
			return m_tile;				
		} else {			
			return super.getTileEntity(pos);
		}
	}
	
	@Override
	public IBlockState getBlockState(BlockPos pos) {

		// if m_state is null, no remote gui is enabled
		if (m_state != null && pos.getX() == m_x && pos.getY() == m_y && pos.getZ() == m_z)
		{				
			return m_state;				
		} else {			
			return super.getBlockState(pos);
		}

	}

}
