package clayborn.universalremote.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import clayborn.universalremote.creative.CreativeTab;
import clayborn.universalremote.entity.EntityPlayerProxy;
import clayborn.universalremote.inventory.ContainerProxy;
import clayborn.universalremote.util.CapabilityHelper;
import clayborn.universalremote.util.TextFormatter;
import clayborn.universalremote.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.energy.CapabilityEnergy;

public class ItemUniversalRemote extends ItemEnergyBase {
	
	public static final int energyCapacity = 100000;
	public static final int energyReceiveRate = 1000;
	public static final int energyCostPerBlock = 1;
	public static final int energyCostAcrossDims = 1000;
	public static final int energyCostBindBlock = 100;
	
	public ItemUniversalRemote()
	{
		super(energyCapacity, energyReceiveRate, 0, "item_universal_remote", CreativeTab.INSTANCE);		
		this.setHasSubtypes(true);		
	}
	
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		
		// no power version
		{
			ItemStack stack = new ItemStack(ItemRegistry.Items().UniveralRemote);			
	        
	        items.add(stack);
		}
		
		// fully powered version
		{
			ItemStack stack = new ItemStack(ItemRegistry.Items().UniveralRemote);
			
			NBTTagCompound tag = null;
			
	        if(!stack.hasTagCompound()){
	        	tag = new NBTTagCompound();
	        } else {
	        	tag = stack.getTagCompound();
	        }
	        
	        tag.setInteger("energy", ItemUniversalRemote.energyCapacity);
	        
	        stack.setTagCompound(tag);
	        
	        items.add(stack);
		}
		
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
        		
	    		ItemStack stack = Util.playerAndHandToItemStack(player, hand);
	    		
	    		// Make sure we have enough energy
	    		
	    		ItemNBTEnergyStorage storage = (ItemNBTEnergyStorage) CapabilityHelper.tryGetCapability(stack, CapabilityEnergy.ENERGY, null);
	    		
	    		int amount = storage.limitlessExtractEnergy(energyCostBindBlock, true);
	    		if (amount >= energyCostBindBlock) {
	    			storage.limitlessExtractEnergy(energyCostBindBlock, false);
	    			
	    			
	    			// Okay time to use NBT
		    		
		    		NBTTagCompound tag = null;
		    		
		            if(!stack.hasTagCompound()){
		            	tag = new NBTTagCompound();
		            } else {
		            	tag = stack.getTagCompound();
		            }
		                        
		            // save dimension info
		            tag.setInteger("remote.dimension.id", player.dimension);
		            
		            // try to get a nice dimension name
		    		String dimName = DimensionManager.getProvider(player.dimension).getDimensionType().getName().
		    				replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2").replace("_", " ").trim();
		    		
		    		// TODO clean up this formatting mess
		    		String[] dimNameWords = dimName.split(" ");
		
		    		for(int i = 0; i < dimNameWords.length; i++)
		    		{
		    			if (dimNameWords[i].length() > 1)
		    			{
		    				dimNameWords[i] = dimNameWords[i].substring(0, 1).toUpperCase() + dimNameWords[i].substring(1);
		    			}    			
		    		}
		    		
		    		String formattedDimName = String.join(" ", dimNameWords);
		    		
		            tag.setString("remote.dimension.name", formattedDimName);
		            
		            int[] blockPosition = {pos.getX(), pos.getY(), pos.getZ()};            
		            tag.setIntArray("remote.blockposition", blockPosition);
		            
		            tag.setString("remote.hand", hand.toString());            
		            tag.setString("remote.facing", facing.toString());
		            
		            tag.setFloat("remote.hit.X", hitX);
		            tag.setFloat("remote.hit.Y", hitY);
		            tag.setFloat("remote.hit.Z", hitZ);
		            
		            tag.setDouble("remote.player.position.X", player.posX);
		            tag.setDouble("remote.player.position.Y", player.posY);
		            tag.setDouble("remote.player.position.Z", player.posZ);
		            
		            // save the data!
		            stack.setTagCompound(tag);
		            
		            player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.bound", TextFormatting.DARK_GREEN));
	    			
	    		} else {
	    			// uh ho not enough power!
	    			player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.notenoughpower", TextFormatting.DARK_RED));
	    		}	    		
	        	
        	}
        	
        	// keep client in sync with return value!
        	return EnumActionResult.SUCCESS;
        	
        }

    	// default behavior
    	return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);        
    	
    }	

	final static NBTTagInt m_intTagType = new NBTTagInt(0);
	final static NBTTagString m_stringTagType = new NBTTagString("");
	final static NBTTagIntArray m_intArrayTagType = new NBTTagIntArray(new int[0]);
	final static NBTTagFloat m_floatTagType = new NBTTagFloat(0F);
	final static NBTTagDouble m_doubleTagType = new NBTTagDouble(0D);
	
	private boolean validateNBT(ItemStack stack) {
		
		if (!stack.hasTagCompound()) return false;
		
		NBTTagCompound tag = stack.getTagCompound();		
		
		// NO crashing due to bad NBT!
		if (!tag.hasKey("remote.dimension.id", m_intTagType.getId())) return false;
		if (!tag.hasKey("remote.dimension.name", m_stringTagType.getId())) return false;
		
		if (!tag.hasKey("remote.blockposition", m_intArrayTagType.getId())) return false;		
		if (tag.getIntArray("remote.blockposition").length != 3) return false;
		
		if (!tag.hasKey("remote.hand", m_stringTagType.getId())) return false;
		if (!tag.hasKey("remote.facing", m_stringTagType.getId())) return false;
		
		if (!tag.hasKey("remote.hit.X", m_floatTagType.getId())) return false;
		if (!tag.hasKey("remote.hit.Y", m_floatTagType.getId())) return false;
		if (!tag.hasKey("remote.hit.Z", m_floatTagType.getId())) return false;
		
		if (!tag.hasKey("remote.player.position.X", m_doubleTagType.getId())) return false;
		if (!tag.hasKey("remote.player.position.Y", m_doubleTagType.getId())) return false;
		if (!tag.hasKey("remote.player.position.Z", m_doubleTagType.getId())) return false;
		
		return true;
		
	}

	// just in case we need thi later...
//	private void clearNBT(ItemStack stack) {
//		
//		if (!stack.hasTagCompound()) return;
//		
//		NBTTagCompound tag = stack.getTagCompound();
//		
//		tag.removeTag("remote.dimension.id");
//		tag.removeTag("remote.dimension.name");
//		
//		tag.removeTag("remote.blockposition");
//		
//		tag.removeTag("remote.hand");
//		tag.removeTag("remote.facing");
//				
//		tag.removeTag("remote.hit.X");
//		tag.removeTag("remote.hit.Y");
//		tag.removeTag("remote.hit.Z");
//		
//		tag.removeTag("remote.player.position.X");
//		tag.removeTag("remote.player.position.Y");
//		tag.removeTag("remote.player.position.Z");
//		
//		stack.setTagCompound(tag);
//		
//	}

    @Override
	public void addInformation(ItemStack stack, World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    	
		// this is a client side only method so we are safe doing client things!
    	
    	String tip = null;
    	
    	if(validateNBT(stack))    		
    	{
    		
    		NBTTagCompound tag = stack.getTagCompound();
    		
    		// need to store the string!
    		String dimName = tag.getString("remote.dimension.name");
    		int[] blockPosition = tag.getIntArray("remote.blockposition");
    		
    		tip = TextFormatter.style(dimName + " (" + blockPosition[0] + ", " + blockPosition[1] + ", " + blockPosition[2] + ")", TextFormatting.GRAY).getFormattedText();
    	} else {
    		tip = TextFormatter.translateAndStyle("universalremote.strings.unbound", TextFormatting.DARK_RED, true).getFormattedText();
    	}
    	
    	tooltip.add(tip);
    	
		super.addInformation(stack, playerIn, tooltip, advanced);
		
		if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			tooltip.add(TextFormatter.translateAndStyle("universalremote.strings.showmore", TextFormatting.DARK_GRAY, true).getFormattedText());
		}
		else
		{
			tooltip.add(TextFormatter.translateAndStyle("universalremote.strings.instructionsOne", TextFormatting.GRAY).getFormattedText());
			tooltip.add(TextFormatter.translateAndStyle("universalremote.strings.instructionsTwo", TextFormatting.GRAY).getFormattedText());
		}
	}

	/**
     * Called when the equipped item is right clicked.
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn)
    {
    	if (!worldIn.isRemote) {
    	
    		int energyCost = 0;
    		
	    	ItemStack stack = Util.playerAndHandToItemStack(player, handIn);
	    	    
	    	// do we have bound block data?
			if (!validateNBT(stack))
			{
				// let the player know he needs data!
									
				player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.notbounderror", TextFormatting.DARK_RED));
				
				// default behavior
				return super.onItemRightClick(worldIn, player, handIn);
			}
			
			NBTTagCompound tag = stack.getTagCompound();					
			
			// unpack NBT
			int dim = tag.getInteger("remote.dimension.id");
			int[] blockPositionArray = tag.getIntArray("remote.blockposition");
			BlockPos blockPosition = new BlockPos(blockPositionArray[0], blockPositionArray[1], blockPositionArray[2]);
			EnumHand hand = EnumHand.valueOf(tag.getString("remote.hand"));
			EnumFacing facing = EnumFacing.byName(tag.getString("remote.facing"));
			
			float hitX = tag.getFloat("remote.hit.X");
			float hitY = tag.getFloat("remote.hit.Y");
			float hitZ = tag.getFloat("remote.hit.Z");
	
			double posX = tag.getFloat("remote.player.position.X");
			double posY = tag.getFloat("remote.player.position.Y");
			double posZ = tag.getFloat("remote.player.position.Z");
			
			World world = null;
					
			if (player.dimension == dim) {	
				
				world = worldIn;
				energyCost = Math.min(energyCostAcrossDims, (int)(player.getDistanceSq(blockPosition) * energyCostPerBlock));
				
			} else {
				
				world = DimensionManager.getWorld(dim);	
				energyCost = energyCostAcrossDims;
				
			}
			
			// this should be null only if we are on the client AND in a different dimension 
			if (world != null)
			{
			
				if (world.getChunkFromBlockCoords(blockPosition).isLoaded())
				{
					
		    		// Make sure we have enough energy
		    		
		    		ItemNBTEnergyStorage storage = (ItemNBTEnergyStorage) CapabilityHelper.tryGetCapability(stack, CapabilityEnergy.ENERGY, null);
		    		
		    		int amount = storage.limitlessExtractEnergy(energyCost, true);
		    		if (amount >= energyCost) {
		    			storage.limitlessExtractEnergy(energyCost, false);
		    			
						IBlockState state = world.getBlockState(blockPosition);				
						
						Container oldContainer = player.openContainer;
						
						state.getBlock().
							onBlockActivated(world, blockPosition, state, player, hand, facing, hitX, hitY, hitZ);
						
						// we opened a container, time to make a wrapper
						if (player.openContainer != oldContainer && !player.openContainer.getClass().getName().startsWith("com.raoulvdberge.refinedstorage"))
						{
							player.openContainer = new ContainerProxy(player.openContainer, new EntityPlayerProxy(player, posX, posY, posZ));
						}
		    			
		    		} else {
		    			// uh ho not enough power!
		    			player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.notenoughpower", TextFormatting.DARK_RED));
		    		}
								
				
				} else {
					// chunk isn't loaded!
					
					// let the player know the chunk isn't loaded
					player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.boundnotloaded", TextFormatting.DARK_RED));
		
					
				}			
	
				
			} else {
				
				// if this happens the whole dimension isn't loaded!
				
				// let the player know the dimension isn't loaded
				player.sendMessage(TextFormatter.translateAndStyle("universalremote.strings.boundnotloaded", TextFormatting.DARK_RED));
				
			}
		
    	}
		
		
		// client still says success so stay in sync
		
		// we did it!
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));   	
    	
    }
	
}
