package clayborn.universalremote.hooks.events;

import clayborn.universalremote.hooks.world.HookedClientWorld;
import clayborn.universalremote.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// is this class name long enough yet?
public class HookedClientWorldEventSync {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiOpenEvent(GuiOpenEvent event)
	{
		if (event.getGui() == null)
		{
			// a gui has been closed -- reset remote gui

			if (Minecraft.getMinecraft().world instanceof HookedClientWorld)
			{
				((HookedClientWorld)Minecraft.getMinecraft().world).ClearRemoteGui();
			}
			else
			{
				Util.logger.error("Minecraft.getMinecraft().world is not instance of RemoteGuiEnabledClientWorld!");
			}
		}
	}

}
