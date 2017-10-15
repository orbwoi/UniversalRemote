package clayborn.universalremote.hooks.client;

import clayborn.universalremote.hooks.world.HookedClientWorld;
import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.Session;

public class MinecraftProxy extends Minecraft {

	public static MinecraftProxy INSTANCE;

	protected Minecraft m_realMinecraft;

	public MinecraftProxy(Minecraft realMinecraft) throws IllegalAccessException {
		// random stuff to make base constructor work
		super(new GameConfiguration(
				new GameConfiguration.UserInformation(new Session("notarealuser",null,null,"mojang"), null, null, null),
				new GameConfiguration.DisplayInformation(0, 0, false, false),
				new GameConfiguration.FolderInformation(null, null, null, null),
				new GameConfiguration.GameInformation(false, null, null),
				new GameConfiguration.ServerInformation(null, 0)));

		// now fix INSTANCE
		InjectionHandler.writeStaticFieldOfType(Minecraft.class, realMinecraft, Minecraft.class);

		m_realMinecraft = realMinecraft;
	}

	@Override
	public void loadWorld(WorldClient worldClientIn) {

		WorldClient newWorld = worldClientIn;

		if (worldClientIn != null && !(worldClientIn instanceof HookedClientWorld))
		{
			// make a brand new world!
			try {

				newWorld = new HookedClientWorld(worldClientIn);

			} catch (IllegalAccessException e) {
				Util.logger.logException("Unable to create HookedClientWorld!", e);
			}

		}

		SyncToReal();

		m_realMinecraft.loadWorld(newWorld);

		SyncFromReal();

		// don't break mod's state machines
		if (newWorld != worldClientIn)
		{
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Unload(worldClientIn));
		}

	}

	public void SyncToReal()
	{
		// sync the player controller if needed
		if (this.playerController != null)
		{
			try {

				// Fix the player internal mc refernce
				InjectionHandler.writeFieldOfType(this.playerController, m_realMinecraft, Minecraft.class);

			} catch (IllegalAccessException e) {
				Util.logger.logException("Unable to set playerController.mc!", e);
			}
		}

		// sync the player if needed
		if (this.player != null)
		{
			try {

				// Fix the player internal mc refernce
				InjectionHandler.writeFieldOfType(this.player, m_realMinecraft, Minecraft.class);

			} catch (IllegalAccessException e) {
				Util.logger.logException("Unable to set player.mc!", e);
			}
		}

		InjectionHandler.copyAllFieldsFromEx(m_realMinecraft, this, Minecraft.class);

	}

	public void SyncFromReal()
	{
		InjectionHandler.copyAllFieldsFromEx(this, m_realMinecraft, Minecraft.class);
	}

}
