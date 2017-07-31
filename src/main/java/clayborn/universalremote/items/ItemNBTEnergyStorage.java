package clayborn.universalremote.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;

public class ItemNBTEnergyStorage implements IEnergyStorage {

	protected ItemStack m_stack;
    protected int m_capacity;
    protected int m_maxReceive;
    protected int m_maxExtract;
	
    public ItemNBTEnergyStorage(ItemStack stack, int capacity)
    {
        this(stack, capacity, capacity, capacity);
    }

    public ItemNBTEnergyStorage(ItemStack stack, int capacity, int maxTransfer)
    {
        this(stack, capacity, maxTransfer, maxTransfer);
    }

    public ItemNBTEnergyStorage(ItemStack stack, int capacity, int maxReceive, int maxExtract)
    {
    	m_stack = stack;
    	
    	// Make sure we have NBT on the stack
        if(!m_stack.hasTagCompound()){
        	m_stack.setTagCompound(new NBTTagCompound());
        }
    	
    	m_capacity = capacity;
    	m_maxReceive = maxReceive;
    	m_maxExtract = maxExtract;
    }    

    @Override
    public int receiveEnergy(int receiveAmount, boolean simulate)
    {
        if (!canReceive())
            return 0;
        
        int realAmount = Math.min(m_maxReceive, receiveAmount);      
        
        return limitlessReceiveEnergy(realAmount, simulate);
    }
    
    public int limitlessReceiveEnergy(int receiveAmount, boolean simulate)
    {
    	// no negative numbers!
    	int realAmount = Math.max(0, receiveAmount);
        int energy = Math.max(this.getEnergyStored(), 0);
        
        int energyReceived = Math.min(m_capacity - energy, realAmount);
        
        if (!simulate)
        	this.setEnergyStored(energy + energyReceived);      
        
        return energyReceived;
    }

    @Override
    public int extractEnergy(int extractAmount, boolean simulate)
    {
        if (!canExtract())
            return 0;

        int realAmount = Math.min(m_maxExtract, extractAmount);         
        
        return limitlessExtractEnergy(realAmount, simulate);
    }
    
    public int limitlessExtractEnergy(int extractAmount, boolean simulate)
    {
    	// no negative numbers!
    	int realAmount = Math.max(0, extractAmount);
        int energy = Math.max(this.getEnergyStored(), 0);
        
        int energytoExtract = Math.min(energy, realAmount);
        
        if (!simulate)
        	this.setEnergyStored(energy - energytoExtract);      
        
        return energytoExtract;
    }
    
    @Override
    public int getEnergyStored()
    {
    	// This is safe because the default value is 0 for missing integer tags
    	return m_stack.getTagCompound().getInteger("energy");
    }
    
    public void setEnergyStored(int energy)
    {
    	// no negative numbers!
    	energy = Math.max(energy, 0);
    	m_stack.getTagCompound().setInteger("energy", energy);
    }

    @Override
    public int getMaxEnergyStored()
    {
        return m_capacity;
    }

    @Override
    public boolean canExtract()
    {
        return m_maxExtract > 0;
    }

    @Override
    public boolean canReceive()
    {
        return m_maxReceive > 0;
    }

}
