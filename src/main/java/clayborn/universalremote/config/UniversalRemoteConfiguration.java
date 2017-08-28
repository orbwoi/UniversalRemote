package clayborn.universalremote.config;

import clayborn.universalremote.items.ItemRegistry;
import clayborn.universalremote.settings.Strings;
import clayborn.universalremote.util.Util;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Strings.MODID)
@Config.LangKey("universalremote.strings.configtitle")
public class UniversalRemoteConfiguration {

	@Config.Comment("Configuration for fuel usage cost of universal remotes.")
	public static final Fuel fuel = new Fuel();

	@Config.Comment("Control which blocks can be accessed by universal remotes.")
	public static final Blacklist blacklist = new Blacklist();

	public static class Fuel
	{
		@Config.Comment("What type of fuel is used by remotes. Current acceptable values are [none, energy].\n" +
				        "If set to none, there will be no cost to use remotes. If set to energy, forge energy will be required.")
		public String fuelType = "energy";

		@Config.Comment("Configuration parameters for when fuel type of 'energy' is used")
		public FuelEnergy energy = new FuelEnergy();
	}

	public static class FuelEnergy {

		@Config.Comment("The total energy storage of the universal remotes.")
		public int energyCapacity = 100000;

		@Config.Comment("The amount of energy that can received per tick when charing a remote.")
		public int energyReceiveRate = 1000;

		@Config.Comment("The cost of same dimension remote access for each block between the player and the bound block.")
		public int energyCostPerBlock = 10;

		@Config.Comment("The highest allowed cost of a remote access in the same dimension. Also the amount charged for cross-dimensional access.")
		public int energyCostMax = 1000;

		@Config.Comment("The energy cost to bind a remote to a new block")
		public int energyCostBindBlock = 100;
	}

	public static class Blacklist
	{
		@Config.Comment("A comma delimited list of blocks which should not be allowed.\n" +
				        "Use '*' in place of the block in the resource string to blacklist a whole mod instead.\n" +
				        "Example: minecraft:chest,rftools:*")
		public String blacklist = "";
	}

	public static enum FuelType
	{
		None("none"),
		Energy("energy");

		private String m_value;
		private FuelType(String type)
		{
			m_value = type;
		}

		@Override
		public String toString()
		{
			return m_value;
		}
	}

	public static void validateConfig()
	{

		// make sure fuel type is valid
		boolean isValueFuelType = false;

		for (FuelType f: FuelType.values())
		{
			if (UniversalRemoteConfiguration.fuel.fuelType.equals(f.toString()))
			{
				isValueFuelType = true;
				break;
			}
		}

		if (!isValueFuelType)
		{
			Util.logger.error("Invalid fuel type of '{}' found in config. Reverting to default value.", UniversalRemoteConfiguration.fuel.fuelType);

			// find the default
			UniversalRemoteConfiguration.fuel.fuelType = new Fuel().fuelType;
		}

		// no negative values!

		if (UniversalRemoteConfiguration.fuel.energy.energyCapacity < 0)
			UniversalRemoteConfiguration.fuel.energy.energyCapacity = 0;

		if (UniversalRemoteConfiguration.fuel.energy.energyReceiveRate < 0)
			UniversalRemoteConfiguration.fuel.energy.energyReceiveRate = 0;

		if (UniversalRemoteConfiguration.fuel.energy.energyCostPerBlock < 0)
			UniversalRemoteConfiguration.fuel.energy.energyCostPerBlock = 0;

		if (UniversalRemoteConfiguration.fuel.energy.energyCostMax < 0)
			UniversalRemoteConfiguration.fuel.energy.energyCostMax = 0;

		if (UniversalRemoteConfiguration.fuel.energy.energyCostBindBlock < 0)
			UniversalRemoteConfiguration.fuel.energy.energyCostBindBlock = 0;

		// better re-sync
		ConfigManager.sync(Strings.MODID, Config.Type.INSTANCE);
	}

	public static boolean isBlockBlacklisted(Block block)
	{
		ResourceLocation loc = Block.REGISTRY.getNameForObject(block);
		String[] blackList = UniversalRemoteConfiguration.blacklist.blacklist.split(",");

		for (String entry : blackList)
		{
			String[] parts = entry.split(":");

			if (loc.toString().equals(entry))
			{
				return true;
			}

			// bad entry?
			if (parts.length != 2) continue;

			if (parts[1].equals("*") && loc.getResourceDomain().equals(parts[0]))
			{
				return true;
			}
		}

		return false;
	}

	@Mod.EventBusSubscriber(modid = Strings.MODID)
	private static class EventHandler {

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(Strings.MODID)) {

				// sync GUI to settings..
				ConfigManager.sync(Strings.MODID, Config.Type.INSTANCE);

				// this also syncs when done
				UniversalRemoteConfiguration.validateConfig();

				int newCapacity = UniversalRemoteConfiguration.fuel.energy.energyCapacity;

				// gotta set it to zero behind the scenes if energy isn't enabled
				if (!UniversalRemoteConfiguration.fuel.fuelType.equals(UniversalRemoteConfiguration.FuelType.Energy.toString()))
				{
					newCapacity = 0;
				}

				// well crap gotta update the registered item now
				ItemRegistry.Items().UniveralRemote.UpdateEnergySettings(
						newCapacity,
						UniversalRemoteConfiguration.fuel.energy.energyReceiveRate,
						0);
			}
		}
	}

}
