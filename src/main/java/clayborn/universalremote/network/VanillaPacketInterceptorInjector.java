package clayborn.universalremote.network;

import clayborn.universalremote.util.Util;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VanillaPacketInterceptorInjector {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientConnectedToServerEvent(ClientConnectedToServerEvent event)
	{
		Util.logger.info("Injecting vanilla packet interceptors...");

		event.getManager().channel().pipeline().addBefore("packet_handler", "universalremote_join_game_handler", new JoinGameInterceptor(event.getManager()));
		event.getManager().channel().pipeline().addBefore("packet_handler", "universalremote_respawn_handler", new RespawnInterceptor(event.getManager()));
	}

}
