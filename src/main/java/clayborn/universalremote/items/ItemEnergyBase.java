package clayborn.universalremote.items;

import java.text.NumberFormat;
import java.util.List;

import clayborn.universalremote.util.CapabilityHelper;
import clayborn.universalremote.util.TextFormatter;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ItemEnergyBase extends ItemBase {

    public class EnergyCapabilityProvider implements ICapabilityProvider {

    	private final ItemNBTEnergyStorage m_storage;

    	public EnergyCapabilityProvider( ItemStack stack, int energyCapacity, int energyReceiveRate,  int energyExtractRate) {
    		m_storage = new ItemNBTEnergyStorage(stack, energyCapacity, energyReceiveRate, energyExtractRate);
    	}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			if (capability == CapabilityEnergy.ENERGY && m_storage.getMaxEnergyStored() > 0)
			{
				return true;
			}

			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityEnergy.ENERGY && m_storage.getMaxEnergyStored() > 0)
			{
				return (T)m_storage;
			}
			return null;
		}

    }

    private int m_energyCapacity,  m_energyReceiveRate, m_energyExtractRate;

    public ItemEnergyBase(int energyCapacity, int energyReceiveRate, int energyExtractRate, String name, CreativeTabs tab){
        super(name, tab);
        m_energyCapacity = energyCapacity;
        m_energyReceiveRate = energyReceiveRate;
        m_energyExtractRate = energyExtractRate;
        this.setMaxStackSize(1);
    }

    public void UpdateEnergySettings(int energyCapacity, int energyReceiveRate, int energyExtractRate)
    {
        m_energyCapacity = energyCapacity;
        m_energyReceiveRate = energyReceiveRate;
        m_energyExtractRate = energyExtractRate;
    }

    @Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new EnergyCapabilityProvider(stack, m_energyCapacity, m_energyReceiveRate, m_energyExtractRate);
	}

	@Override
    public boolean showDurabilityBar(ItemStack stack){

		// Don't show bar if we have no capacity
		if (m_energyCapacity == 0) return false;

		// Don't show the bar when we are full
		return getDurabilityForDisplay(stack) > 0.0F;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack){

    	IEnergyStorage storage = CapabilityHelper.tryGetCapability(stack, CapabilityEnergy.ENERGY, null);

        if(storage != null){
        	return 1D - ((double)storage.getEnergyStored()/(double)storage.getMaxEnergyStored());
        }

        return super.getDurabilityForDisplay(stack);
    }

    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> tooltip, ITooltipFlag advanced){

    	IEnergyStorage storage = CapabilityHelper.tryGetCapability(stack, CapabilityEnergy.ENERGY, null);

        if(storage != null && m_energyCapacity > 0){
        	// Thank to Ellpeck's Actually Addition for this formating code!
            NumberFormat format = NumberFormat.getInstance();
            tooltip.add(TextFormatter.translateAndStyle(TextFormatting.DARK_GREEN, "universalremote.strings.powertooltip",
            		format.format(storage.getEnergyStored()),
            		format.format(storage.getMaxEnergyStored())).getFormattedText());
        }
    }

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		// a nice dark green
		return MathHelper.hsvToRGB(1.0F / 3.0F, 0.5F, 0.25F);
	}

	@Override
	public boolean updateItemStackNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		return super.updateItemStackNBT(nbt);
	}

}
