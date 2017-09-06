package clayborn.universalremote.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import clayborn.universalremote.config.UniversalRemoteConfiguration;
import clayborn.universalremote.creative.UniversalRemoteTab;
import clayborn.universalremote.entity.EntityPlayerMPProxy;
import clayborn.universalremote.entity.EntityPlayerProxy;
import clayborn.universalremote.inventory.ContainerProxy;
import clayborn.universalremote.util.CapabilityHelper;
import clayborn.universalremote.util.TextFormatter;
import clayborn.universalremote.util.Util;
import clayborn.universalremote.world.PlayerRemoteGuiDataManagerServer;
import clayborn.universalremote.world.PlayerWorldSyncServer;
import clayborn.universalremote.world.WorldServerProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.energy.CapabilityEnergy;

public class ItemUniversalRemote extends ItemEnergyBase {

	public static class ItemUniversalRemoteNBTParser
	{

		protected NBTTagCompound m_tag;

		final static private NBTTagInt m_intTagType = new NBTTagInt(0);
		final static private NBTTagString m_stringTagType = new NBTTagString("");
		final static private NBTTagIntArray m_intArrayTagType = new NBTTagIntArray(new int[0]);
		final static private NBTTagFloat m_floatTagType = new NBTTagFloat(0F);
		final static private NBTTagDouble m_doubleTagType = new NBTTagDouble(0D);

		public ItemUniversalRemoteNBTParser(ItemStack stack)
		{
            if(!stack.hasTagCompound()){
            	m_tag = new NBTTagCompound();
            } else {
            	m_tag = stack.getTagCompound();
            }
		}

		public ItemUniversalRemoteNBTParser(NBTTagCompound tag)
		{
			m_tag = tag;
		}

		public void configureNBT(EntityPlayer player, BlockPos pos, World world, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
		{
            // save dimension info
			m_tag.setInteger("remote.dimension.id", player.dimension);

            String formattedDimName = Util.getNiceDimensionName(player.dimension);

            m_tag.setString("remote.dimension.name", formattedDimName);

            int[] blockPosition = {pos.getX(), pos.getY(), pos.getZ()};
            m_tag.setIntArray("remote.blockposition", blockPosition);

            m_tag.setString("remote.block.name", world.getBlockState(pos).getBlock().getClass().getName());

            m_tag.setString("remote.hand", hand.toString());
            m_tag.setString("remote.facing", facing.toString());

            m_tag.setFloat("remote.hit.X", hitX);
            m_tag.setFloat("remote.hit.Y", hitY);
            m_tag.setFloat("remote.hit.Z", hitZ);

            m_tag.setDouble("remote.player.position.X", player.posX);
            m_tag.setDouble("remote.player.position.Y", player.posY);
            m_tag.setDouble("remote.player.position.Z", player.posZ);

            m_tag.setFloat("remote.player.look.pitch", player.rotationPitch);
            m_tag.setFloat("remote.player.look.yaw", player.rotationYaw);
		}

		// May be needed later...
		public void clearNBT() {

			m_tag.removeTag("remote.dimension.id");
			m_tag.removeTag("remote.dimension.name");

			m_tag.removeTag("remote.block.name");

			m_tag.removeTag("remote.blockposition");

			m_tag.removeTag("remote.hand");
			m_tag.removeTag("remote.facing");

			m_tag.removeTag("remote.hit.X");
			m_tag.removeTag("remote.hit.Y");
			m_tag.removeTag("remote.hit.Z");

			m_tag.removeTag("remote.player.position.X");
			m_tag.removeTag("remote.player.position.Y");
			m_tag.removeTag("remote.player.position.Z");

		}

		public boolean validateNBT() {

			// NO crashing due to bad NBT!
			if (!m_tag.hasKey("remote.dimension.id", m_intTagType.getId())) return false;
			if (!m_tag.hasKey("remote.dimension.name", m_stringTagType.getId())) return false;

			if (!m_tag.hasKey("remote.block.name", m_stringTagType.getId())) return false;

			if (!m_tag.hasKey("remote.blockposition", m_intArrayTagType.getId())) return false;
			if (m_tag.getIntArray("remote.blockposition").length != 3) return false;

			if (!m_tag.hasKey("remote.hand", m_stringTagType.getId())) return false;
			if (!m_tag.hasKey("remote.facing", m_stringTagType.getId())) return false;

			if (!m_tag.hasKey("remote.hit.X", m_floatTagType.getId())) return false;
			if (!m_tag.hasKey("remote.hit.Y", m_floatTagType.getId())) return false;
			if (!m_tag.hasKey("remote.hit.Z", m_floatTagType.getId())) return false;

			if (!m_tag.hasKey("remote.player.position.X", m_doubleTagType.getId())) return false;
			if (!m_tag.hasKey("remote.player.position.Y", m_doubleTagType.getId())) return false;
			if (!m_tag.hasKey("remote.player.position.Z", m_doubleTagType.getId())) return false;

			if (!m_tag.hasKey("remote.player.look.pitch", m_floatTagType.getId())) return false;
			if (!m_tag.hasKey("remote.player.look.yaw", m_floatTagType.getId())) return false;

			return true;

		}

		public NBTTagCompound getTag()
		{
			return m_tag;
		}

		public int getDimensionId()
		{
			return m_tag.getInteger("remote.dimension.id");
		}

		public String getDimensionName()
		{
			return m_tag.getString("remote.dimension.name");
		}

		public String getBlockName()
		{
			return m_tag.getString("remote.block.name");
		}

		public BlockPos getBlockPosition()
		{
			int[] blockPositionArray = m_tag.getIntArray("remote.blockposition");
			return new BlockPos(blockPositionArray[0], blockPositionArray[1], blockPositionArray[2]);
		}

		public EnumHand getHand()
		{
			return EnumHand.valueOf(m_tag.getString("remote.hand"));
		}

		public EnumFacing getFacing()
		{
			return EnumFacing.byName(m_tag.getString("remote.facing"));
		}

		public float getHitX()
		{
			return m_tag.getFloat("remote.hit.X");
		}

		public float getHitY()
		{
			return m_tag.getFloat("remote.hit.Y");
		}

		public float getHitZ()
		{
			return m_tag.getFloat("remote.hit.Z");
		}

		public double getPlayerX()
		{
			return m_tag.getDouble("remote.player.position.X");
		}

		public double getPlayerY()
		{
			return m_tag.getDouble("remote.player.position.Y");
		}

		public double getPlayerZ()
		{
			return m_tag.getDouble("remote.player.position.Z");
		}

		public float getPlayerPitch()
		{
			return m_tag.getFloat("remote.player.look.pitch");
		}

		public float getPlayerYaw()
		{
			return m_tag.getFloat("remote.player.look.yaw");
		}
	}

	// these guys work only without the wrapper proxies!
	protected static final String[] m_containerProxyExceptionsList = { "com.raoulvdberge.refinedstorage", "appeng", "com.rwtema" };

	// can't handle the proxy player...
	protected static final String[] m_playerProxyDuringActivationExceptionsList = { "ic2" };

	// can't handle the proxy world...
	protected static final String[] m_worldProxyDuringActivationExceptionsList = { "ic2" };

	protected boolean m_publishSubTypes;

	public ItemUniversalRemote(String name, boolean publishSubTypes)
	{
		super(UniversalRemoteConfiguration.fuel.fuelType.equals(UniversalRemoteConfiguration.FuelType.Energy.toString()) ?
					UniversalRemoteConfiguration.fuel.energy.energyCapacity : 0,
			    UniversalRemoteConfiguration.fuel.energy.energyReceiveRate,
		        0, name, null);
		m_publishSubTypes = publishSubTypes;
		if (m_publishSubTypes) this.setHasSubtypes(true);
	}

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {

		if (m_publishSubTypes && (tab == UniversalRemoteTab.INSTANCE || tab == CreativeTabs.SEARCH))
		{

			// no power version
			{
				ItemStack stack = new ItemStack(ItemRegistry.Items().UniveralRemote);

				NBTTagCompound tag = null;

		        if(!stack.hasTagCompound()){
		        	tag = new NBTTagCompound();
		        } else {
		        	tag = stack.getTagCompound();
		        }

		        tag.setInteger("energy", 0);

		        stack.setTagCompound(tag);

		        items.add(stack);
			}

			// fully powered version
			if (UniversalRemoteConfiguration.fuel.fuelType.equals(UniversalRemoteConfiguration.FuelType.Energy.toString()) &&
	    			UniversalRemoteConfiguration.fuel.energy.energyCapacity > 0)
			{
				ItemStack stack = new ItemStack(ItemRegistry.Items().UniveralRemote);

				NBTTagCompound tag = null;

		        if(!stack.hasTagCompound()){
		        	tag = new NBTTagCompound();
		        } else {
		        	tag = stack.getTagCompound();
		        }

		        tag.setInteger("energy", UniversalRemoteConfiguration.fuel.energy.energyCapacity);

		        stack.setTagCompound(tag);

		        items.add(stack);
			}

		}

	}

    @Override
	public void register(ModelRegistryEvent event) {

		// register the models
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(this, 1, new ModelResourceLocation(this.getRegistryName() + "_bound", "inventory"));

	}

	/**
     * Called when a Block is right-clicked with this Item
     */
	@Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {

		// only works if a player is sneaking
        if (player.isSneaking())
        {
        	// server only
        	if (!worldIn.isRemote)
        	{

        		Block block = worldIn.getBlockState(pos).getBlock();

        		if (!UniversalRemoteConfiguration.isBlockBlacklisted(block))
        		{

		    		ItemStack stack = Util.playerAndHandToItemStack(player, hand);

		    		// Make sure we have enough energy

		    		ItemNBTEnergyStorage storage = (ItemNBTEnergyStorage) CapabilityHelper.tryGetCapability(stack, CapabilityEnergy.ENERGY, null);

		    		int amount = storage.limitlessExtractEnergy(UniversalRemoteConfiguration.fuel.energy.energyCostBindBlock, true);
		    		if (amount >= UniversalRemoteConfiguration.fuel.energy.energyCostBindBlock)
		    		{

		    			// extract the energy
		    			storage.limitlessExtractEnergy(UniversalRemoteConfiguration.fuel.energy.energyCostBindBlock, false);

		    			// Okay time to use NBT
			            ItemUniversalRemoteNBTParser myNBT = new ItemUniversalRemoteNBTParser(stack);

			            myNBT.configureNBT(player, pos, worldIn, hand, facing, hitX, hitY, hitZ);

			            // transform as needed
			            if (stack.getMetadata() != 1)
			            {
			            	stack = new ItemStack(ItemRegistry.Items().UniveralRemote, 1, 1);
			            	Util.setPlayerItemStackInHand(stack, player, hand);
			            }

			            // save the data!
			            stack.setTagCompound(myNBT.getTag());

			            // clear the player's remote gui if they used it on a block that didn't actually have a GUI
			            PlayerRemoteGuiDataManagerServer.INSTANCE.CancelRemoteActivation(player);

			            player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.bound", TextFormatting.DARK_GREEN));

		    		} else {
		    			// uh ho not enough power!
		    			player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.notenoughpower", TextFormatting.DARK_RED));
		    		}

        		} else {
        			// blacklisted!
        			player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.blockblacklist", TextFormatting.DARK_RED));
        		}

        	}

        	// keep client in sync with return value!
        	return EnumActionResult.SUCCESS;

        }

    	// default behavior
    	return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);

    }

    @Override
	public void addInformation(ItemStack stack, World playerIn, List<String> tooltip, ITooltipFlag advanced) {

		// this is a client side only method so we are safe doing client things!

    	String tip = null;

    	ItemUniversalRemoteNBTParser myNBT = new ItemUniversalRemoteNBTParser(stack);

    	if(myNBT.validateNBT())
    	{

    		// need to store the string!
    		String dimName = myNBT.getDimensionName();
    		BlockPos blockPosition = myNBT.getBlockPosition();

    		tip = TextFormatter.style(TextFormatting.GRAY, dimName + " (" + blockPosition.getX() + ", " + blockPosition.getY() + ", " + blockPosition.getZ() + ")").getFormattedText();
    	} else {
    		tip = TextFormatter.translateAndStyle(TextFormatting.DARK_RED, true, "universalremote.strings.unbound").getFormattedText();
    	}

    	tooltip.add(tip);

		super.addInformation(stack, playerIn, tooltip, advanced);

		if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			tooltip.add(TextFormatter.translateAndStyle(TextFormatting.DARK_GRAY, true, "universalremote.strings.showmore").getFormattedText());
		}
		else
		{
			tooltip.add(TextFormatter.translateAndStyle("universalremote.strings.instructionsOne", TextFormatting.GRAY).getFormattedText());
			tooltip.add(TextFormatter.translateAndStyle("universalremote.strings.instructionsTwo", TextFormatting.GRAY).getFormattedText());
		}
	}


    private boolean internalExtractEnergy(ItemStack stack, int amount)
    {
    	// check fuel type
    	if (UniversalRemoteConfiguration.fuel.fuelType.equals(UniversalRemoteConfiguration.FuelType.Energy.toString()) &&
    			UniversalRemoteConfiguration.fuel.energy.energyCapacity > 0)
		{

			ItemNBTEnergyStorage storage = (ItemNBTEnergyStorage) CapabilityHelper.tryGetCapability(stack, CapabilityEnergy.ENERGY, null);

			int extractableAmount = storage.limitlessExtractEnergy(amount, true);
			if (extractableAmount >= amount) {

				storage.limitlessExtractEnergy(amount, false);
				return true;
			}

			return false;

		} else {
			return true;
		}
    }

    private int computeEnergyCost(EntityPlayer player, int blockDim, BlockPos pos)
    {
    	// check fuel type
    	if (UniversalRemoteConfiguration.fuel.fuelType.equals(UniversalRemoteConfiguration.FuelType.Energy.toString()) &&
    			UniversalRemoteConfiguration.fuel.energy.energyCapacity > 0)
		{

			if (player.dimension == blockDim) {

				return Math.min(UniversalRemoteConfiguration.fuel.energy.energyCostMax, (int)(Math.sqrt(player.getDistanceSq(pos)) * UniversalRemoteConfiguration.fuel.energy.energyCostPerBlock));

			} else {

				return UniversalRemoteConfiguration.fuel.energy.energyCostMax;

			}

		} else {
			return 0;
		}
    }

    public static EntityPlayer ActivateBlock(EntityPlayer player, IBlockState state, ItemUniversalRemoteNBTParser myNBT, World world)
    {
    	EntityPlayer fakePlayer;

    	if (player instanceof EntityPlayerMP)
    	{

			fakePlayer = new EntityPlayerMPProxy(
				(EntityPlayerMP)player,
				myNBT.getPlayerX(),
				myNBT.getPlayerY(),
				myNBT.getPlayerZ(),
				myNBT.getPlayerPitch(),
				myNBT.getPlayerYaw());


    	} else {

			fakePlayer = new EntityPlayerProxy(
				player,
				myNBT.getPlayerX(),
				myNBT.getPlayerY(),
				myNBT.getPlayerZ(),
				myNBT.getPlayerPitch(),
				myNBT.getPlayerYaw());

    	}

		EntityPlayer activatePlayer = fakePlayer;

		// these guys can't handle fake players so lie about the TE instead!
		if (Util.doesStringStartWithAnyInArray(m_playerProxyDuringActivationExceptionsList, state.getClass().getName()))
		{
			activatePlayer = player;
		}

		Container oldContainer = player.openContainer;

		state.getBlock().
 			onBlockActivated(world, myNBT.getBlockPosition(), state, activatePlayer,
					myNBT.getHand(), myNBT.getFacing(), myNBT.getHitX(), myNBT.getHitY(), myNBT.getHitZ());

		// make sure any property sets are copied over to the real player
		if (activatePlayer != player && activatePlayer.openContainer != oldContainer  )
		{
	    	player.openContainer = activatePlayer.openContainer;
		}

		return activatePlayer;
    }

	/**
     * Called when the equipped item is right clicked.
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn)
    {

	    if (!worldIn.isRemote) {

	    	ItemStack stack = Util.playerAndHandToItemStack(player, handIn);

	    	ItemUniversalRemoteNBTParser myNBT = new ItemUniversalRemoteNBTParser(stack);

	    	// do we have bound block data?
			if (!myNBT.validateNBT())
			{
				// let the player know he needs data!

				player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.notbounderror", TextFormatting.DARK_RED));

				// default behavior
				return super.onItemRightClick(worldIn, player, handIn);
			}

            // transform as needed (this covers people who upgrade)
            if (stack.getMetadata() != 1)
            {
            	stack = new ItemStack(ItemRegistry.Items().UniveralRemote, 1, 1);
            	stack.setTagCompound(myNBT.getTag());
            	Util.setPlayerItemStackInHand(stack, player, handIn);
            }

			int energyCost = computeEnergyCost(player, myNBT.getDimensionId(), myNBT.getBlockPosition());

			WorldServer world = DimensionManager.getWorld(myNBT.getDimensionId());

			// this should be null only if the target dimension is not loaded
			if (world != null)
			{

				// The block needs to be in a loaded chunk
				if (world.isBlockLoaded(myNBT.getBlockPosition()))
				{

					IBlockState state = world.getBlockState(myNBT.getBlockPosition());

	        		if (!UniversalRemoteConfiguration.isBlockBlacklisted(state.getBlock()))
	        		{

						String test = state.getBlock().getClass().getName();

						if (test.equals(myNBT.getBlockName())) {

				    		// Make sure we have enough energy and if so extract it
				    		if (internalExtractEnergy(stack, energyCost)) {

				    			// ensure player is in world sync
				    			PlayerWorldSyncServer.INSTANCE.resyncIfNeeded(player);

				    			// container backup
								Container oldContainer = player.openContainer;

								// world backup
								WorldServer oldWorld = (WorldServer) player.world;

				    			// setup extra field need to setup client for remote modded gui activation!
				    			if (!test.startsWith("net.minecraft"))
				    			{
				    				// prepare for remote activation!
				    				PlayerRemoteGuiDataManagerServer.INSTANCE.PrepareForRemoteActivation(world, (EntityPlayerMP) player, myNBT.getBlockPosition(), new Vec3d(myNBT.getPlayerX(), myNBT.getPlayerY(), myNBT.getPlayerZ()));

				    				// Send the pre-activation trigger and config packet!
				    				PlayerRemoteGuiDataManagerServer.INSTANCE.SendPreparePacket(player, myNBT.getTag());

				    				// make sure player.GetEntityWorld returns the TE's world
									if (oldWorld != world)
									{

										if (!Util.doesStringStartWithAnyInArray(m_worldProxyDuringActivationExceptionsList, state.getClass().getName()))
										{
											player.world = new WorldServerProxy(oldWorld, world, test);
										} else {
											player.world = world;
										}

									}

				    			}

				    			EntityPlayer fakePlayer = ActivateBlock(player, state, myNBT, world);

								// did we get re-routed to another block?
								// then we need to try again!
								while (PlayerRemoteGuiDataManagerServer.INSTANCE.IsRetryNeeded(player))
								{
									Util.logger.info("Retrying OpenGui..");

									// note: count of tries kept in RemoteGuiPlayerData
									PlayerRemoteGuiDataManagerServer.INSTANCE.Retry(player);
								}

								// player opened a container, time to make a wrapper if needed
								if (player.openContainer != oldContainer)
								{

									if (!Util.doesStringStartWithAnyInArray(m_containerProxyExceptionsList, player.openContainer.getClass().getName()))
									{
										player.openContainer = new ContainerProxy(player.openContainer, fakePlayer);
									}

									// don't need this for vanilla
									if (!test.startsWith("net.minecraft") && oldWorld != world)
									{

										PlayerWorldSyncServer.INSTANCE.setPlayerData(player, player.openContainer,
												oldWorld.provider.getDimension(), world.provider.getDimension());

									}

								} else {

									// it didn't open anything, clear the player data
									PlayerRemoteGuiDataManagerServer.INSTANCE.CancelRemoteActivation(player);

									// put the world back since the player didn't open a container!
									player.world = oldWorld;

								}

				    		} else {

				    			// uh ho not enough power!
				    			player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.notenoughpower", TextFormatting.DARK_RED));

				    		}

						} else {

							// bad binding...
							player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.blockchanged", TextFormatting.DARK_RED));

						}

	        		} else {

	        			// blacklisted!
	        			player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.blockblacklist", TextFormatting.DARK_RED));

	        		}

				} else {
					// chunk isn't loaded!

					// let the player know the chunk isn't loaded
					player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.boundnotloaded", TextFormatting.DARK_RED));

				}


			} else {

				// let the player know the chunk (or dimension in this case) isn't loaded
				player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.boundnotloaded", TextFormatting.DARK_RED));

			}

    	}


		// client still says success so we stay in sync

		// we did it!
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));

    }

}
