package clayborn.universalremote;

import clayborn.universalremote.config.UniversalRemoteConfiguration;
import clayborn.universalremote.hooks.events.PlayerWorldSyncServer;
import clayborn.universalremote.hooks.network.OpenGuiWrapper;
import clayborn.universalremote.network.RemoteGuiNetworkManager;
import clayborn.universalremote.proxy.ISidedProxy;
import clayborn.universalremote.registrar.Registrar;
import clayborn.universalremote.settings.Strings;
import clayborn.universalremote.util.Logger;
import clayborn.universalremote.util.Util;
import clayborn.universalremote.version.UniversalRemoteVersionProvider;
import clayborn.universalremote.version.VersionTracker;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

@Mod(modid = Strings.MODID, version = Strings.VERSION)
public class UniversalRemote
{

	// Proxy
    @SidedProxy(clientSide = Strings.CLIENTPROXY, serverSide = Strings.SERVERPROXY)
    public static ISidedProxy proxy;

    // Here be initialization events!
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	Util.logger = new Logger(event.getModLog());

    	// find the fields we need regardless of obs
    	OpenGuiWrapper.findFields();

    	// register event handlers
    	MinecraftForge.EVENT_BUS.register(new Registrar());
    	MinecraftForge.EVENT_BUS.register(new VersionTracker());
    	MinecraftForge.EVENT_BUS.register(PlayerWorldSyncServer.INSTANCE);

		// setup networking
    	RemoteGuiNetworkManager.INSTANCE.preInit(event);

    	VersionTracker.register(new UniversalRemoteVersionProvider());

    	proxy.preInit(event);

    	// fix broken config issues
    	UniversalRemoteConfiguration.validateConfig();
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
		Util.logger.info("Starting initalization...");

		// setup networking
		RemoteGuiNetworkManager.INSTANCE.init(event);

    	// get version data from the net
    	VersionTracker.downloadVersions();

    	proxy.init(event);

    	Util.logger.info("Initalization complete!");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
    	proxy.postInit(event);
    }

    @EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
    	proxy.serverAboutToStart(event);
	}

}
