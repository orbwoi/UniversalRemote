package clayborn.universalremote.inventory;

import java.util.List;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.world.PlayerWorldSyncServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ContainerProxy extends Container {

	Container m_realContainer;
	EntityPlayer m_playerProxy;

	public ContainerProxy(Container realContainer, EntityPlayer playerProxy)
	{
		super();

		m_realContainer = realContainer;
		m_playerProxy = playerProxy;

//	    this.inventoryItemStacks = realContainer.inventoryItemStacks;
//	    this.inventorySlots = realContainer.inventorySlots;
//		this.windowId = realContainer.windowId;

		InjectionHandler.copyAllFieldsFrom(this, realContainer, Container.class);

	}

	/* Modified Functions */

	// NOTE: the if m_realContainer == null in each function is to handle the case
	// where the super constructor calls this member function during object construction

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		if (m_realContainer == null) {
			return false;
		} else {
			return m_realContainer.canInteractWith(m_playerProxy);
		}
	}

	@Override
	public boolean getCanCraft(EntityPlayer player) {
		if (m_realContainer == null) {
			return super.getCanCraft(player);
		} else {
			return m_realContainer.getCanCraft(m_playerProxy);
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		if (m_realContainer == null) {
			super.onContainerClosed(playerIn);
		} else {

			// call the sync event!
			PlayerWorldSyncServer.INSTANCE.resyncIfNeeded(playerIn);

			// trigger default event
			m_realContainer.onContainerClosed(playerIn);
		}
	}

	/* Proxy Functions */

	@Override
	public void addListener(IContainerListener listener) {
		if (m_realContainer == null) {
			super.addListener(listener);
		} else {
			m_realContainer.addListener(listener);
		}
	}

	@Override
	public void removeListener(IContainerListener listener) {
		if (m_realContainer == null) {
			super.removeListener(listener);
		} else {
			m_realContainer.removeListener(listener);
		}
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		if (m_realContainer == null) {
			return super.getInventory();
		} else {
			return m_realContainer.getInventory();
		}
	}

	@Override
	public void detectAndSendChanges() {
		if (m_realContainer == null) {
			super.detectAndSendChanges();
		} else {
			m_realContainer.detectAndSendChanges();
		}
	}

	@Override
	public boolean enchantItem(EntityPlayer playerIn, int id) {
		if (m_realContainer == null) {
			return super.enchantItem(playerIn, id);
		} else {
			return m_realContainer.enchantItem(playerIn, id);
		}
	}

	@Override
	public Slot getSlotFromInventory(IInventory inv, int slotIn) {
		if (m_realContainer == null) {
			return super.getSlotFromInventory(inv, slotIn);
		} else {
			return m_realContainer.getSlotFromInventory(inv, slotIn);
		}
	}

	@Override
	public Slot getSlot(int slotId) {
		if (m_realContainer == null) {
			return super.getSlot(slotId);
		} else {
			return m_realContainer.getSlot(slotId);
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		if (m_realContainer == null) {
			return super.transferStackInSlot(playerIn, index);
		} else {
			return m_realContainer.transferStackInSlot(playerIn, index);
		}
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (m_realContainer == null) {
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		} else {
			return m_realContainer.slotClick(slotId, dragType, clickTypeIn, player);
		}
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		if (m_realContainer == null) {
			return super.canMergeSlot(stack, slotIn);
		} else {
			return m_realContainer.canMergeSlot(stack, slotIn);
		}
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		if (m_realContainer == null) {
			super.onCraftMatrixChanged(inventoryIn);
		} else {
			m_realContainer.onCraftMatrixChanged(inventoryIn);
		}
	}

	@Override
	public void putStackInSlot(int slotID, ItemStack stack) {
		if (m_realContainer == null) {
			super.putStackInSlot(slotID, stack);
		} else {
			m_realContainer.putStackInSlot(slotID, stack);
		}
	}

//	@Override
//	public void addItem(int slotIn, ItemStack stack) {
//		if (m_realContainer == null) {
//			super.addItem(slotIn, stack);
//		} else {
//			m_realContainer.addItem(slotIn, stack);
//		}
//	}

	@Override
	public void setAll(List<ItemStack> p_190896_1_) {
		if (m_realContainer == null) {
			super.setAll(p_190896_1_);
		} else {
			m_realContainer.setAll(p_190896_1_);
		}
	}

	@Override
	public void updateProgressBar(int id, int data) {
		if (m_realContainer == null) {
			super.updateProgressBar(id, data);
		} else {
			m_realContainer.updateProgressBar(id, data);
		}
	}

	@Override
	public short getNextTransactionID(InventoryPlayer invPlayer) {
		if (m_realContainer == null) {
			return super.getNextTransactionID(invPlayer);
		} else {
			return m_realContainer.getNextTransactionID(invPlayer);
		}
	}

	@Override
	public void setCanCraft(EntityPlayer player, boolean canCraft) {
		if (m_realContainer == null) {
			super.setCanCraft(player, canCraft);
		} else {
			m_realContainer.setCanCraft(player, canCraft);
		}
	}

	@Override
	public boolean canDragIntoSlot(Slot slotIn) {
		if (m_realContainer == null) {
			return super.canDragIntoSlot(slotIn);
		} else {
			return m_realContainer.canDragIntoSlot(slotIn);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (m_realContainer == null) {
			return super.equals(obj);
		} else {
			return m_realContainer.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		if (m_realContainer == null) {
			return super.hashCode();
		} else {
			return m_realContainer.hashCode();
		}
	}

	@Override
	public String toString() {
		if (m_realContainer == null) {
			return super.toString();
		} else {
			return m_realContainer.toString();
		}
	}

}
