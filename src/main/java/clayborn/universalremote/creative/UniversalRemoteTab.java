package clayborn.universalremote.creative;

import clayborn.universalremote.items.ItemNBTEnergyStorage;
import clayborn.universalremote.items.ItemRegistry;
import clayborn.universalremote.util.CapabilityHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;

public class UniversalRemoteTab extends CreativeTabs {

	public static final UniversalRemoteTab INSTANCE = new UniversalRemoteTab("universalremotetab");

	public UniversalRemoteTab(String label) {
		super(label);
	}

	@Override
	public ItemStack getTabIconItem() {
		// surely there is a better way to do this...

		ItemStack stack = new ItemStack(ItemRegistry.Items().UniveralRemote, 1, 1);

        // fill up the energy for the photo
        ItemNBTEnergyStorage storage = (ItemNBTEnergyStorage) CapabilityHelper.tryGetCapability(stack, CapabilityEnergy.ENERGY, null);

        // energy might be disabled!
        if (storage != null)
        {
        	storage.setEnergyStored(storage.getMaxEnergyStored());
        }

		return stack;
	}

}
