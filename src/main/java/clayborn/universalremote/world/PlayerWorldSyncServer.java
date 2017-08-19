package clayborn.universalremote.world;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerWorldSyncServer {
	
	public static PlayerWorldSyncServer INSTANCE = new PlayerWorldSyncServer();
	
	private static class PlayerWorldResetData {
		public Object container;
		public int realDimension;
		public int fakeDimension;
		
		public PlayerWorldResetData (Object container, int realDimension, int fakeDimension)
		{
			this.realDimension = realDimension;
			this.fakeDimension = fakeDimension;
			this.container = container;
		}
	}
	
	private Map<EntityPlayer, PlayerWorldResetData> m_validContainers = new HashMap<EntityPlayer, PlayerWorldResetData>();
	
	public void setPlayerData(EntityPlayer player, Object container, int realDimension, int fakeDimension)
	{
		m_validContainers.put(player, new PlayerWorldResetData(container, realDimension, fakeDimension));
	}

	public void onWindowClose(EntityPlayer player)
	{
		if (!player.world.isRemote && 
				m_validContainers.containsKey(player))
		{
			if (player.world.provider.getDimension() == m_validContainers.get(player).fakeDimension)
			{
				player.world = DimensionManager.getWorld(m_validContainers.get(player).realDimension);
			}

			m_validContainers.remove(player);
		}
		
	}
	
	// Unfortunately this is the only way to be sure nothing leaks :(
	@SubscribeEvent
	public void onPlayerTickEvent(PlayerTickEvent event)
	{
		EntityPlayer player = event.player;
		
		if (event.side == Side.SERVER && event.phase == TickEvent.Phase.START &&
				m_validContainers.containsKey(player) && 
				player.openContainer != m_validContainers.get(player).container)
		{
			if (player.world.provider.getDimension() == m_validContainers.get(player).fakeDimension)
			{
				player.world = DimensionManager.getWorld(m_validContainers.get(player).realDimension);
			}
			
			m_validContainers.remove(player);
		}

	}
	
}
