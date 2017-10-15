package clayborn.universalremote.hooks.world;

import clayborn.universalremote.util.Util;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;

public class HookedChunkProviderClient extends ChunkProviderClient {

	private String m_modPrefix;
	private int m_remoteChunkX;
	private int m_remoteChunkZ;

	// it's just a reference... no need to use reflect to get private copy in child
	final private World m_world;

	public HookedChunkProviderClient(World worldIn) {
		super(worldIn);
		m_world = worldIn;
	}

	public void SetRemoteGui(String modPrefix, int chunkX, int chunkZ)
	{
		m_remoteChunkX = chunkX;
		m_remoteChunkZ = chunkZ;
		m_modPrefix = modPrefix;
	}

	public void ClearRemoteGui()
	{
		m_modPrefix = null;
	}

	@Override
	public Chunk getLoadedChunk(int x, int z) {

		Chunk tryget = super.getLoadedChunk(x, z);

		if (tryget == null &&
			m_modPrefix != null &&
			x == m_remoteChunkX &&
			z == m_remoteChunkZ &&
			Util.isPrefixInCallStack(m_modPrefix))
		{
			// some mods use this to check if a chunk is loaded client side...
			// but they don't actually use the chunk so just give them *something*
			return new EmptyChunk(m_world, 0, 0);
		} else {
			return tryget;
		}
	}

}
