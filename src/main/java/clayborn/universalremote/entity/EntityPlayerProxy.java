package clayborn.universalremote.entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import clayborn.universalremote.util.InjectionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.FoodStats;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class EntityPlayerProxy extends EntityPlayer {

	private EntityPlayer m_realPlayer;
	
	public EntityPlayerProxy(EntityPlayer realPlayer, double posX, double posY, double posZ)
	{
		super(realPlayer.world, realPlayer.getGameProfile());		
		
//		this.inventory = realPlayer.inventory;
//		this.inventoryContainer = realPlayer.inventoryContainer;
		
		InjectionHandler.copyAllFieldsFrom(this, realPlayer, EntityPlayer.class);
		
		this.setPosition(posX, posY, posZ);
		
		m_realPlayer = realPlayer;
	}
	
	/* Modified Functions */	
	
	@Override
	public double getDistanceSq(double x, double y, double z) {
		return super.getDistanceSq(x, y, z);
	}

	@Override
	public double getDistanceSq(BlockPos pos) {
		return super.getDistanceSq(pos);
	}

	@Override
	public double getDistanceSqToCenter(BlockPos pos) {
		return super.getDistanceSqToCenter(pos);
	}

	@Override
	public double getDistance(double x, double y, double z) {
		return super.getDistance(x, y, z);
	}
	
	/* Proxy Functions */
	
	// NOTE: the if m_realPlayer == null in each function is to handle the case
	// where the super constructor calls this member function during object construction
	
	@Override
	public double getDistanceSqToEntity(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.getDistanceSqToEntity(entityIn);
		} else {
			return m_realPlayer.getDistanceSqToEntity(entityIn);
		}
	}
	
	@Override
	public float getDistanceToEntity(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.getDistanceToEntity(entityIn);
		} else {
			return m_realPlayer.getDistanceToEntity(entityIn);
		}
	}
	
	@Override
	public void onUpdate() {
		if (m_realPlayer == null) {
				super.onUpdate();
		} else {
			m_realPlayer.onUpdate();
		}
	}

	@Override
	public int getMaxInPortalTime() {
		if (m_realPlayer == null) {
			return super.getMaxInPortalTime();
		} else {
			return m_realPlayer.getMaxInPortalTime();
		}
	}

	@Override
	public int getPortalCooldown() {
		if (m_realPlayer == null) {
			return super.getPortalCooldown();
		} else {
			return m_realPlayer.getPortalCooldown();
		}
	}

	@Override
	public void playSound(SoundEvent soundIn, float volume, float pitch) {
		if (m_realPlayer == null) {
			super.playSound(soundIn, volume, pitch);
		} else {
			m_realPlayer.playSound(soundIn, volume, pitch);
		}
	}

	@Override
	public SoundCategory getSoundCategory() {
		if (m_realPlayer == null) {
			return super.getSoundCategory();
		} else {
			return m_realPlayer.getSoundCategory();
		}
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (m_realPlayer == null) {
			super.handleStatusUpdate(id);
		} else {
			m_realPlayer.handleStatusUpdate(id);
		}
	}

	@Override
	public void closeScreen() {
		if (m_realPlayer == null) {
			super.closeScreen();
		} else {
			m_realPlayer.closeScreen();
		}
	}

	@Override
	public void updateRidden() {
		if (m_realPlayer == null) {
			super.updateRidden();
		} else {
			m_realPlayer.updateRidden();
		}
	}

	@Override
	public void preparePlayerToSpawn() {
		if (m_realPlayer == null) {
			super.preparePlayerToSpawn();
		} else {
			m_realPlayer.preparePlayerToSpawn();
		}
	}

	@Override
	public void onLivingUpdate() {
		if (m_realPlayer == null) {
			super.onLivingUpdate();
		} else {
			m_realPlayer.onLivingUpdate();
		}
	}

	@Override
	public int getScore() {
		if (m_realPlayer == null) {
			return super.getScore();
		} else {
			return m_realPlayer.getScore();
		}
	}

	@Override
	public void setScore(int scoreIn) {
		if (m_realPlayer == null) {
			super.setScore(scoreIn);
		} else {
			m_realPlayer.setScore(scoreIn);
		}
	}

	@Override
	public void addScore(int scoreIn) {
		if (m_realPlayer == null) {
			super.addScore(scoreIn);
		} else {
			m_realPlayer.addScore(scoreIn);
		}
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (m_realPlayer == null) {
			super.onDeath(cause);
		} else {
			m_realPlayer.onDeath(cause);
		}
	}

	@Override
	public EntityItem dropItem(boolean dropAll) {
		if (m_realPlayer == null) {
			return super.dropItem(dropAll);
		} else {
			return m_realPlayer.dropItem(dropAll);
		}
	}

	@Override
	public EntityItem dropItem(ItemStack itemStackIn, boolean unused) {
		if (m_realPlayer == null) {
			return super.dropItem(itemStackIn, unused);
		} else {
			return m_realPlayer.dropItem(itemStackIn, unused);
		}
	}

	@Override
	public EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
		if (m_realPlayer == null) {
			return super.dropItem(droppedItem, dropAround, traceItem);
		} else {
			return m_realPlayer.dropItem(droppedItem, dropAround, traceItem);
		}
	}

	@Override
	public ItemStack dropItemAndGetStack(EntityItem p_184816_1_) {
		if (m_realPlayer == null) {
			return super.dropItemAndGetStack(p_184816_1_);
		} else {
			return m_realPlayer.dropItemAndGetStack(p_184816_1_);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public float getDigSpeed(IBlockState state) {
		if (m_realPlayer == null) {
			return super.getDigSpeed(state);
		} else {
			return m_realPlayer.getDigSpeed(state);
		}
	}

	@Override
	public float getDigSpeed(IBlockState state, BlockPos pos) {
		if (m_realPlayer == null) {
			return super.getDigSpeed(state, pos);
		} else {
			return m_realPlayer.getDigSpeed(state, pos);
		}
	}

	@Override
	public boolean canHarvestBlock(IBlockState state) {
		if (m_realPlayer == null) {
			return super.canHarvestBlock(state);
		} else {
			return m_realPlayer.canHarvestBlock(state);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			super.readEntityFromNBT(compound);
		} else {
			m_realPlayer.readEntityFromNBT(compound);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			super.writeEntityToNBT(compound);
		} else {
			m_realPlayer.writeEntityToNBT(compound);
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (m_realPlayer == null) {
			return super.attackEntityFrom(source, amount);
		} else {
			return m_realPlayer.attackEntityFrom(source, amount);
		}
	}

	@Override
	public boolean canAttackPlayer(EntityPlayer other) {
		if (m_realPlayer == null) {
			return super.canAttackPlayer(other);
		} else {
			return m_realPlayer.canAttackPlayer(other);
		}
	}

	@Override
	public float getArmorVisibility() {
		if (m_realPlayer == null) {
			return super.getArmorVisibility();
		} else {
			return m_realPlayer.getArmorVisibility();
		}
	}

	@Override
	public void openEditSign(TileEntitySign signTile) {
		if (m_realPlayer == null) {
			super.openEditSign(signTile);
		} else {
			m_realPlayer.openEditSign(signTile);
		}
	}

	@Override
	public void displayGuiEditCommandCart(CommandBlockBaseLogic commandBlock) {
		if (m_realPlayer == null) {
			super.displayGuiEditCommandCart(commandBlock);
		} else {
			m_realPlayer.displayGuiEditCommandCart(commandBlock);
		}
	}

	@Override
	public void displayGuiCommandBlock(TileEntityCommandBlock commandBlock) {
		if (m_realPlayer == null) {
			super.displayGuiCommandBlock(commandBlock);
		} else {
			m_realPlayer.displayGuiCommandBlock(commandBlock);
		}
	}

	@Override
	public void openEditStructure(TileEntityStructure structure) {
		if (m_realPlayer == null) {
			super.openEditStructure(structure);
		} else {
			m_realPlayer.openEditStructure(structure);
		}
	}

	@Override
	public void displayVillagerTradeGui(IMerchant villager) {
		if (m_realPlayer == null) {
			super.displayVillagerTradeGui(villager);
		} else {
			m_realPlayer.displayVillagerTradeGui(villager);
		}
	}

	@Override
	public void displayGUIChest(IInventory chestInventory) {
		if (m_realPlayer == null) {
			super.displayGUIChest(chestInventory);
		} else {
			m_realPlayer.displayGUIChest(chestInventory);
		}
	}

	@Override
	public void openGuiHorseInventory(AbstractHorse horse, IInventory inventoryIn) {
		if (m_realPlayer == null) {
			super.openGuiHorseInventory(horse, inventoryIn);
		} else {
			m_realPlayer.openGuiHorseInventory(horse, inventoryIn);
		}
	}

	@Override
	public void displayGui(IInteractionObject guiOwner) {
		if (m_realPlayer == null) {
			super.displayGui(guiOwner);
		} else {
			m_realPlayer.displayGui(guiOwner);
		}
	}

	@Override
	public void openBook(ItemStack stack, EnumHand hand) {
		if (m_realPlayer == null) {
			super.openBook(stack, hand);
		} else {
			m_realPlayer.openBook(stack, hand);
		}
	}

	@Override
	public EnumActionResult interactOn(Entity p_190775_1_, EnumHand p_190775_2_) {
		if (m_realPlayer == null) {
			return super.interactOn(p_190775_1_, p_190775_2_);
		} else {
			return m_realPlayer.interactOn(p_190775_1_, p_190775_2_);
		}
	}

	@Override
	public double getYOffset() {
		if (m_realPlayer == null) {
			return super.getYOffset();
		} else {
			return m_realPlayer.getYOffset();
		}
	}

	@Override
	public void dismountRidingEntity() {
		if (m_realPlayer == null) {
			super.dismountRidingEntity();
		} else {
			m_realPlayer.dismountRidingEntity();
		}
	}

	@Override
	public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
		if (m_realPlayer == null) {
			super.attackTargetEntityWithCurrentItem(targetEntity);
		} else {
			m_realPlayer.attackTargetEntityWithCurrentItem(targetEntity);
		}
	}

	@Override
	public void disableShield(boolean p_190777_1_) {
		if (m_realPlayer == null) {
			super.disableShield(p_190777_1_);
		} else {
			m_realPlayer.disableShield(p_190777_1_);
		}
	}

	@Override
	public void onCriticalHit(Entity entityHit) {
		if (m_realPlayer == null) {
			super.onCriticalHit(entityHit);
		} else {
			m_realPlayer.onCriticalHit(entityHit);
		}
	}

	@Override
	public void onEnchantmentCritical(Entity entityHit) {
		if (m_realPlayer == null) {
			super.onEnchantmentCritical(entityHit);
		} else {
			m_realPlayer.onEnchantmentCritical(entityHit);
		}
	}

	@Override
	public void spawnSweepParticles() {
		if (m_realPlayer == null) {
			super.spawnSweepParticles();
		} else {
			m_realPlayer.spawnSweepParticles();
		}
	}

	@Override
	public void respawnPlayer() {
		if (m_realPlayer == null) {
			super.respawnPlayer();
		} else {
			m_realPlayer.respawnPlayer();
		}
	}

	@Override
	public void setDead() {
		if (m_realPlayer == null) {
			super.setDead();
		} else {
			m_realPlayer.setDead();
		}
	}

	@Override
	public boolean isEntityInsideOpaqueBlock() {
		if (m_realPlayer == null) {
			return super.isEntityInsideOpaqueBlock();
		} else {
			return m_realPlayer.isEntityInsideOpaqueBlock();
		}
	}

	@Override
	public boolean isUser() {
		if (m_realPlayer == null) {
			return super.isUser();
		} else {
			return m_realPlayer.isUser();
		}
	}

	@Override
	public GameProfile getGameProfile() {
		if (m_realPlayer == null) {
			return super.getGameProfile();
		} else {
			return m_realPlayer.getGameProfile();
		}
	}

	@Override
	public SleepResult trySleep(BlockPos bedLocation) {
		if (m_realPlayer == null) {
			return super.trySleep(bedLocation);
		} else {
			return m_realPlayer.trySleep(bedLocation);
		}
	}

	@Override
	public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
		if (m_realPlayer == null) {
			super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);
		} else {
			m_realPlayer.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);
		}
	}

	@Override
	public float getBedOrientationInDegrees() {
		if (m_realPlayer == null) {
			return super.getBedOrientationInDegrees();
		} else {
			return m_realPlayer.getBedOrientationInDegrees();
		}
	}

	@Override
	public boolean isPlayerSleeping() {
		if (m_realPlayer == null) {
			return super.isPlayerSleeping();
		} else {
			return m_realPlayer.isPlayerSleeping();
		}
	}

	@Override
	public boolean isPlayerFullyAsleep() {
		if (m_realPlayer == null) {
			return super.isPlayerFullyAsleep();
		} else {
			return m_realPlayer.isPlayerFullyAsleep();
		}
	}

	@Override
	public int getSleepTimer() {
		if (m_realPlayer == null) {
			return super.getSleepTimer();
		} else {
			return m_realPlayer.getSleepTimer();
		}
	}

	@Override
	public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {
		if (m_realPlayer == null) {
			super.sendStatusMessage(chatComponent, actionBar);
		} else {
			m_realPlayer.sendStatusMessage(chatComponent, actionBar);
		}
	}

	@Override
	public BlockPos getBedLocation() {
		if (m_realPlayer == null) {
			return super.getBedLocation();
		} else {
			return m_realPlayer.getBedLocation();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isSpawnForced() {
		if (m_realPlayer == null) {
			return super.isSpawnForced();
		} else {
			return m_realPlayer.isSpawnForced();
		}
	}

	@Override
	public void setSpawnPoint(BlockPos pos, boolean forced) {
		if (m_realPlayer == null) {
			super.setSpawnPoint(pos, forced);
		} else {
			m_realPlayer.setSpawnPoint(pos, forced);
		}
	}

	@Override
	public void addStat(StatBase stat) {
		if (m_realPlayer == null) {
			super.addStat(stat);
		} else {
			m_realPlayer.addStat(stat);
		}
	}

	@Override
	public void addStat(StatBase stat, int amount) {
		if (m_realPlayer == null) {
			super.addStat(stat, amount);
		} else {
			m_realPlayer.addStat(stat, amount);
		}
	}

	@Override
	public void takeStat(StatBase stat) {
		if (m_realPlayer == null) {
			super.takeStat(stat);
		} else {
			m_realPlayer.takeStat(stat);
		}
	}

	@Override
	public void unlockRecipes(List<IRecipe> p_192021_1_) {
		if (m_realPlayer == null) {
			super.unlockRecipes(p_192021_1_);
		} else {
			m_realPlayer.unlockRecipes(p_192021_1_);
		}
	}

	@Override
	public void unlockRecipes(ResourceLocation[] p_193102_1_) {
		if (m_realPlayer == null) {
			super.unlockRecipes(p_193102_1_);
		} else {
			m_realPlayer.unlockRecipes(p_193102_1_);
		}
	}

	@Override
	public void resetRecipes(List<IRecipe> p_192022_1_) {
		if (m_realPlayer == null) {
			super.resetRecipes(p_192022_1_);
		} else {
			m_realPlayer.resetRecipes(p_192022_1_);
		}
	}

	@Override
	public void jump() {
		if (m_realPlayer == null) {
			super.jump();
		} else {
			m_realPlayer.jump();
		}
	}

	@Override
	public void travel(float p_191986_1_, float p_191986_2_, float p_191986_3_) {
		if (m_realPlayer == null) {
			super.travel(p_191986_1_, p_191986_2_, p_191986_3_);
		} else {
			m_realPlayer.travel(p_191986_1_, p_191986_2_, p_191986_3_);
		}
	}

	@Override
	public float getAIMoveSpeed() {
		if (m_realPlayer == null) {
			return super.getAIMoveSpeed();
		} else {
			return m_realPlayer.getAIMoveSpeed();
		}
	}

	@Override
	public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_) {
		if (m_realPlayer == null) {
			super.addMovementStat(p_71000_1_, p_71000_3_, p_71000_5_);
		} else {
			m_realPlayer.addMovementStat(p_71000_1_, p_71000_3_, p_71000_5_);
		}
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		if (m_realPlayer == null) {
			super.fall(distance, damageMultiplier);
		} else {
			m_realPlayer.fall(distance, damageMultiplier);
		}
	}

	@Override
	public void onKillEntity(EntityLivingBase entityLivingIn) {
		if (m_realPlayer == null) {
			super.onKillEntity(entityLivingIn);
		} else {
			m_realPlayer.onKillEntity(entityLivingIn);
		}
	}

	@Override
	public void setInWeb() {
		if (m_realPlayer == null) {
			super.setInWeb();
		} else {
			m_realPlayer.setInWeb();
		}
	}

	@Override
	public void addExperience(int amount) {
		if (m_realPlayer == null) {
			super.addExperience(amount);
		} else {
			m_realPlayer.addExperience(amount);
		}
	}

	@Override
	public int getXPSeed() {
		if (m_realPlayer == null) {
			return super.getXPSeed();
		} else {
			return m_realPlayer.getXPSeed();
		}
	}

	@Override
	public void onEnchant(ItemStack enchantedItem, int cost) {
		if (m_realPlayer == null) {
			super.onEnchant(enchantedItem, cost);
		} else {
			m_realPlayer.onEnchant(enchantedItem, cost);
		}
	}

	@Override
	public void addExperienceLevel(int levels) {
		if (m_realPlayer == null) {
			super.addExperienceLevel(levels);
		} else {
			m_realPlayer.addExperienceLevel(levels);
		}
	}

	@Override
	public int xpBarCap() {
		if (m_realPlayer == null) {
			return super.xpBarCap();
		} else {
			return m_realPlayer.xpBarCap();
		}
	}

	@Override
	public void addExhaustion(float exhaustion) {
		if (m_realPlayer == null) {
			super.addExhaustion(exhaustion);
		} else {
			m_realPlayer.addExhaustion(exhaustion);
		}
	}

	@Override
	public FoodStats getFoodStats() {
		if (m_realPlayer == null) {
			return super.getFoodStats();
		} else {
			return m_realPlayer.getFoodStats();
		}
	}

	@Override
	public boolean canEat(boolean ignoreHunger) {
		if (m_realPlayer == null) {
			return super.canEat(ignoreHunger);
		} else {
			return m_realPlayer.canEat(ignoreHunger);
		}
	}

	@Override
	public boolean shouldHeal() {
		if (m_realPlayer == null) {
			return super.shouldHeal();
		} else {
			return m_realPlayer.shouldHeal();
		}
	}

	@Override
	public boolean isAllowEdit() {
		if (m_realPlayer == null) {
			return super.isAllowEdit();
		} else {
			return m_realPlayer.isAllowEdit();
		}
	}

	@Override
	public boolean canPlayerEdit(BlockPos pos, EnumFacing facing, ItemStack stack) {
		if (m_realPlayer == null) {
			return super.canPlayerEdit(pos, facing, stack);
		} else {
			return m_realPlayer.canPlayerEdit(pos, facing, stack);
		}
	}

	@Override
	public boolean getAlwaysRenderNameTagForRender() {
		if (m_realPlayer == null) {
			return super.getAlwaysRenderNameTagForRender();
		} else {
			return m_realPlayer.getAlwaysRenderNameTagForRender();
		}
	}

	@Override
	public void sendPlayerAbilities() {
		if (m_realPlayer == null) {
			super.sendPlayerAbilities();
		} else {
			m_realPlayer.sendPlayerAbilities();
		}
	}

	@Override
	public void setGameType(GameType gameType) {
		if (m_realPlayer == null) {
			super.setGameType(gameType);
		} else {
			m_realPlayer.setGameType(gameType);
		}
	}

	@Override
	public String getName() {
		if (m_realPlayer == null) {
			return super.getName();
		} else {
			return m_realPlayer.getName();
		}
	}

	@Override
	public InventoryEnderChest getInventoryEnderChest() {
		if (m_realPlayer == null) {
			return super.getInventoryEnderChest();
		} else {
			return m_realPlayer.getInventoryEnderChest();
		}
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
		if (m_realPlayer == null) {
			return super.getItemStackFromSlot(slotIn);
		} else {
			return m_realPlayer.getItemStackFromSlot(slotIn);
		}
	}

	@Override
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
		if (m_realPlayer == null) {
			super.setItemStackToSlot(slotIn, stack);
		} else {
			m_realPlayer.setItemStackToSlot(slotIn, stack);
		}
	}

	@Override
	public boolean addItemStackToInventory(ItemStack p_191521_1_) {
		if (m_realPlayer == null) {
			return super.addItemStackToInventory(p_191521_1_);
		} else {
			return m_realPlayer.addItemStackToInventory(p_191521_1_);
		}
	}

	@Override
	public Iterable<ItemStack> getHeldEquipment() {
		if (m_realPlayer == null) {
			return super.getHeldEquipment();
		} else {
			return m_realPlayer.getHeldEquipment();
		}
	}

	@Override
	public Iterable<ItemStack> getArmorInventoryList() {
		if (m_realPlayer == null) {
			return super.getArmorInventoryList();
		} else {
			return m_realPlayer.getArmorInventoryList();
		}
	}

	@Override
	public boolean addShoulderEntity(NBTTagCompound p_192027_1_) {
		if (m_realPlayer == null) {
			return super.addShoulderEntity(p_192027_1_);
		} else {
			return m_realPlayer.addShoulderEntity(p_192027_1_);
		}
	}

	@Override
	public boolean isInvisibleToPlayer(EntityPlayer player) {
		if (m_realPlayer == null) {
			return super.isInvisibleToPlayer(player);
		} else {
			return m_realPlayer.isInvisibleToPlayer(player);
		}
	}

	@Override
	public boolean isSpectator() {
		if (m_realPlayer == null) {
			return false;
		} else {
			return m_realPlayer.isSpectator();
		}
	}

	@Override
	public boolean isCreative() {
		if (m_realPlayer == null) {
			return false;
		} else {
			return m_realPlayer.isCreative();
		}
	}

	@Override
	public boolean isPushedByWater() {
		if (m_realPlayer == null) {
			return super.isPushedByWater();
		} else {
			return m_realPlayer.isPushedByWater();
		}
	}

	@Override
	public Scoreboard getWorldScoreboard() {
		if (m_realPlayer == null) {
			return super.getWorldScoreboard();
		} else {
			return m_realPlayer.getWorldScoreboard();
		}
	}

	@Override
	public Team getTeam() {
		if (m_realPlayer == null) {
			return super.getTeam();
		} else {
			return m_realPlayer.getTeam();
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		if (m_realPlayer == null) {
			return super.getDisplayName();
		} else {
			return m_realPlayer.getDisplayName();
		}
	}

	@Override
	public float getEyeHeight() {
		if (m_realPlayer == null) {
			return super.getEyeHeight();
		} else {
			return m_realPlayer.getEyeHeight();
		}
	}

	@Override
	public void setAbsorptionAmount(float amount) {
		if (m_realPlayer == null) {
			super.setAbsorptionAmount(amount);
		} else {
			m_realPlayer.setAbsorptionAmount(amount);
		}
	}

	@Override
	public float getAbsorptionAmount() {
		if (m_realPlayer == null) {
			return super.getAbsorptionAmount();
		} else {
			return m_realPlayer.getAbsorptionAmount();
		}
	}

	@Override
	public boolean canOpen(LockCode code) {
		if (m_realPlayer == null) {
			return super.canOpen(code);
		} else {
			return m_realPlayer.canOpen(code);
		}
	}

	@Override
	public boolean isWearing(EnumPlayerModelParts part) {
		if (m_realPlayer == null) {
			return super.isWearing(part);
		} else {
			return m_realPlayer.isWearing(part);
		}
	}

	@Override
	public boolean sendCommandFeedback() {
		if (m_realPlayer == null) {
			return super.sendCommandFeedback();
		} else {
			return m_realPlayer.sendCommandFeedback();
		}
	}

	@Override
	public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
		if (m_realPlayer == null) {
			return super.replaceItemInInventory(inventorySlot, itemStackIn);
		} else {
			return m_realPlayer.replaceItemInInventory(inventorySlot, itemStackIn);
		}
	}

	@Override
	public boolean hasReducedDebug() {
		if (m_realPlayer == null) {
			return super.hasReducedDebug();
		} else {
			return m_realPlayer.hasReducedDebug();
		}
	}

	@Override
	public void setReducedDebug(boolean reducedDebug) {
		if (m_realPlayer == null) {
			super.setReducedDebug(reducedDebug);
		} else {
			m_realPlayer.setReducedDebug(reducedDebug);
		}
	}

	@Override
	public EnumHandSide getPrimaryHand() {
		if (m_realPlayer == null) {
			return super.getPrimaryHand();
		} else {
			return m_realPlayer.getPrimaryHand();
		}
	}

	@Override
	public void setPrimaryHand(EnumHandSide hand) {
		if (m_realPlayer == null) {
			super.setPrimaryHand(hand);
		} else {
			m_realPlayer.setPrimaryHand(hand);
		}
	}

	@Override
	public NBTTagCompound getLeftShoulderEntity() {
		if (m_realPlayer == null) {
			return super.getLeftShoulderEntity();
		} else {
			return m_realPlayer.getLeftShoulderEntity();
		}
	}

	@Override
	public NBTTagCompound getRightShoulderEntity() {
		if (m_realPlayer == null) {
			return super.getRightShoulderEntity();
		} else {
			return m_realPlayer.getRightShoulderEntity();
		}
	}

	@Override
	public float getCooldownPeriod() {
		if (m_realPlayer == null) {
			return super.getCooldownPeriod();
		} else {
			return m_realPlayer.getCooldownPeriod();
		}
	}

	@Override
	public float getCooledAttackStrength(float adjustTicks) {
		if (m_realPlayer == null) {
			return super.getCooledAttackStrength(adjustTicks);
		} else {
			return m_realPlayer.getCooledAttackStrength(adjustTicks);
		}
	}

	@Override
	public void resetCooldown() {
		if (m_realPlayer == null) {
			super.resetCooldown();
		} else {
			m_realPlayer.resetCooldown();
		}
	}

	@Override
	public CooldownTracker getCooldownTracker() {
		if (m_realPlayer == null) {
			return super.getCooldownTracker();
		} else {
			return m_realPlayer.getCooldownTracker();
		}
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		if (m_realPlayer == null) {
			super.applyEntityCollision(entityIn);
		} else {
			m_realPlayer.applyEntityCollision(entityIn);
		}
	}

	@Override
	public float getLuck() {
		if (m_realPlayer == null) {
			return super.getLuck();
		} else {
			return m_realPlayer.getLuck();
		}
	}

	@Override
	public boolean canUseCommandBlock() {
		if (m_realPlayer == null) {
			return super.canUseCommandBlock();
		} else {
			return m_realPlayer.canUseCommandBlock();
		}
	}

	@Override
	public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
		if (m_realPlayer == null) {
			super.openGui(mod, modGuiId, world, x, y, z);
		} else {
			m_realPlayer.openGui(mod, modGuiId, world, x, y, z);
		}
	}

	@Override
	public BlockPos getBedLocation(int dimension) {
		if (m_realPlayer == null) {
			return super.getBedLocation(dimension);
		} else {
			return m_realPlayer.getBedLocation(dimension);
		}
	}

	@Override
	public boolean isSpawnForced(int dimension) {
		if (m_realPlayer == null) {
			return super.isSpawnForced(dimension);
		} else {
			return m_realPlayer.isSpawnForced(dimension);
		}
	}

	@Override
	public void setSpawnChunk(BlockPos pos, boolean forced, int dimension) {
		if (m_realPlayer == null) {
			super.setSpawnChunk(pos, forced, dimension);
		} else {
			m_realPlayer.setSpawnChunk(pos, forced, dimension);
		}
	}

	@Override
	public float getDefaultEyeHeight() {
		if (m_realPlayer == null) {
			return super.getDefaultEyeHeight();
		} else {
			return m_realPlayer.getDefaultEyeHeight();
		}
	}

	@Override
	public String getDisplayNameString() {
		if (m_realPlayer == null) {
			return super.getDisplayNameString();
		} else {
			return m_realPlayer.getDisplayNameString();
		}
	}

	@Override
	public void refreshDisplayName() {
		if (m_realPlayer == null) {
			super.refreshDisplayName();
		} else {
			m_realPlayer.refreshDisplayName();
		}
	}

	@Override
	public void addPrefix(ITextComponent prefix) {
		if (m_realPlayer == null) {
			super.addPrefix(prefix);
		} else {
			m_realPlayer.addPrefix(prefix);
		}
	}

	@Override
	public void addSuffix(ITextComponent suffix) {
		if (m_realPlayer == null) {
			super.addSuffix(suffix);
		} else {
			m_realPlayer.addSuffix(suffix);
		}
	}

	@Override
	public Collection<ITextComponent> getPrefixes() {
		if (m_realPlayer == null) {
			return super.getPrefixes();
		} else {
			return m_realPlayer.getPrefixes();
		}
	}

	@Override
	public Collection<ITextComponent> getSuffixes() {
		if (m_realPlayer == null) {
			return super.getSuffixes();
		} else {
			return m_realPlayer.getSuffixes();
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (m_realPlayer == null) {
			return super.getCapability(capability, facing);
		} else {
			return m_realPlayer.getCapability(capability, facing);
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (m_realPlayer == null) {
			return super.hasCapability(capability, facing);
		} else {
			return m_realPlayer.hasCapability(capability, facing);
		}
	}

	@Override
	public boolean hasSpawnDimension() {
		if (m_realPlayer == null) {
			return super.hasSpawnDimension();
		} else {
			return m_realPlayer.hasSpawnDimension();
		}
	}

	@Override
	public int getSpawnDimension() {
		if (m_realPlayer == null) {
			return super.getSpawnDimension();
		} else {
			return m_realPlayer.getSpawnDimension();
		}
	}

	@Override
	public void setSpawnDimension(Integer dimension) {
		if (m_realPlayer == null) {
			super.setSpawnDimension(dimension);
		} else {
			m_realPlayer.setSpawnDimension(dimension);
		}
	}

	@Override
	public void onKillCommand() {
		if (m_realPlayer == null) {
			super.onKillCommand();
		} else {
			m_realPlayer.onKillCommand();
		}
	}

	@Override
	public boolean canBreatheUnderwater() {
		if (m_realPlayer == null) {
			return super.canBreatheUnderwater();
		} else {
			return m_realPlayer.canBreatheUnderwater();
		}
	}

	@Override
	public void onEntityUpdate() {
		if (m_realPlayer == null) {
			super.onEntityUpdate();
		} else {
			m_realPlayer.onEntityUpdate();
		}
	}

	@Override
	public boolean isChild() {
		if (m_realPlayer == null) {
			return super.isChild();
		} else {
			return m_realPlayer.isChild();
		}
	}

	@Override
	public Random getRNG() {
		if (m_realPlayer == null) {
			return super.getRNG();
		} else {
			return m_realPlayer.getRNG();
		}
	}

	@Override
	public EntityLivingBase getRevengeTarget() {
		if (m_realPlayer == null) {
			return super.getRevengeTarget();
		} else {
			return m_realPlayer.getRevengeTarget();
		}
	}

	@Override
	public int getRevengeTimer() {
		if (m_realPlayer == null) {
			return super.getRevengeTimer();
		} else {
			return m_realPlayer.getRevengeTimer();
		}
	}

	@Override
	public void setRevengeTarget(EntityLivingBase livingBase) {
		if (m_realPlayer == null) {
			super.setRevengeTarget(livingBase);
		} else {
			m_realPlayer.setRevengeTarget(livingBase);
		}
	}

	@Override
	public EntityLivingBase getLastAttackedEntity() {
		if (m_realPlayer == null) {
			return super.getLastAttackedEntity();
		} else {
			return m_realPlayer.getLastAttackedEntity();
		}
	}

	@Override
	public int getLastAttackedEntityTime() {
		if (m_realPlayer == null) {
			return super.getLastAttackedEntityTime();
		} else {
			return m_realPlayer.getLastAttackedEntityTime();
		}
	}

	@Override
	public void setLastAttackedEntity(Entity entityIn) {
		if (m_realPlayer == null) {
			super.setLastAttackedEntity(entityIn);
		} else {
			m_realPlayer.setLastAttackedEntity(entityIn);
		}
	}

	@Override
	public int getIdleTime() {
		if (m_realPlayer == null) {
			return super.getIdleTime();
		} else {
			return m_realPlayer.getIdleTime();
		}
	}

	@Override
	public void clearActivePotions() {
		if (m_realPlayer == null) {
			super.clearActivePotions();
		} else {
			m_realPlayer.clearActivePotions();
		}
	}

	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		if (m_realPlayer == null) {
			return super.getActivePotionEffects();
		} else {
			return m_realPlayer.getActivePotionEffects();
		}
	}

	@Override
	public Map<Potion, PotionEffect> getActivePotionMap() {
		if (m_realPlayer == null) {
			return super.getActivePotionMap();
		} else {
			return m_realPlayer.getActivePotionMap();
		}
	}

	@Override
	public boolean isPotionActive(Potion potionIn) {
		if (m_realPlayer == null) {
			return super.isPotionActive(potionIn);
		} else {
			return m_realPlayer.isPotionActive(potionIn);
		}
	}

	@Override
	public PotionEffect getActivePotionEffect(Potion potionIn) {
		if (m_realPlayer == null) {
			return super.getActivePotionEffect(potionIn);
		} else {
			return m_realPlayer.getActivePotionEffect(potionIn);
		}
	}

	@Override
	public void addPotionEffect(PotionEffect potioneffectIn) {
		if (m_realPlayer == null) {
			super.addPotionEffect(potioneffectIn);
		} else {
			m_realPlayer.addPotionEffect(potioneffectIn);
		}
	}

	@Override
	public boolean isPotionApplicable(PotionEffect potioneffectIn) {
		if (m_realPlayer == null) {
			return super.isPotionApplicable(potioneffectIn);
		} else {
			return m_realPlayer.isPotionApplicable(potioneffectIn);
		}
	}

	@Override
	public boolean isEntityUndead() {
		if (m_realPlayer == null) {
			return super.isEntityUndead();
		} else {
			return m_realPlayer.isEntityUndead();
		}
	}

	@Override
	public PotionEffect removeActivePotionEffect(Potion potioneffectin) {
		if (m_realPlayer == null) {
			return super.removeActivePotionEffect(potioneffectin);
		} else {
			return m_realPlayer.removeActivePotionEffect(potioneffectin);
		}
	}

	@Override
	public void removePotionEffect(Potion potionIn) {
		if (m_realPlayer == null) {
			super.removePotionEffect(potionIn);
		} else {
			m_realPlayer.removePotionEffect(potionIn);
		}
	}

	@Override
	public void heal(float healAmount) {
		if (m_realPlayer == null) {
			super.heal(healAmount);
		} else {
			m_realPlayer.heal(healAmount);
		}
	}

	@Override
	public void setHealth(float health) {
		if (m_realPlayer == null) {
			super.setHealth(health);
		} else {
			m_realPlayer.setHealth(health);
		}
	}

	@Override
	public DamageSource getLastDamageSource() {
		if (m_realPlayer == null) {
			return super.getLastDamageSource();
		} else {
			return m_realPlayer.getLastDamageSource();
		}
	}

	@Override
	public void renderBrokenItemStack(ItemStack stack) {
		if (m_realPlayer == null) {
			super.renderBrokenItemStack(stack);
		} else {
			m_realPlayer.renderBrokenItemStack(stack);
		}
	}

	@Override
	public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
		if (m_realPlayer == null) {
			super.knockBack(entityIn, strength, xRatio, zRatio);
		} else {
			m_realPlayer.knockBack(entityIn, strength, xRatio, zRatio);
		}
	}

	@Override
	public boolean isOnLadder() {
		if (m_realPlayer == null) {
			return super.isOnLadder();
		} else {
			return m_realPlayer.isOnLadder();
		}
	}

	@Override
	public boolean isEntityAlive() {
		if (m_realPlayer == null) {
			return super.isEntityAlive();
		} else {
			return m_realPlayer.isEntityAlive();
		}
	}

	@Override
	public void performHurtAnimation() {
		if (m_realPlayer == null) {
			super.performHurtAnimation();
		} else {
			m_realPlayer.performHurtAnimation();
		}
	}

	@Override
	public int getTotalArmorValue() {
		if (m_realPlayer == null) {
			return super.getTotalArmorValue();
		} else {
			return m_realPlayer.getTotalArmorValue();
		}
	}

	@Override
	public CombatTracker getCombatTracker() {
		if (m_realPlayer == null) {
			return super.getCombatTracker();
		} else {
			return m_realPlayer.getCombatTracker();
		}
	}

	@Override
	public EntityLivingBase getAttackingEntity() {
		if (m_realPlayer == null) {
			return super.getAttackingEntity();
		} else {
			return m_realPlayer.getAttackingEntity();
		}
	}

	@Override
	public void swingArm(EnumHand hand) {
		if (m_realPlayer == null) {
			super.swingArm(hand);
		} else {
			m_realPlayer.swingArm(hand);
		}
	}

	@Override
	public IAttributeInstance getEntityAttribute(IAttribute attribute) {
		if (m_realPlayer == null) {
			return super.getEntityAttribute(attribute);
		} else {
			return m_realPlayer.getEntityAttribute(attribute);
		}
	}

	@Override
	public AbstractAttributeMap getAttributeMap() {
		if (m_realPlayer == null) {
			return super.getAttributeMap();
		} else {
			return m_realPlayer.getAttributeMap();
		}
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		if (m_realPlayer == null) {
			return super.getCreatureAttribute();
		} else {
			return m_realPlayer.getCreatureAttribute();
		}
	}

	@Override
	public ItemStack getHeldItemMainhand() {
		if (m_realPlayer == null) {
			return super.getHeldItemMainhand();
		} else {
			return m_realPlayer.getHeldItemMainhand();
		}
	}

	@Override
	public ItemStack getHeldItemOffhand() {
		if (m_realPlayer == null) {
			return super.getHeldItemOffhand();
		} else {
			return m_realPlayer.getHeldItemOffhand();
		}
	}

	@Override
	public ItemStack getHeldItem(EnumHand hand) {
		if (m_realPlayer == null) {
			return super.getHeldItem(hand);
		} else {
			return m_realPlayer.getHeldItem(hand);
		}
	}

	@Override
	public void setHeldItem(EnumHand hand, ItemStack stack) {
		if (m_realPlayer == null) {
			super.setHeldItem(hand, stack);
		} else {
			m_realPlayer.setHeldItem(hand, stack);
		}
	}

	@Override
	public boolean hasItemInSlot(EntityEquipmentSlot p_190630_1_) {
		if (m_realPlayer == null) {
			return super.hasItemInSlot(p_190630_1_);
		} else {
			return m_realPlayer.hasItemInSlot(p_190630_1_);
		}
	}

	@Override
	public void setSprinting(boolean sprinting) {
		if (m_realPlayer == null) {
			super.setSprinting(sprinting);
		} else {
			m_realPlayer.setSprinting(sprinting);
		}
	}

	@Override
	public void dismountEntity(Entity entityIn) {
		if (m_realPlayer == null) {
			super.dismountEntity(entityIn);
		} else {
			m_realPlayer.dismountEntity(entityIn);
		}
	}

	@Override
	public void setAIMoveSpeed(float speedIn) {
		if (m_realPlayer == null) {
			super.setAIMoveSpeed(speedIn);
		} else {
			m_realPlayer.setAIMoveSpeed(speedIn);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.attackEntityAsMob(entityIn);
		} else {
			return m_realPlayer.attackEntityAsMob(entityIn);
		}
	}

	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
			int posRotationIncrements, boolean teleport) {
		if (m_realPlayer == null) {
			super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
		} else {
			m_realPlayer.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
		}
	}

	@Override
	public void setJumping(boolean jumping) {
		if (m_realPlayer == null) {
			super.setJumping(jumping);
		} else {
			m_realPlayer.setJumping(jumping);
		}
	}

	@Override
	public void onItemPickup(Entity entityIn, int quantity) {
		if (m_realPlayer == null) {
			super.onItemPickup(entityIn, quantity);
		} else {
			m_realPlayer.onItemPickup(entityIn, quantity);
		}
	}

	@Override
	public boolean canEntityBeSeen(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.canEntityBeSeen(entityIn);
		} else {
			return m_realPlayer.canEntityBeSeen(entityIn);
		}
	}

	@Override
	public Vec3d getLook(float partialTicks) {
		if (m_realPlayer == null) {
			return super.getLook(partialTicks);
		} else {
			return m_realPlayer.getLook(partialTicks);
		}
	}

	@Override
	public float getSwingProgress(float partialTickTime) {
		if (m_realPlayer == null) {
			return super.getSwingProgress(partialTickTime);
		} else {
			return m_realPlayer.getSwingProgress(partialTickTime);
		}
	}

	@Override
	public boolean isServerWorld() {
		if (m_realPlayer == null) {
			return super.isServerWorld();
		} else {
			return m_realPlayer.isServerWorld();
		}
	}

	@Override
	public boolean canBeCollidedWith() {
		if (m_realPlayer == null) {
			return super.canBeCollidedWith();
		} else {
			return m_realPlayer.canBeCollidedWith();
		}
	}

	@Override
	public boolean canBePushed() {
		if (m_realPlayer == null) {
			return super.canBePushed();
		} else {
			return m_realPlayer.canBePushed();
		}
	}

	@Override
	public float getRotationYawHead() {
		if (m_realPlayer == null) {
			return super.getRotationYawHead();
		} else {
			return m_realPlayer.getRotationYawHead();
		}
	}

	@Override
	public void setRotationYawHead(float rotation) {
		if (m_realPlayer == null) {
			super.setRotationYawHead(rotation);
		} else {
			m_realPlayer.setRotationYawHead(rotation);
		}
	}

	@Override
	public void setRenderYawOffset(float offset) {
		if (m_realPlayer == null) {
			super.setRenderYawOffset(offset);
		} else {
			m_realPlayer.setRenderYawOffset(offset);
		}
	}

	@Override
	public void sendEnterCombat() {
		if (m_realPlayer == null) {
			super.sendEnterCombat();
		} else {
			m_realPlayer.sendEnterCombat();
		}
	}

	@Override
	public void sendEndCombat() {
		if (m_realPlayer == null) {
			super.sendEndCombat();
		} else {
			m_realPlayer.sendEndCombat();
		}
	}

	@Override
	public void curePotionEffects(ItemStack curativeItem) {
		if (m_realPlayer == null) {
			super.curePotionEffects(curativeItem);
		} else {
			m_realPlayer.curePotionEffects(curativeItem);
		}
	}

	@Override
	public boolean shouldRiderFaceForward(EntityPlayer player) {
		if (m_realPlayer == null) {
			return super.shouldRiderFaceForward(player);
		} else {
			return m_realPlayer.shouldRiderFaceForward(player);
		}
	}

	@Override
	public boolean isHandActive() {
		if (m_realPlayer == null) {
			return super.isHandActive();
		} else {
			return m_realPlayer.isHandActive();
		}
	}

	@Override
	public EnumHand getActiveHand() {
		if (m_realPlayer == null) {
			return super.getActiveHand();
		} else {
			return m_realPlayer.getActiveHand();
		}
	}

	@Override
	public void setActiveHand(EnumHand hand) {
		if (m_realPlayer == null) {
			super.setActiveHand(hand);
		} else {
			m_realPlayer.setActiveHand(hand);
		}
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (m_realPlayer == null) {
			super.notifyDataManagerChange(key);
		} else {
			m_realPlayer.notifyDataManagerChange(key);
		}
	}

	@Override
	public ItemStack getActiveItemStack() {
		if (m_realPlayer == null) {
			return super.getActiveItemStack();
		} else {
			return m_realPlayer.getActiveItemStack();
		}
	}

	@Override
	public int getItemInUseCount() {
		if (m_realPlayer == null) {
			return super.getItemInUseCount();
		} else {
			return m_realPlayer.getItemInUseCount();
		}
	}

	@Override
	public int getItemInUseMaxCount() {
		if (m_realPlayer == null) {
			return super.getItemInUseMaxCount();
		} else {
			return m_realPlayer.getItemInUseMaxCount();
		}
	}

	@Override
	public void stopActiveHand() {
		if (m_realPlayer == null) {
			super.stopActiveHand();
		} else {
			m_realPlayer.stopActiveHand();
		}
	}

	@Override
	public void resetActiveHand() {
		if (m_realPlayer == null) {
			super.resetActiveHand();
		} else {
			m_realPlayer.resetActiveHand();
		}
	}

	@Override
	public boolean isActiveItemStackBlocking() {
		if (m_realPlayer == null) {
			return super.isActiveItemStackBlocking();
		} else {
			return m_realPlayer.isActiveItemStackBlocking();
		}
	}

	@Override
	public boolean isElytraFlying() {
		if (m_realPlayer == null) {
			return super.isElytraFlying();
		} else {
			return m_realPlayer.isElytraFlying();
		}
	}

	@Override
	public int getTicksElytraFlying() {
		if (m_realPlayer == null) {
			return super.getTicksElytraFlying();
		} else {
			return m_realPlayer.getTicksElytraFlying();
		}
	}

	@Override
	public boolean attemptTeleport(double x, double y, double z) {
		if (m_realPlayer == null) {
			return super.attemptTeleport(x, y, z);
		} else {
			return m_realPlayer.attemptTeleport(x, y, z);
		}
	}

	@Override
	public boolean canBeHitWithPotion() {
		if (m_realPlayer == null) {
			return super.canBeHitWithPotion();
		} else {
			return m_realPlayer.canBeHitWithPotion();
		}
	}

	@Override
	public boolean attackable() {
		if (m_realPlayer == null) {
			return super.attackable();
		} else {
			return m_realPlayer.attackable();
		}
	}

	@Override
	public void setPartying(BlockPos pos, boolean p_191987_2_) {
		if (m_realPlayer == null) {
			super.setPartying(pos, p_191987_2_);
		} else {
			m_realPlayer.setPartying(pos, p_191987_2_);
		}
	}

	@Override
	public int getEntityId() {
		if (m_realPlayer == null) {
			return super.getEntityId();
		} else {
			return m_realPlayer.getEntityId();
		}
	}

	@Override
	public void setEntityId(int id) {
		if (m_realPlayer == null) {
			super.setEntityId(id);
		} else {
			m_realPlayer.setEntityId(id);
		}
	}

	@Override
	public Set<String> getTags() {
		if (m_realPlayer == null) {
			return super.getTags();
		} else {
			return m_realPlayer.getTags();
		}
	}

	@Override
	public boolean addTag(String tag) {
		if (m_realPlayer == null) {
			return super.addTag(tag);
		} else {
			return m_realPlayer.addTag(tag);
		}
	}

	@Override
	public boolean removeTag(String tag) {
		if (m_realPlayer == null) {
			return super.removeTag(tag);
		} else {
			return m_realPlayer.removeTag(tag);
		}
	}

	@Override
	public EntityDataManager getDataManager() {
		if (m_realPlayer == null) {
			return super.getDataManager();
		} else {
			return m_realPlayer.getDataManager();
		}
	}

	@Override
	public boolean equals(Object p_equals_1_) {
		if (m_realPlayer == null) {
			return super.equals(p_equals_1_);
		} else {
			return m_realPlayer.equals(p_equals_1_);
		}
	}

	@Override
	public int hashCode() {
		if (m_realPlayer == null) {
			return super.hashCode();
		} else {
			return m_realPlayer.hashCode();
		}
	}

	@Override
	public void setDropItemsWhenDead(boolean dropWhenDead) {
		if (m_realPlayer == null) {
			super.setDropItemsWhenDead(dropWhenDead);
		} else {
			m_realPlayer.setDropItemsWhenDead(dropWhenDead);
		}
	}

	@Override
	public void setPosition(double x, double y, double z) {
		if (m_realPlayer == null) {
			super.setPosition(x, y, z);
		} else {
			m_realPlayer.setPosition(x, y, z);
		}
	}

	@Override
	public void turn(float yaw, float pitch) {
		if (m_realPlayer == null) {
			super.turn(yaw, pitch);
		} else {
			m_realPlayer.turn(yaw, pitch);
		}
	}

	@Override
	public void setFire(int seconds) {
		if (m_realPlayer == null) {
			super.setFire(seconds);
		} else {
			m_realPlayer.setFire(seconds);
		}
	}

	@Override
	public void extinguish() {
		if (m_realPlayer == null) {
			super.extinguish();
		} else {
			m_realPlayer.extinguish();
		}
	}

	@Override
	public boolean isOffsetPositionInLiquid(double x, double y, double z) {
		if (m_realPlayer == null) {
			return super.isOffsetPositionInLiquid(x, y, z);
		} else {
			return m_realPlayer.isOffsetPositionInLiquid(x, y, z);
		}
	}

	@Override
	public void move(MoverType type, double x, double y, double z) {
		if (m_realPlayer == null) {
			super.move(type, x, y, z);
		} else {
			m_realPlayer.move(type, x, y, z);
		}
	}

	@Override
	public void resetPositionToBB() {
		if (m_realPlayer == null) {
			super.resetPositionToBB();
		} else {
			m_realPlayer.resetPositionToBB();
		}
	}

	@Override
	public boolean isSilent() {
		if (m_realPlayer == null) {
			return super.isSilent();
		} else {
			return m_realPlayer.isSilent();
		}
	}

	@Override
	public void setSilent(boolean isSilent) {
		if (m_realPlayer == null) {
			super.setSilent(isSilent);
		} else {
			m_realPlayer.setSilent(isSilent);
		}
	}

	@Override
	public boolean hasNoGravity() {
		if (m_realPlayer == null) {
			return super.hasNoGravity();
		} else {
			return m_realPlayer.hasNoGravity();
		}
	}

	@Override
	public void setNoGravity(boolean noGravity) {
		if (m_realPlayer == null) {
			super.setNoGravity(noGravity);
		} else {
			m_realPlayer.setNoGravity(noGravity);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		if (m_realPlayer == null) {
			return super.getCollisionBoundingBox();
		} else {
			return m_realPlayer.getCollisionBoundingBox();
		}
	}

	@Override
	public boolean isWet() {
		if (m_realPlayer == null) {
			return super.isWet();
		} else {
			return m_realPlayer.isWet();
		}
	}

	@Override
	public boolean isInWater() {
		if (m_realPlayer == null) {
			return super.isInWater();
		} else {
			return m_realPlayer.isInWater();
		}
	}

	@Override
	public boolean isOverWater() {
		if (m_realPlayer == null) {
			return super.isOverWater();
		} else {
			return m_realPlayer.isOverWater();
		}
	}

	@Override
	public boolean handleWaterMovement() {
		if (m_realPlayer == null) {
			return super.handleWaterMovement();
		} else {
			return m_realPlayer.handleWaterMovement();
		}
	}

	@Override
	public void spawnRunningParticles() {
		if (m_realPlayer == null) {
			super.spawnRunningParticles();
		} else {
			m_realPlayer.spawnRunningParticles();
		}
	}

	@Override
	public boolean isInsideOfMaterial(Material materialIn) {
		if (m_realPlayer == null) {
			return super.isInsideOfMaterial(materialIn);
		} else {
			return m_realPlayer.isInsideOfMaterial(materialIn);
		}
	}

	@Override
	public boolean isInLava() {
		if (m_realPlayer == null) {
			return super.isInLava();
		} else {
			return m_realPlayer.isInLava();
		}
	}

	@Override
	public void moveRelative(float strafe, float up, float forward, float friction) {
		if (m_realPlayer == null) {
			super.moveRelative(strafe, up, forward, friction);
		} else {
			m_realPlayer.moveRelative(strafe, up, forward, friction);
		}
	}

	@Override
	public int getBrightnessForRender() {
		if (m_realPlayer == null) {
			return super.getBrightnessForRender();
		} else {
			return m_realPlayer.getBrightnessForRender();
		}
	}

	@Override
	public float getBrightness() {
		if (m_realPlayer == null) {
			return super.getBrightness();
		} else {
			return m_realPlayer.getBrightness();
		}
	}

	@Override
	public void setWorld(World worldIn) {
		if (m_realPlayer == null) {
			super.setWorld(worldIn);
		} else {
			m_realPlayer.setWorld(worldIn);
		}
	}

	@Override
	public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
		if (m_realPlayer == null) {
			super.setPositionAndRotation(x, y, z, yaw, pitch);
		} else {
			m_realPlayer.setPositionAndRotation(x, y, z, yaw, pitch);
		}
	}

	@Override
	public void moveToBlockPosAndAngles(BlockPos pos, float rotationYawIn, float rotationPitchIn) {
		if (m_realPlayer == null) {
			super.moveToBlockPosAndAngles(pos, rotationYawIn, rotationPitchIn);
		} else {
			m_realPlayer.moveToBlockPosAndAngles(pos, rotationYawIn, rotationPitchIn);
		}
	}

	@Override
	public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
		if (m_realPlayer == null) {
			super.setLocationAndAngles(x, y, z, yaw, pitch);
		} else {
			m_realPlayer.setLocationAndAngles(x, y, z, yaw, pitch);
		}
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		if (m_realPlayer == null) {
			super.onCollideWithPlayer(entityIn);
		} else {
			m_realPlayer.onCollideWithPlayer(entityIn);
		}
	}

	@Override
	public void addVelocity(double x, double y, double z) {
		if (m_realPlayer == null) {
			super.addVelocity(x, y, z);
		} else {
			m_realPlayer.addVelocity(x, y, z);
		}
	}

	@Override
	public Vec3d getPositionEyes(float partialTicks) {
		if (m_realPlayer == null) {
			return super.getPositionEyes(partialTicks);
		} else {
			return m_realPlayer.getPositionEyes(partialTicks);
		}
	}

	@Override
	public RayTraceResult rayTrace(double blockReachDistance, float partialTicks) {
		if (m_realPlayer == null) {
			return super.rayTrace(blockReachDistance, partialTicks);
		} else {
			return m_realPlayer.rayTrace(blockReachDistance, partialTicks);
		}
	}

	@Override
	public void awardKillScore(Entity p_191956_1_, int p_191956_2_, DamageSource p_191956_3_) {
		if (m_realPlayer == null) {
			super.awardKillScore(p_191956_1_, p_191956_2_, p_191956_3_);
		} else {
			m_realPlayer.awardKillScore(p_191956_1_, p_191956_2_, p_191956_3_);
		}
	}

	@Override
	public boolean isInRangeToRender3d(double x, double y, double z) {
		if (m_realPlayer == null) {
			return super.isInRangeToRender3d(x, y, z);
		} else {
			return m_realPlayer.isInRangeToRender3d(x, y, z);
		}
	}

	@Override
	public boolean isInRangeToRenderDist(double distance) {
		if (m_realPlayer == null) {
			return super.isInRangeToRenderDist(distance);
		} else {
			return m_realPlayer.isInRangeToRenderDist(distance);
		}
	}

	@Override
	public boolean writeToNBTAtomically(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			return super.writeToNBTAtomically(compound);
		} else {
			return m_realPlayer.writeToNBTAtomically(compound);
		}
	}

	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			return super.writeToNBTOptional(compound);
		} else {
			return m_realPlayer.writeToNBTOptional(compound);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			return super.writeToNBT(compound);
		} else {
			return m_realPlayer.writeToNBT(compound);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			super.readFromNBT(compound);
		} else {
			m_realPlayer.readFromNBT(compound);
		}
	}

	@Override
	public EntityItem dropItem(Item itemIn, int size) {
		if (m_realPlayer == null) {
			return super.dropItem(itemIn, size);
		} else {
			return m_realPlayer.dropItem(itemIn, size);
		}
	}

	@Override
	public EntityItem dropItemWithOffset(Item itemIn, int size, float offsetY) {
		if (m_realPlayer == null) {
			return super.dropItemWithOffset(itemIn, size, offsetY);
		} else {
			return m_realPlayer.dropItemWithOffset(itemIn, size, offsetY);
		}
	}

	@Override
	public EntityItem entityDropItem(ItemStack stack, float offsetY) {
		if (m_realPlayer == null) {
			return super.entityDropItem(stack, offsetY);
		} else {
			return m_realPlayer.entityDropItem(stack, offsetY);
		}
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if (m_realPlayer == null) {
			return super.processInitialInteract(player, hand);
		} else {
			return m_realPlayer.processInitialInteract(player, hand);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.getCollisionBox(entityIn);
		} else {
			return m_realPlayer.getCollisionBox(entityIn);
		}
	}

	@Override
	public void updatePassenger(Entity passenger) {
		if (m_realPlayer == null) {
			super.updatePassenger(passenger);
		} else {
			m_realPlayer.updatePassenger(passenger);
		}
	}

	@Override
	public void applyOrientationToEntity(Entity entityToUpdate) {
		if (m_realPlayer == null) {
			super.applyOrientationToEntity(entityToUpdate);
		} else {
			m_realPlayer.applyOrientationToEntity(entityToUpdate);
		}
	}

	@Override
	public double getMountedYOffset() {
		if (m_realPlayer == null) {
			return super.getMountedYOffset();
		} else {
			return m_realPlayer.getMountedYOffset();
		}
	}

	@Override
	public boolean startRiding(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.startRiding(entityIn);
		} else {
			return m_realPlayer.startRiding(entityIn);
		}
	}

	@Override
	public boolean startRiding(Entity entityIn, boolean force) {
		if (m_realPlayer == null) {
			return super.startRiding(entityIn, force);
		} else {
			return m_realPlayer.startRiding(entityIn, force);
		}
	}

	@Override
	public void removePassengers() {
		if (m_realPlayer == null) {
			super.removePassengers();
		} else {
			m_realPlayer.removePassengers();
		}
	}

	@Override
	public float getCollisionBorderSize() {
		if (m_realPlayer == null) {
			return super.getCollisionBorderSize();
		} else {
			return m_realPlayer.getCollisionBorderSize();
		}
	}

	@Override
	public Vec3d getLookVec() {
		if (m_realPlayer == null) {
			return super.getLookVec();
		} else {
			return m_realPlayer.getLookVec();
		}
	}

	@Override
	public Vec2f getPitchYaw() {
		if (m_realPlayer == null) {
			return super.getPitchYaw();
		} else {
			return m_realPlayer.getPitchYaw();
		}
	}

	@Override
	public Vec3d getForward() {
		if (m_realPlayer == null) {
			return super.getForward();
		} else {
			return m_realPlayer.getForward();
		}
	}

	@Override
	public void setPortal(BlockPos pos) {
		if (m_realPlayer == null) {
			super.setPortal(pos);
		} else {
			m_realPlayer.setPortal(pos);
		}
	}

	@Override
	public void setVelocity(double x, double y, double z) {
		if (m_realPlayer == null) {
			super.setVelocity(x, y, z);
		} else {
			m_realPlayer.setVelocity(x, y, z);
		}
	}

	@Override
	public Iterable<ItemStack> getEquipmentAndArmor() {
		if (m_realPlayer == null) {
			return super.getEquipmentAndArmor();
		} else {
			return m_realPlayer.getEquipmentAndArmor();
		}
	}

	@Override
	public boolean isBurning() {
		if (m_realPlayer == null) {
			return super.isBurning();
		} else {
			return m_realPlayer.isBurning();
		}
	}

	@Override
	public boolean isRiding() {
		if (m_realPlayer == null) {
			return super.isRiding();
		} else {
			return m_realPlayer.isRiding();
		}
	}

	@Override
	public boolean isBeingRidden() {
		if (m_realPlayer == null) {
			return super.isBeingRidden();
		} else {
			return m_realPlayer.isBeingRidden();
		}
	}

	@Override
	public boolean isSneaking() {
		if (m_realPlayer == null) {
			return super.isSneaking();
		} else {
			return m_realPlayer.isSneaking();
		}
	}

	@Override
	public void setSneaking(boolean sneaking) {
		if (m_realPlayer == null) {
			super.setSneaking(sneaking);
		} else {
			m_realPlayer.setSneaking(sneaking);
		}
	}

	@Override
	public boolean isSprinting() {
		if (m_realPlayer == null) {
			return super.isSprinting();
		} else {
			return m_realPlayer.isSprinting();
		}
	}

	@Override
	public boolean isGlowing() {
		if (m_realPlayer == null) {
			return super.isGlowing();
		} else {
			return m_realPlayer.isGlowing();
		}
	}

	@Override
	public void setGlowing(boolean glowingIn) {
		if (m_realPlayer == null) {
			super.setGlowing(glowingIn);
		} else {
			m_realPlayer.setGlowing(glowingIn);
		}
	}

	@Override
	public boolean isInvisible() {
		if (m_realPlayer == null) {
			return super.isInvisible();
		} else {
			return m_realPlayer.isInvisible();
		}
	}

	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.isOnSameTeam(entityIn);
		} else {
			return m_realPlayer.isOnSameTeam(entityIn);
		}
	}

	@Override
	public boolean isOnScoreboardTeam(Team teamIn) {
		if (m_realPlayer == null) {
			return super.isOnScoreboardTeam(teamIn);
		} else {
			return m_realPlayer.isOnScoreboardTeam(teamIn);
		}
	}

	@Override
	public void setInvisible(boolean invisible) {
		if (m_realPlayer == null) {
			super.setInvisible(invisible);
		} else {
			m_realPlayer.setInvisible(invisible);
		}
	}

	@Override
	public int getAir() {
		if (m_realPlayer == null) {
			return super.getAir();
		} else {
			return m_realPlayer.getAir();
		}
	}

	@Override
	public void setAir(int air) {
		if (m_realPlayer == null) {
			super.setAir(air);
		} else {
			m_realPlayer.setAir(air);
		}
	}

	@Override
	public void onStruckByLightning(EntityLightningBolt lightningBolt) {
		if (m_realPlayer == null) {
			super.onStruckByLightning(lightningBolt);
		} else {
			m_realPlayer.onStruckByLightning(lightningBolt);
		}
	}

	@Override
	public Entity[] getParts() {
		if (m_realPlayer == null) {
			return super.getParts();
		} else {
			return m_realPlayer.getParts();
		}
	}

	@Override
	public boolean isEntityEqual(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.isEntityEqual(entityIn);
		} else {
			return m_realPlayer.isEntityEqual(entityIn);
		}
	}

	@Override
	public boolean canBeAttackedWithItem() {
		if (m_realPlayer == null) {
			return super.canBeAttackedWithItem();
		} else {
			return m_realPlayer.canBeAttackedWithItem();
		}
	}

	@Override
	public boolean hitByEntity(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.hitByEntity(entityIn);
		} else {
			return m_realPlayer.hitByEntity(entityIn);
		}
	}

	@Override
	public String toString() {
		if (m_realPlayer == null) {
			return super.toString();
		} else {
			return m_realPlayer.toString();
		}
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		if (m_realPlayer == null) {
			return super.isEntityInvulnerable(source);
		} else {
			return m_realPlayer.isEntityInvulnerable(source);
		}
	}

	@Override
	public boolean getIsInvulnerable() {
		if (m_realPlayer == null) {
			return super.getIsInvulnerable();
		} else {
			return m_realPlayer.getIsInvulnerable();
		}
	}

	@Override
	public void setEntityInvulnerable(boolean isInvulnerable) {
		if (m_realPlayer == null) {
			super.setEntityInvulnerable(isInvulnerable);
		} else {
			m_realPlayer.setEntityInvulnerable(isInvulnerable);
		}
	}

	@Override
	public void copyLocationAndAnglesFrom(Entity entityIn) {
		if (m_realPlayer == null) {
			super.copyLocationAndAnglesFrom(entityIn);
		} else {
			m_realPlayer.copyLocationAndAnglesFrom(entityIn);
		}
	}

	@Override
	public Entity changeDimension(int dimensionIn) {
		if (m_realPlayer == null) {
			return super.changeDimension(dimensionIn);
		} else {
			return m_realPlayer.changeDimension(dimensionIn);
		}
	}

	@Override
	public boolean isNonBoss() {
		if (m_realPlayer == null) {
			return super.isNonBoss();
		} else {
			return m_realPlayer.isNonBoss();
		}
	}

	@Override
	public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
		if (m_realPlayer == null) {
			return super.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn);
		} else {
			return m_realPlayer.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn);
		}
	}

	@Override
	public boolean canExplosionDestroyBlock(Explosion explosionIn, World worldIn, BlockPos pos,
			IBlockState blockStateIn, float p_174816_5_) {
		if (m_realPlayer == null) {
			return super.canExplosionDestroyBlock(explosionIn, worldIn, pos, blockStateIn, p_174816_5_);
		} else {
			return m_realPlayer.canExplosionDestroyBlock(explosionIn, worldIn, pos, blockStateIn, p_174816_5_);
		}
	}

	@Override
	public int getMaxFallHeight() {
		if (m_realPlayer == null) {
			return super.getMaxFallHeight();
		} else {
			return m_realPlayer.getMaxFallHeight();
		}
	}

	@Override
	public Vec3d getLastPortalVec() {
		if (m_realPlayer == null) {
			return super.getLastPortalVec();
		} else {
			return m_realPlayer.getLastPortalVec();
		}
	}

	@Override
	public EnumFacing getTeleportDirection() {
		if (m_realPlayer == null) {
			return super.getTeleportDirection();
		} else {
			return m_realPlayer.getTeleportDirection();
		}
	}

	@Override
	public boolean doesEntityNotTriggerPressurePlate() {
		if (m_realPlayer == null) {
			return super.doesEntityNotTriggerPressurePlate();
		} else {
			return m_realPlayer.doesEntityNotTriggerPressurePlate();
		}
	}

	@Override
	public void addEntityCrashInfo(CrashReportCategory category) {
		if (m_realPlayer == null) {
			super.addEntityCrashInfo(category);
		} else {
			m_realPlayer.addEntityCrashInfo(category);
		}
	}

	@Override
	public void setUniqueId(UUID uniqueIdIn) {
		if (m_realPlayer == null) {
			super.setUniqueId(uniqueIdIn);
		} else {
			m_realPlayer.setUniqueId(uniqueIdIn);
		}
	}

	@Override
	public boolean canRenderOnFire() {
		if (m_realPlayer == null) {
			return super.canRenderOnFire();
		} else {
			return m_realPlayer.canRenderOnFire();
		}
	}

	@Override
	public UUID getUniqueID() {
		if (m_realPlayer == null) {
			return super.getUniqueID();
		} else {
			return m_realPlayer.getUniqueID();
		}
	}

	@Override
	public String getCachedUniqueIdString() {
		if (m_realPlayer == null) {
			return super.getCachedUniqueIdString();
		} else {
			return m_realPlayer.getCachedUniqueIdString();
		}
	}

	@Override
	public void setCustomNameTag(String name) {
		if (m_realPlayer == null) {
			super.setCustomNameTag(name);
		} else {
			m_realPlayer.setCustomNameTag(name);
		}
	}

	@Override
	public String getCustomNameTag() {
		if (m_realPlayer == null) {
			return super.getCustomNameTag();
		} else {
			return m_realPlayer.getCustomNameTag();
		}
	}

	@Override
	public boolean hasCustomName() {
		if (m_realPlayer == null) {
			return super.hasCustomName();
		} else {
			return m_realPlayer.hasCustomName();
		}
	}

	@Override
	public void setAlwaysRenderNameTag(boolean alwaysRenderNameTag) {
		if (m_realPlayer == null) {
			super.setAlwaysRenderNameTag(alwaysRenderNameTag);
		} else {
			m_realPlayer.setAlwaysRenderNameTag(alwaysRenderNameTag);
		}
	}

	@Override
	public boolean getAlwaysRenderNameTag() {
		if (m_realPlayer == null) {
			return super.getAlwaysRenderNameTag();
		} else {
			return m_realPlayer.getAlwaysRenderNameTag();
		}
	}

	@Override
	public void setPositionAndUpdate(double x, double y, double z) {
		if (m_realPlayer == null) {
			super.setPositionAndUpdate(x, y, z);
		} else {
			m_realPlayer.setPositionAndUpdate(x, y, z);
		}
	}

	@Override
	public EnumFacing getHorizontalFacing() {
		if (m_realPlayer == null) {
			return super.getHorizontalFacing();
		} else {
			return m_realPlayer.getHorizontalFacing();
		}
	}

	@Override
	public EnumFacing getAdjustedHorizontalFacing() {
		if (m_realPlayer == null) {
			return super.getAdjustedHorizontalFacing();
		} else {
			return m_realPlayer.getAdjustedHorizontalFacing();
		}
	}

	@Override
	public boolean isSpectatedByPlayer(EntityPlayerMP player) {
		if (m_realPlayer == null) {
			return super.isSpectatedByPlayer(player);
		} else {
			return m_realPlayer.isSpectatedByPlayer(player);
		}
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		if (m_realPlayer == null) {
			return super.getEntityBoundingBox();
		} else {
			return m_realPlayer.getEntityBoundingBox();
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (m_realPlayer == null) {
			return super.getRenderBoundingBox();
		} else {
			return m_realPlayer.getRenderBoundingBox();
		}
	}

	@Override
	public void setEntityBoundingBox(AxisAlignedBB bb) {
		if (m_realPlayer == null) {
			super.setEntityBoundingBox(bb);
		} else {
			m_realPlayer.setEntityBoundingBox(bb);
		}
	}

	@Override
	public boolean isOutsideBorder() {
		if (m_realPlayer == null) {
			return super.isOutsideBorder();
		} else {
			return m_realPlayer.isOutsideBorder();
		}
	}

	@Override
	public void setOutsideBorder(boolean outsideBorder) {
		if (m_realPlayer == null) {
			super.setOutsideBorder(outsideBorder);
		} else {
			m_realPlayer.setOutsideBorder(outsideBorder);
		}
	}

	@Override
	public void sendMessage(ITextComponent component) {
		if (m_realPlayer == null) {
			super.sendMessage(component);
		} else {
			m_realPlayer.sendMessage(component);
		}
	}

	@Override
	public boolean canUseCommand(int permLevel, String commandName) {
		if (m_realPlayer == null) {
			return super.canUseCommand(permLevel, commandName);
		} else {
			return m_realPlayer.canUseCommand(permLevel, commandName);
		}
	}

	@Override
	public BlockPos getPosition() {
		if (m_realPlayer == null) {
			return super.getPosition();
		} else {
			return m_realPlayer.getPosition();
		}
	}

	@Override
	public Vec3d getPositionVector() {
		if (m_realPlayer == null) {
			return super.getPositionVector();
		} else {
			return m_realPlayer.getPositionVector();
		}
	}

	@Override
	public Entity getCommandSenderEntity() {
		if (m_realPlayer == null) {
			return super.getCommandSenderEntity();
		} else {
			return m_realPlayer.getCommandSenderEntity();
		}
	}

	@Override
	public void setCommandStat(Type type, int amount) {
		if (m_realPlayer == null) {
			super.setCommandStat(type, amount);
		} else {
			m_realPlayer.setCommandStat(type, amount);
		}
	}

	@Override
	public MinecraftServer getServer() {
		if (m_realPlayer == null) {
			return super.getServer();
		} else {
			return m_realPlayer.getServer();
		}
	}

	@Override
	public CommandResultStats getCommandStats() {
		if (m_realPlayer == null) {
			return super.getCommandStats();
		} else {
			return m_realPlayer.getCommandStats();
		}
	}

	@Override
	public void setCommandStats(Entity entityIn) {
		if (m_realPlayer == null) {
			super.setCommandStats(entityIn);
		} else {
			m_realPlayer.setCommandStats(entityIn);
		}
	}

	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		if (m_realPlayer == null) {
			return super.applyPlayerInteraction(player, vec, hand);
		} else {
			return m_realPlayer.applyPlayerInteraction(player, vec, hand);
		}
	}

	@Override
	public boolean isImmuneToExplosions() {
		if (m_realPlayer == null) {
			return super.isImmuneToExplosions();
		} else {
			return m_realPlayer.isImmuneToExplosions();
		}
	}

	@Override
	public NBTTagCompound getEntityData() {
		if (m_realPlayer == null) {
			return super.getEntityData();
		} else {
			return m_realPlayer.getEntityData();
		}
	}

	@Override
	public boolean shouldRiderSit() {
		if (m_realPlayer == null) {
			return super.shouldRiderSit();
		} else {
			return m_realPlayer.shouldRiderSit();
		}
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		if (m_realPlayer == null) {
			return super.getPickedResult(target);
		} else {
			return m_realPlayer.getPickedResult(target);
		}
	}

	@Override
	public UUID getPersistentID() {
		if (m_realPlayer == null) {
			return super.getPersistentID();
		} else {
			return m_realPlayer.getPersistentID();
		}
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		if (m_realPlayer == null) {
			return super.shouldRenderInPass(pass);
		} else {
			return m_realPlayer.shouldRenderInPass(pass);
		}
	}

	@Override
	public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
		if (m_realPlayer == null) {
			return super.isCreatureType(type, forSpawnCount);
		} else {
			return m_realPlayer.isCreatureType(type, forSpawnCount);
		}
	}

	@Override
	public boolean canRiderInteract() {
		if (m_realPlayer == null) {
			return super.canRiderInteract();
		} else {
			return m_realPlayer.canRiderInteract();
		}
	}

	@Override
	public boolean shouldDismountInWater(Entity rider) {
		if (m_realPlayer == null) {
			return super.shouldDismountInWater(rider);
		} else {
			return m_realPlayer.shouldDismountInWater(rider);
		}
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if (m_realPlayer == null) {
			super.deserializeNBT(nbt);
		} else {
			m_realPlayer.deserializeNBT(nbt);
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		if (m_realPlayer == null) {
			return super.serializeNBT();
		} else {
			return m_realPlayer.serializeNBT();
		}
	}

	@Override
	public boolean canTrample(World world, Block block, BlockPos pos, float fallDistance) {
		if (m_realPlayer == null) {
			return super.canTrample(world, block, pos, fallDistance);
		} else {
			return m_realPlayer.canTrample(world, block, pos, fallDistance);
		}
	}

	@Override
	public void addTrackingPlayer(EntityPlayerMP player) {
		if (m_realPlayer == null) {
			super.addTrackingPlayer(player);
		} else {
			m_realPlayer.addTrackingPlayer(player);
		}
	}

	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		if (m_realPlayer == null) {
			super.removeTrackingPlayer(player);
		} else {
			m_realPlayer.removeTrackingPlayer(player);
		}
	}

	@Override
	public float getRotatedYaw(Rotation transformRotation) {
		if (m_realPlayer == null) {
			return super.getRotatedYaw(transformRotation);
		} else {
			return m_realPlayer.getRotatedYaw(transformRotation);
		}
	}

	@Override
	public float getMirroredYaw(Mirror transformMirror) {
		if (m_realPlayer == null) {
			return super.getMirroredYaw(transformMirror);
		} else {
			return m_realPlayer.getMirroredYaw(transformMirror);
		}
	}

	@Override
	public boolean ignoreItemEntityData() {
		if (m_realPlayer == null) {
			return super.ignoreItemEntityData();
		} else {
			return m_realPlayer.ignoreItemEntityData();
		}
	}

	@Override
	public boolean setPositionNonDirty() {
		if (m_realPlayer == null) {
			return super.setPositionNonDirty();
		} else {
			return m_realPlayer.setPositionNonDirty();
		}
	}

	@Override
	public Entity getControllingPassenger() {
		if (m_realPlayer == null) {
			return super.getControllingPassenger();
		} else {
			return m_realPlayer.getControllingPassenger();
		}
	}

	@Override
	public List<Entity> getPassengers() {
		if (m_realPlayer == null) {
			return super.getPassengers();
		} else {
			return m_realPlayer.getPassengers();
		}
	}

	@Override
	public boolean isPassenger(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.isPassenger(entityIn);
		} else {
			return m_realPlayer.isPassenger(entityIn);
		}
	}

	@Override
	public Collection<Entity> getRecursivePassengers() {
		if (m_realPlayer == null) {
			return super.getRecursivePassengers();
		} else {
			return m_realPlayer.getRecursivePassengers();
		}
	}

	@Override
	public <T extends Entity> Collection<T> getRecursivePassengersByType(Class<T> entityClass) {
		if (m_realPlayer == null) {
			return super.getRecursivePassengersByType(entityClass);
		} else {
			return m_realPlayer.getRecursivePassengersByType(entityClass);
		}
	}

	@Override
	public Entity getLowestRidingEntity() {
		if (m_realPlayer == null) {
			return super.getLowestRidingEntity();
		} else {
			return m_realPlayer.getLowestRidingEntity();
		}
	}

	@Override
	public boolean isRidingSameEntity(Entity entityIn) {
		if (m_realPlayer == null) {
				return super.isRidingSameEntity(entityIn);
		} else {
			return m_realPlayer.isRidingSameEntity(entityIn);
		}
	}

	@Override
	public boolean isRidingOrBeingRiddenBy(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.isRidingOrBeingRiddenBy(entityIn);
		} else {
			return m_realPlayer.isRidingOrBeingRiddenBy(entityIn);
		}
	}

	@Override
	public boolean canPassengerSteer() {
		if (m_realPlayer == null) {
			return super.canPassengerSteer();
		} else {
			return m_realPlayer.canPassengerSteer();
		}
	}

	@Override
	public Entity getRidingEntity() {
		if (m_realPlayer == null) {
			return super.getRidingEntity();
		} else {
			return m_realPlayer.getRidingEntity();
		}
	}

	@Override
	public EnumPushReaction getPushReaction() {
		if (m_realPlayer == null) {
			return super.getPushReaction();
		} else {
			return m_realPlayer.getPushReaction();
		}
	}
	
	
}
