package clayborn.universalremote.hooks.server;

import clayborn.universalremote.hooks.server.dedicated.HookedDedicatedPlayerList;
import clayborn.universalremote.hooks.server.integrated.HookedIntegratedPlayerList;
import clayborn.universalremote.util.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.integrated.IntegratedPlayerList;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ServerInjector {

	@SideOnly(Side.CLIENT)
	public static void InjectIntegrated(MinecraftServer server)
	{
		PlayerList playerList = server.getPlayerList();

		try {
			if (!(playerList instanceof HookedIntegratedPlayerList) && playerList instanceof IntegratedPlayerList)
			{
				server.setPlayerList(new HookedIntegratedPlayerList((IntegratedPlayerList)playerList));
			}
			else
			{
				// uh ho...
				Util.logger.error("Unable to inject custom PlayerList into server due to unknown type! PlayerList was of type {}.", playerList.getClass().toString());
			}
		} catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
			Util.logger.logException("Exception trying to inject custom PlayerList into server!", e);
		}
	}

	@SideOnly(Side.SERVER)
	public static void InjectDedicated(MinecraftServer server)
	{
		PlayerList playerList = server.getPlayerList();

		try {
			if (playerList instanceof DedicatedPlayerList)
			{
				server.setPlayerList(new HookedDedicatedPlayerList((DedicatedPlayerList)playerList));
			}
			else
			{
				// uh ho...
				Util.logger.error("Unable to inject custom PlayerList into server due to unknown type! PlayerList was of type {}.", playerList.getClass().toString());
			}
		} catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
			Util.logger.logException("Exception trying to inject custom PlayerList into server!", e);
		}
	}



}
