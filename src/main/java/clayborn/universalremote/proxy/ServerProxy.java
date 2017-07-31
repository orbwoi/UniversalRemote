package clayborn.universalremote.proxy;

import clayborn.universalremote.util.Util;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy implements ISidedProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		Util.logger.debug("server preInit starting...");
    	
		Util.logger.debug("server preInit complete!");
	}

	@Override
	public void init(FMLInitializationEvent event) {
		Util.logger.debug("server init starting...");
    	
		Util.logger.debug("server init complete!");
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		Util.logger.debug("server postInit starting...");
		
		Util.logger.debug("server postInit complete!");
	}

}
