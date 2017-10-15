package clayborn.universalremote.hooks.events;

import java.util.IdentityHashMap;
import java.util.Map;

import clayborn.universalremote.hooks.entity.HookedEntityPlayerMP;
import clayborn.universalremote.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerWorldSyncServer {

	public static PlayerWorldSyncServer INSTANCE = new PlayerWorldSyncServer();

	private static class PlayerWorldResetData {
		public Container container;
//		public RemoteEnabledEntityPlayerMP player;
//		public int realDimension;
//		public int fakeDimension;

		public PlayerWorldResetData (Container container)
		{
//			this.realDimension = realDimension;
//			this.fakeDimension = fakeDimension;
			this.container = container;
		}
	}

	private Map<EntityPlayer, PlayerWorldResetData> m_validContainers = new IdentityHashMap<EntityPlayer, PlayerWorldResetData>();

	public void setPlayerData(EntityPlayer player, Container container)
	{
		m_validContainers.put(player, new PlayerWorldResetData(container));
	}

	public void resyncIfNeeded(EntityPlayer player)
	{
		if (!player.world.isRemote &&
				m_validContainers.containsKey(player))
		{

			if (player instanceof HookedEntityPlayerMP)
			{
				((HookedEntityPlayerMP)player).ClearRemoteFilter();
			} else {
				// uh ho..
				Util.logger.error("Could not resync player because player is not an instance of RemoteEnabledEntityPlayerMP!");
			}

//			if (player.world.provider.getDimension() == m_validContainers.get(player).fakeDimension)
//			{
//				player.world = DimensionManager.getWorld(m_validContainers.get(player).realDimension);
//			}

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

			if (player instanceof HookedEntityPlayerMP)
			{
				((HookedEntityPlayerMP)player).ClearRemoteFilter();
			} else {
				// uh ho..
				Util.logger.error("Could not resync player because player is not an instance of RemoteEnabledEntityPlayerMP!");
			}

//			if (player.world.provider.getDimension() == m_validContainers.get(player).fakeDimension)
//			{
//				player.world = DimensionManager.getWorld(m_validContainers.get(player).realDimension);
//			}

			m_validContainers.remove(player);
		}

	}

}
