package clayborn.universalremote.network;

import clayborn.universalremote.settings.Strings;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class UniversalRemotePacketHandler {
	
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Strings.MODID);
	
	// unique id generator
	private static int nextId = 0;
	
	public static int getNextId()
	{
		return nextId++;
	}
}
