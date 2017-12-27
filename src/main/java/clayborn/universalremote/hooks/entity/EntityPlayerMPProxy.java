package clayborn.universalremote.hooks.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import clayborn.universalremote.util.InjectionHandler;
import net.minecraft.advancements.PlayerAdvancements;
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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManagerServer;
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
import net.minecraft.util.NonNullList;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class EntityPlayerMPProxy extends EntityPlayerMP {

	private EntityPlayerMP m_realPlayer;

	private ArrayList<Field> m_nonsyncingFields = new ArrayList<Field>();

	private double m_fakePosX, m_fakePosY, m_fakePosZ;
	private float m_fakePitch, m_fakeYaw;
	private int m_fakeDimension;

	private double m_realPosX, m_realPosY, m_realPosZ;
	private float m_realPitch, m_realYaw, m_realYawHead;
	private int m_realDimension;

	//public EntityPlayerMP(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn)
	public EntityPlayerMPProxy(EntityPlayerMP realPlayer, double posX, double posY, double posZ, float pitch, float yaw, int dimension)
	{
		super(realPlayer.mcServer, (WorldServer) realPlayer.world, realPlayer.getGameProfile(), realPlayer.interactionManager);

		// yeah we don't really want to inject into the interactionManager..
		realPlayer.interactionManager.player = realPlayer;

		InjectionHandler.copyAllFieldsFrom(this, realPlayer, EntityPlayerMP.class);

		this.posX = m_fakePosX = posX;
		this.posY = m_fakePosY = posY;
		this.posZ = m_fakePosZ = posZ;
		this.rotationPitch = m_fakePitch = pitch;
		this.rotationYaw = m_fakeYaw = yaw;
		this.rotationYawHead = yaw;
		this.dimension = m_fakeDimension = dimension;

		m_realPosX = realPlayer.posX;
		m_realPosY = realPlayer.posY;
		m_realPosZ = realPlayer.posZ;
		m_realPitch = realPlayer.rotationPitch;
		m_realYaw = realPlayer.rotationYaw;
		m_realYawHead = realPlayer.rotationYawHead;
		m_realDimension = realPlayer.dimension;

		m_nonsyncingFields.add(ReflectionHelper.findField(Entity.class, "posX", "field_70165_t"));
		m_nonsyncingFields.add(ReflectionHelper.findField(Entity.class, "posY", "field_70163_u"));
		m_nonsyncingFields.add(ReflectionHelper.findField(Entity.class, "posZ", "field_70161_v"));
		m_nonsyncingFields.add(ReflectionHelper.findField(Entity.class, "rotationPitch", "field_70125_A"));
		m_nonsyncingFields.add(ReflectionHelper.findField(Entity.class, "rotationYaw", "field_70177_z"));
		m_nonsyncingFields.add(ReflectionHelper.findField(EntityLivingBase.class, "rotationYawHead", "field_70759_as"));
		m_nonsyncingFields.add(ReflectionHelper.findField(Entity.class, "dimension", "field_71093_bK"));

		m_realPlayer = realPlayer;
	}

	public EntityPlayerMP getRealPlayer()
	{
		return m_realPlayer;
	}

	public void syncToRealPlayer()
	{
		// iterate over every public property of the real player and compare to this one
        InjectionHandler.IComparisonCallback callback = (Field f, Object oldValue, Object newValue) ->
        {
        	if (!m_nonsyncingFields.contains(f))
        	{
	        	//Util.logger.info("Syncing field {} of {} with {} overwriting {}", f.getName(), m_realPlayer.getClass().getName(), newValue, oldValue);
	        	InjectionHandler.copyField(f, m_realPlayer, this);
        	} else {
        		// check if the original value has been over-written
        		if (this.posX != m_fakePosX) m_realPlayer.posX = this.posX;
        		if (this.posY != m_fakePosY) m_realPlayer.posY = this.posY;
        		if (this.posZ != m_fakePosZ) m_realPlayer.posZ = this.posZ;
        		if (this.rotationPitch != m_fakePitch) m_realPlayer.posX = this.posX;
        		if (this.rotationYaw != m_fakeYaw) m_realPlayer.rotationYaw = this.rotationYaw;
        		if (this.rotationYawHead != m_fakeYaw) m_realPlayer.rotationYawHead = this.rotationYawHead;
        		if (this.dimension != m_fakeDimension) m_realPlayer.dimension = this.dimension;

        		//Util.logger.info("Syncing special syncing field {}", f.getName());
        	}
        };
        InjectionHandler.comparePublicFields(m_realPlayer, this, EntityPlayerMP.class, callback);
	}

	private <T> T syncPublicFieldsFromRealAndReturn(T value)
	{
		syncPublicFieldsFromReal();
		return value;
	}

	private void syncPublicFieldsFromReal()
	{
		// iterate over every public property of the real player and compare to this one
        InjectionHandler.IComparisonCallback callback = (Field f, Object oldValue, Object newValue) ->
        {
        	if (!m_nonsyncingFields.contains(f))
        	{
            	//Util.logger.info("Updating field {} of {} with {} overwriting {}", f.getName(), this.getClass().getName(), newValue, oldValue);
            	InjectionHandler.copyField(f, this, m_realPlayer);
        	} else {
        		// check if the original value has been over-written
        		if (m_realPlayer.posX != m_realPosX) this.posX = m_realPlayer.posX;
        		if (m_realPlayer.posY != m_realPosY) this.posY = m_realPlayer.posY;
        		if (m_realPlayer.posZ != m_realPosZ) this.posZ = m_realPlayer.posZ;
        		if (m_realPlayer.rotationPitch != m_realPitch) this.posX = m_realPlayer.posX;
        		if (m_realPlayer.rotationYaw != m_realYaw) this.rotationYaw = m_realPlayer.rotationYaw;
        		if (m_realPlayer.rotationYawHead != m_realYawHead) this.rotationYawHead = m_realPlayer.rotationYawHead;
        		if (m_realPlayer.dimension != m_realDimension) this.dimension = m_realPlayer.dimension;

        		//Util.logger.info("Updating special updating field {}", f.getName());
        	}
        };
        InjectionHandler.comparePublicFields(this, m_realPlayer, EntityPlayerMP.class, callback);
	}

	/* Modified Functions */

	@Override
	public Vec3d getLookVec() {
		return super.getLookVec();
	}

	@Override
	public Vec2f getPitchYaw() {
		return super.getPitchYaw();
	}

	@Override
	public BlockPos getPosition() {
		return super.getPosition();
	}

	@Override
	public Vec3d getPositionVector() {
		return super.getPositionVector();
	}

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

	@Override
	public Vec3d getLook(float partialTicks) {
		return super.getLook(partialTicks);
	}

	/* Proxy Functions */

	// NOTE: the if m_realPlayer == null in each function is to handle the case
	// where the super constructor calls this member function during object construction

	@Override
	public void addSelfToInternalCraftingInventory() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.addSelfToInternalCraftingInventory();
			syncPublicFieldsFromReal();
		} else {
			super.addSelfToInternalCraftingInventory();
		}
	}

	@Override
	public void onUpdateEntity() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.onUpdateEntity();
			syncPublicFieldsFromReal();
		} else {
			super.onUpdateEntity();
		}
	}

	@Override
	public void handleFalling(double y, boolean onGroundIn) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.handleFalling(y, onGroundIn);
			syncPublicFieldsFromReal();
		} else {
			super.handleFalling(y, onGroundIn);
		}
	}

	@Override
	public void getNextWindowId() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.getNextWindowId();
			syncPublicFieldsFromReal();
		} else {
			super.getNextWindowId();
		}
	}

	@Override
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.sendSlotContents(containerToSend, slotInd, stack);
			syncPublicFieldsFromReal();
		} else {
			super.sendSlotContents(containerToSend, slotInd, stack);
		}
	}

	@Override
	public void sendContainerToPlayer(Container containerIn) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.sendContainerToPlayer(containerIn);
			syncPublicFieldsFromReal();
		} else {
			super.sendContainerToPlayer(containerIn);
		}
	}

	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.sendAllContents(containerToSend, itemsList);
			syncPublicFieldsFromReal();
		} else {
			super.sendAllContents(containerToSend, itemsList);
		}
	}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.sendWindowProperty(containerIn, varToUpdate, newValue);
			syncPublicFieldsFromReal();
		} else {
			super.sendWindowProperty(containerIn, varToUpdate, newValue);
		}
	}

	@Override
	public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.sendAllWindowProperties(containerIn, inventory);
			syncPublicFieldsFromReal();
		} else {
			super.sendAllWindowProperties(containerIn, inventory);
		}
	}

	@Override
	public void updateHeldItem() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.updateHeldItem();
			syncPublicFieldsFromReal();
		} else {
			super.updateHeldItem();
		}
	}

	@Override
	public void closeContainer() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.closeContainer();
			syncPublicFieldsFromReal();
		} else {
			super.closeContainer();
		}
	}

	@Override
	public void setEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.setEntityActionState(strafe, forward, jumping, sneaking);
			syncPublicFieldsFromReal();
		} else {
			super.setEntityActionState(strafe, forward, jumping, sneaking);
		}
	}

	@Override
	public void mountEntityAndWakeUp() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.mountEntityAndWakeUp();
			syncPublicFieldsFromReal();
		} else {
			super.mountEntityAndWakeUp();
		}
	}

	@Override
	public boolean hasDisconnected() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.hasDisconnected());
		} else {
			return super.hasDisconnected();
		}
	}

	@Override
	public void setPlayerHealthUpdated() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.setPlayerHealthUpdated();
			syncPublicFieldsFromReal();
		} else {
			super.setPlayerHealthUpdated();
		}
	}

	@Override
	public void copyFrom(EntityPlayerMP that, boolean p_193104_2_) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.copyFrom(that, p_193104_2_);
			syncPublicFieldsFromReal();
		} else {
			super.copyFrom(that, p_193104_2_);
		}
	}

	@Override
	public WorldServer getServerWorld() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getServerWorld());
		} else {
			return super.getServerWorld();
		}
	}

	@Override
	public String getPlayerIP() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPlayerIP());
		} else {
			return super.getPlayerIP();
		}
	}

	@Override
	public void handleClientSettings(CPacketClientSettings packetIn) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.handleClientSettings(packetIn);
			syncPublicFieldsFromReal();
		} else {
			super.handleClientSettings(packetIn);
		}
	}

	@Override
	public EnumChatVisibility getChatVisibility() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getChatVisibility());
		} else {
			return super.getChatVisibility();
		}
	}

	@Override
	public void loadResourcePack(String url, String hash) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.loadResourcePack(url, hash);
			syncPublicFieldsFromReal();
		} else {
			super.loadResourcePack(url, hash);
		}
	}

	@Override
	public void markPlayerActive() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.markPlayerActive();
			syncPublicFieldsFromReal();
		} else {
			super.markPlayerActive();
		}
	}

	@Override
	public StatisticsManagerServer getStatFile() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getStatFile());
		} else {
			return super.getStatFile();
		}
	}

	@Override
	public RecipeBookServer getRecipeBook() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRecipeBook());
		} else {
			return super.getRecipeBook();
		}
	}

	@Override
	public void removeEntity(Entity entityIn) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.removeEntity(entityIn);
			syncPublicFieldsFromReal();
		} else {
			super.removeEntity(entityIn);
		}
	}

	@Override
	public void addEntity(Entity entityIn) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.addEntity(entityIn);
			syncPublicFieldsFromReal();
		} else {
			super.addEntity(entityIn);
		}
	}

	@Override
	public Entity getSpectatingEntity() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getSpectatingEntity());
		} else {
			return super.getSpectatingEntity();
		}
	}

	@Override
	public void setSpectatingEntity(Entity entityToSpectate) {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.setSpectatingEntity(entityToSpectate);
			syncPublicFieldsFromReal();
		} else {
			super.setSpectatingEntity(entityToSpectate);
		}
	}

	@Override
	public long getLastActiveTime() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getLastActiveTime());
		} else {
			return super.getLastActiveTime();
		}
	}

	@Override
	public ITextComponent getTabListDisplayName() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getTabListDisplayName());
		} else {
			return super.getTabListDisplayName();
		}
	}

	@Override
	public boolean isInvulnerableDimensionChange() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return m_realPlayer.isInvulnerableDimensionChange();
		} else {
			return super.isInvulnerableDimensionChange();
		}
	}

	@Override
	public void clearInvulnerableDimensionChange() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.clearInvulnerableDimensionChange();
			syncPublicFieldsFromReal();
		} else {
			super.clearInvulnerableDimensionChange();
		}
	}

	@Override
	public void setElytraFlying() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.setElytraFlying();
			syncPublicFieldsFromReal();
		} else {
			super.setElytraFlying();
		}
	}

	@Override
	public void clearElytraFlying() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			m_realPlayer.clearElytraFlying();
			syncPublicFieldsFromReal();
		} else {
			super.clearElytraFlying();
		}
	}

	@Override
	public PlayerAdvancements getAdvancements() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAdvancements());
		} else {
			return super.getAdvancements();
		}
	}

	@Override
	public Vec3d getEnteredNetherPosition() {
		if(m_realPlayer != null) {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getEnteredNetherPosition());
		} else {
			return super.getEnteredNetherPosition();
		}
	}

	@Override
	public World getEntityWorld() {
		if (m_realPlayer == null) {
			return super.getEntityWorld();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getEntityWorld());
		}
	}

	@Override
	public void onUpdate() {
		if (m_realPlayer == null) {
				super.onUpdate();
		} else {
			syncToRealPlayer();
			m_realPlayer.onUpdate();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int getMaxInPortalTime() {
		if (m_realPlayer == null) {
			return super.getMaxInPortalTime();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getMaxInPortalTime());
		}
	}

	@Override
	public int getPortalCooldown() {
		if (m_realPlayer == null) {
			return super.getPortalCooldown();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPortalCooldown());
		}
	}

	@Override
	public void playSound(SoundEvent soundIn, float volume, float pitch) {
		if (m_realPlayer == null) {
			super.playSound(soundIn, volume, pitch);
		} else {
			syncToRealPlayer();
			m_realPlayer.playSound(soundIn, volume, pitch);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public SoundCategory getSoundCategory() {
		if (m_realPlayer == null) {
			return super.getSoundCategory();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getSoundCategory());
		}
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (m_realPlayer == null) {
			super.handleStatusUpdate(id);
		} else {
			syncToRealPlayer();
			m_realPlayer.handleStatusUpdate(id);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void closeScreen() {
		if (m_realPlayer == null) {
			super.closeScreen();
		} else {
			syncToRealPlayer();
			m_realPlayer.closeScreen();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void updateRidden() {
		if (m_realPlayer == null) {
			super.updateRidden();
		} else {
			syncToRealPlayer();
			m_realPlayer.updateRidden();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void preparePlayerToSpawn() {
		if (m_realPlayer == null) {
			super.preparePlayerToSpawn();
		} else {
			syncToRealPlayer();
			m_realPlayer.preparePlayerToSpawn();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onLivingUpdate() {
		if (m_realPlayer == null) {
			super.onLivingUpdate();
		} else {
			syncToRealPlayer();
			m_realPlayer.onLivingUpdate();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int getScore() {
		if (m_realPlayer == null) {
			return super.getScore();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getScore());
		}
	}

	@Override
	public void setScore(int scoreIn) {
		if (m_realPlayer == null) {
			super.setScore(scoreIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.setScore(scoreIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void addScore(int scoreIn) {
		if (m_realPlayer == null) {
			super.addScore(scoreIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.addScore(scoreIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (m_realPlayer == null) {
			super.onDeath(cause);
		} else {
			syncToRealPlayer();
			m_realPlayer.onDeath(cause);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public EntityItem dropItem(boolean dropAll) {
		if (m_realPlayer == null) {
			return super.dropItem(dropAll);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.dropItem(dropAll));
		}
	}

	@Override
	public EntityItem dropItem(ItemStack itemStackIn, boolean unused) {
		if (m_realPlayer == null) {
			return super.dropItem(itemStackIn, unused);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.dropItem(itemStackIn, unused));
		}
	}

	@Override
	public EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
		if (m_realPlayer == null) {
			return super.dropItem(droppedItem, dropAround, traceItem);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.dropItem(droppedItem, dropAround, traceItem));
		}
	}

	@Override
	public ItemStack dropItemAndGetStack(EntityItem p_184816_1_) {
		if (m_realPlayer == null) {
			return super.dropItemAndGetStack(p_184816_1_);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.dropItemAndGetStack(p_184816_1_));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public float getDigSpeed(IBlockState state) {
		if (m_realPlayer == null) {
			return super.getDigSpeed(state);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getDigSpeed(state));
		}
	}

	@Override
	public float getDigSpeed(IBlockState state, BlockPos pos) {
		if (m_realPlayer == null) {
			return super.getDigSpeed(state, pos);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getDigSpeed(state, pos));
		}
	}

	@Override
	public boolean canHarvestBlock(IBlockState state) {
		if (m_realPlayer == null) {
			return super.canHarvestBlock(state);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canHarvestBlock(state));
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			super.readEntityFromNBT(compound);
		} else {
			syncToRealPlayer();
			m_realPlayer.readEntityFromNBT(compound);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			super.writeEntityToNBT(compound);
		} else {
			syncToRealPlayer();
			m_realPlayer.writeEntityToNBT(compound);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (m_realPlayer == null) {
			return super.attackEntityFrom(source, amount);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.attackEntityFrom(source, amount));
		}
	}

	@Override
	public boolean canAttackPlayer(EntityPlayer other) {
		if (m_realPlayer == null) {
			return super.canAttackPlayer(other);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canAttackPlayer(other));
		}
	}

	@Override
	public float getArmorVisibility() {
		if (m_realPlayer == null) {
			return super.getArmorVisibility();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getArmorVisibility());
		}
	}

	@Override
	public void openEditSign(TileEntitySign signTile) {
		if (m_realPlayer == null) {
			super.openEditSign(signTile);
		} else {
			syncToRealPlayer();
			m_realPlayer.openEditSign(signTile);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void displayGuiEditCommandCart(CommandBlockBaseLogic commandBlock) {
		if (m_realPlayer == null) {
			super.displayGuiEditCommandCart(commandBlock);
		} else {
			syncToRealPlayer();
			m_realPlayer.displayGuiEditCommandCart(commandBlock);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void displayGuiCommandBlock(TileEntityCommandBlock commandBlock) {
		if (m_realPlayer == null) {
			super.displayGuiCommandBlock(commandBlock);
		} else {
			syncToRealPlayer();
			m_realPlayer.displayGuiCommandBlock(commandBlock);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void openEditStructure(TileEntityStructure structure) {
		if (m_realPlayer == null) {
			super.openEditStructure(structure);
		} else {
			syncToRealPlayer();
			m_realPlayer.openEditStructure(structure);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void displayVillagerTradeGui(IMerchant villager) {
		if (m_realPlayer == null) {
			super.displayVillagerTradeGui(villager);
		} else {
			syncToRealPlayer();
			m_realPlayer.displayVillagerTradeGui(villager);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void displayGUIChest(IInventory chestInventory) {
		if (m_realPlayer == null) {
			super.displayGUIChest(chestInventory);
		} else {
			syncToRealPlayer();
			m_realPlayer.displayGUIChest(chestInventory);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void openGuiHorseInventory(AbstractHorse horse, IInventory inventoryIn) {
		if (m_realPlayer == null) {
			super.openGuiHorseInventory(horse, inventoryIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.openGuiHorseInventory(horse, inventoryIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void displayGui(IInteractionObject guiOwner) {
		if (m_realPlayer == null) {
			super.displayGui(guiOwner);
		} else {
			syncToRealPlayer();
			m_realPlayer.displayGui(guiOwner);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void openBook(ItemStack stack, EnumHand hand) {
		if (m_realPlayer == null) {
			super.openBook(stack, hand);
		} else {
			syncToRealPlayer();
			m_realPlayer.openBook(stack, hand);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public EnumActionResult interactOn(Entity p_190775_1_, EnumHand p_190775_2_) {
		if (m_realPlayer == null) {
			return super.interactOn(p_190775_1_, p_190775_2_);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.interactOn(p_190775_1_, p_190775_2_));
		}
	}

	@Override
	public double getYOffset() {
		if (m_realPlayer == null) {
			return super.getYOffset();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getYOffset());
		}
	}

	@Override
	public void dismountRidingEntity() {
		if (m_realPlayer == null) {
			super.dismountRidingEntity();
		} else {
			syncToRealPlayer();
			m_realPlayer.dismountRidingEntity();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
		if (m_realPlayer == null) {
			super.attackTargetEntityWithCurrentItem(targetEntity);
		} else {
			syncToRealPlayer();
			m_realPlayer.attackTargetEntityWithCurrentItem(targetEntity);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void disableShield(boolean p_190777_1_) {
		if (m_realPlayer == null) {
			super.disableShield(p_190777_1_);
		} else {
			syncToRealPlayer();
			m_realPlayer.disableShield(p_190777_1_);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onCriticalHit(Entity entityHit) {
		if (m_realPlayer == null) {
			super.onCriticalHit(entityHit);
		} else {
			syncToRealPlayer();
			m_realPlayer.onCriticalHit(entityHit);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onEnchantmentCritical(Entity entityHit) {
		if (m_realPlayer == null) {
			super.onEnchantmentCritical(entityHit);
		} else {
			syncToRealPlayer();
			m_realPlayer.onEnchantmentCritical(entityHit);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void spawnSweepParticles() {
		if (m_realPlayer == null) {
			super.spawnSweepParticles();
		} else {
			syncToRealPlayer();
			m_realPlayer.spawnSweepParticles();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void respawnPlayer() {
		if (m_realPlayer == null) {
			super.respawnPlayer();
		} else {
			syncToRealPlayer();
			m_realPlayer.respawnPlayer();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setDead() {
		if (m_realPlayer == null) {
			super.setDead();
		} else {
			syncToRealPlayer();
			m_realPlayer.setDead();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isEntityInsideOpaqueBlock() {
		if (m_realPlayer == null) {
			return super.isEntityInsideOpaqueBlock();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isEntityInsideOpaqueBlock();
		}
	}

	@Override
	public boolean isUser() {
		if (m_realPlayer == null) {
			return super.isUser();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isUser();
		}
	}

	@Override
	public GameProfile getGameProfile() {
		if (m_realPlayer == null) {
			return super.getGameProfile();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getGameProfile());
		}
	}

	@Override
	public SleepResult trySleep(BlockPos bedLocation) {
		if (m_realPlayer == null) {
			return super.trySleep(bedLocation);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.trySleep(bedLocation));
		}
	}

	@Override
	public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
		if (m_realPlayer == null) {
			super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);
		} else {
			syncToRealPlayer();
			m_realPlayer.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public float getBedOrientationInDegrees() {
		if (m_realPlayer == null) {
			return super.getBedOrientationInDegrees();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getBedOrientationInDegrees());
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
			syncToRealPlayer();
			return m_realPlayer.isPlayerFullyAsleep();
		}
	}

	@Override
	public int getSleepTimer() {
		if (m_realPlayer == null) {
			return super.getSleepTimer();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getSleepTimer());
		}
	}

	@Override
	public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {
		if (m_realPlayer == null) {
			super.sendStatusMessage(chatComponent, actionBar);
		} else {
			syncToRealPlayer();
			m_realPlayer.sendStatusMessage(chatComponent, actionBar);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public BlockPos getBedLocation() {
		if (m_realPlayer == null) {
			return super.getBedLocation();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getBedLocation());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isSpawnForced() {
		if (m_realPlayer == null) {
			return super.isSpawnForced();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isSpawnForced();
		}
	}

	@Override
	public void setSpawnPoint(BlockPos pos, boolean forced) {
		if (m_realPlayer == null) {
			super.setSpawnPoint(pos, forced);
		} else {
			syncToRealPlayer();
			m_realPlayer.setSpawnPoint(pos, forced);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void addStat(StatBase stat) {
		if (m_realPlayer == null) {
			super.addStat(stat);
		} else {
			syncToRealPlayer();
			m_realPlayer.addStat(stat);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void addStat(StatBase stat, int amount) {
		if (m_realPlayer == null) {
			super.addStat(stat, amount);
		} else {
			syncToRealPlayer();
			m_realPlayer.addStat(stat, amount);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void takeStat(StatBase stat) {
		if (m_realPlayer == null) {
			super.takeStat(stat);
		} else {
			syncToRealPlayer();
			m_realPlayer.takeStat(stat);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void unlockRecipes(List<IRecipe> p_192021_1_) {
		if (m_realPlayer == null) {
			super.unlockRecipes(p_192021_1_);
		} else {
			syncToRealPlayer();
			m_realPlayer.unlockRecipes(p_192021_1_);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void unlockRecipes(ResourceLocation[] p_193102_1_) {
		if (m_realPlayer == null) {
			super.unlockRecipes(p_193102_1_);
		} else {
			syncToRealPlayer();
			m_realPlayer.unlockRecipes(p_193102_1_);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void resetRecipes(List<IRecipe> p_192022_1_) {
		if (m_realPlayer == null) {
			super.resetRecipes(p_192022_1_);
		} else {
			syncToRealPlayer();
			m_realPlayer.resetRecipes(p_192022_1_);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void jump() {
		if (m_realPlayer == null) {
			super.jump();
		} else {
			syncToRealPlayer();
			m_realPlayer.jump();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void travel(float p_191986_1_, float p_191986_2_, float p_191986_3_) {
		if (m_realPlayer == null) {
			super.travel(p_191986_1_, p_191986_2_, p_191986_3_);
		} else {
			syncToRealPlayer();
			m_realPlayer.travel(p_191986_1_, p_191986_2_, p_191986_3_);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public float getAIMoveSpeed() {
		if (m_realPlayer == null) {
			return super.getAIMoveSpeed();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAIMoveSpeed());
		}
	}

	@Override
	public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_) {
		if (m_realPlayer == null) {
			super.addMovementStat(p_71000_1_, p_71000_3_, p_71000_5_);
		} else {
			syncToRealPlayer();
			m_realPlayer.addMovementStat(p_71000_1_, p_71000_3_, p_71000_5_);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		if (m_realPlayer == null) {
			super.fall(distance, damageMultiplier);
		} else {
			syncToRealPlayer();
			m_realPlayer.fall(distance, damageMultiplier);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onKillEntity(EntityLivingBase entityLivingIn) {
		if (m_realPlayer == null) {
			super.onKillEntity(entityLivingIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.onKillEntity(entityLivingIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setInWeb() {
		if (m_realPlayer == null) {
			super.setInWeb();
		} else {
			syncToRealPlayer();
			m_realPlayer.setInWeb();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void addExperience(int amount) {
		if (m_realPlayer == null) {
			super.addExperience(amount);
		} else {
			syncToRealPlayer();
			m_realPlayer.addExperience(amount);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int getXPSeed() {
		if (m_realPlayer == null) {
			return super.getXPSeed();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getXPSeed());
		}
	}

	@Override
	public void onEnchant(ItemStack enchantedItem, int cost) {
		if (m_realPlayer == null) {
			super.onEnchant(enchantedItem, cost);
		} else {
			syncToRealPlayer();
			m_realPlayer.onEnchant(enchantedItem, cost);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void addExperienceLevel(int levels) {
		if (m_realPlayer == null) {
			super.addExperienceLevel(levels);
		} else {
			syncToRealPlayer();
			m_realPlayer.addExperienceLevel(levels);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int xpBarCap() {
		if (m_realPlayer == null) {
			return super.xpBarCap();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.xpBarCap());
		}
	}

	@Override
	public void addExhaustion(float exhaustion) {
		if (m_realPlayer == null) {
			super.addExhaustion(exhaustion);
		} else {
			syncToRealPlayer();
			m_realPlayer.addExhaustion(exhaustion);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public FoodStats getFoodStats() {
		if (m_realPlayer == null) {
			return super.getFoodStats();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getFoodStats());
		}
	}

	@Override
	public boolean canEat(boolean ignoreHunger) {
		if (m_realPlayer == null) {
			return super.canEat(ignoreHunger);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canEat(ignoreHunger));
		}
	}

	@Override
	public boolean shouldHeal() {
		if (m_realPlayer == null) {
			return super.shouldHeal();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.shouldHeal());
		}
	}

	@Override
	public boolean isAllowEdit() {
		if (m_realPlayer == null) {
			return super.isAllowEdit();
		} else {
			syncToRealPlayer();
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
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAlwaysRenderNameTagForRender());
		}
	}

	@Override
	public void sendPlayerAbilities() {
		if (m_realPlayer == null) {
			super.sendPlayerAbilities();
		} else {
			syncToRealPlayer();
			m_realPlayer.sendPlayerAbilities();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setGameType(GameType gameType) {
		if (m_realPlayer == null) {
			super.setGameType(gameType);
		} else {
			syncToRealPlayer();
			m_realPlayer.setGameType(gameType);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public String getName() {
		if (m_realPlayer == null) {
			return super.getName();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getName());
		}
	}

	@Override
	public InventoryEnderChest getInventoryEnderChest() {
		if (m_realPlayer == null) {
			return super.getInventoryEnderChest();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getInventoryEnderChest());
		}
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
		if (m_realPlayer == null) {
			return super.getItemStackFromSlot(slotIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getItemStackFromSlot(slotIn));
		}
	}

	@Override
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
		if (m_realPlayer == null) {
			super.setItemStackToSlot(slotIn, stack);
		} else {
			syncToRealPlayer();
			m_realPlayer.setItemStackToSlot(slotIn, stack);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean addItemStackToInventory(ItemStack p_191521_1_) {
		if (m_realPlayer == null) {
			return super.addItemStackToInventory(p_191521_1_);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.addItemStackToInventory(p_191521_1_));
		}
	}

	@Override
	public Iterable<ItemStack> getHeldEquipment() {
		if (m_realPlayer == null) {
			return super.getHeldEquipment();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getHeldEquipment());
		}
	}

	@Override
	public Iterable<ItemStack> getArmorInventoryList() {
		if (m_realPlayer == null) {
			return super.getArmorInventoryList();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getArmorInventoryList());
		}
	}

	@Override
	public boolean addShoulderEntity(NBTTagCompound p_192027_1_) {
		if (m_realPlayer == null) {
			return super.addShoulderEntity(p_192027_1_);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.addShoulderEntity(p_192027_1_));
		}
	}

	@Override
	public boolean isInvisibleToPlayer(EntityPlayer player) {
		if (m_realPlayer == null) {
			return super.isInvisibleToPlayer(player);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isInvisibleToPlayer(player);
		}
	}

	@Override
	public boolean isSpectator() {
		if (m_realPlayer == null) {
			return false;
		} else {
			syncToRealPlayer();
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
			syncToRealPlayer();
			return m_realPlayer.isPushedByWater();
		}
	}

	@Override
	public Scoreboard getWorldScoreboard() {
		if (m_realPlayer == null) {
			return super.getWorldScoreboard();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getWorldScoreboard());
		}
	}

	@Override
	public Team getTeam() {
		if (m_realPlayer == null) {
			return super.getTeam();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getTeam());
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		if (m_realPlayer == null) {
			return super.getDisplayName();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getDisplayName());
		}
	}

	@Override
	public float getEyeHeight() {
		if (m_realPlayer == null) {
			return super.getEyeHeight();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getEyeHeight());
		}
	}

	@Override
	public void setAbsorptionAmount(float amount) {
		if (m_realPlayer == null) {
			super.setAbsorptionAmount(amount);
		} else {
			syncToRealPlayer();
			m_realPlayer.setAbsorptionAmount(amount);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public float getAbsorptionAmount() {
		if (m_realPlayer == null) {
			return super.getAbsorptionAmount();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAbsorptionAmount());
		}
	}

	@Override
	public boolean canOpen(LockCode code) {
		if (m_realPlayer == null) {
			return super.canOpen(code);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canOpen(code));
		}
	}

	@Override
	public boolean isWearing(EnumPlayerModelParts part) {
		if (m_realPlayer == null) {
			return super.isWearing(part);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isWearing(part);
		}
	}

	@Override
	public boolean sendCommandFeedback() {
		if (m_realPlayer == null) {
			return super.sendCommandFeedback();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.sendCommandFeedback());
		}
	}

	@Override
	public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
		if (m_realPlayer == null) {
			return super.replaceItemInInventory(inventorySlot, itemStackIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.replaceItemInInventory(inventorySlot, itemStackIn));
		}
	}

	@Override
	public boolean hasReducedDebug() {
		if (m_realPlayer == null) {
			return super.hasReducedDebug();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.hasReducedDebug());
		}
	}

	@Override
	public void setReducedDebug(boolean reducedDebug) {
		if (m_realPlayer == null) {
			super.setReducedDebug(reducedDebug);
		} else {
			syncToRealPlayer();
			m_realPlayer.setReducedDebug(reducedDebug);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public EnumHandSide getPrimaryHand() {
		if (m_realPlayer == null) {
			return super.getPrimaryHand();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPrimaryHand());
		}
	}

	@Override
	public void setPrimaryHand(EnumHandSide hand) {
		if (m_realPlayer == null) {
			super.setPrimaryHand(hand);
		} else {
			syncToRealPlayer();
			m_realPlayer.setPrimaryHand(hand);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public NBTTagCompound getLeftShoulderEntity() {
		if (m_realPlayer == null) {
			return super.getLeftShoulderEntity();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getLeftShoulderEntity());
		}
	}

	@Override
	public NBTTagCompound getRightShoulderEntity() {
		if (m_realPlayer == null) {
			return super.getRightShoulderEntity();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRightShoulderEntity());
		}
	}

	@Override
	public float getCooldownPeriod() {
		if (m_realPlayer == null) {
			return super.getCooldownPeriod();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCooldownPeriod());
		}
	}

	@Override
	public float getCooledAttackStrength(float adjustTicks) {
		if (m_realPlayer == null) {
			return super.getCooledAttackStrength(adjustTicks);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCooledAttackStrength(adjustTicks));
		}
	}

	@Override
	public void resetCooldown() {
		if (m_realPlayer == null) {
			super.resetCooldown();
		} else {
			syncToRealPlayer();
			m_realPlayer.resetCooldown();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public CooldownTracker getCooldownTracker() {
		if (m_realPlayer == null) {
			return super.getCooldownTracker();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCooldownTracker());
		}
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		if (m_realPlayer == null) {
			super.applyEntityCollision(entityIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.applyEntityCollision(entityIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public float getLuck() {
		if (m_realPlayer == null) {
			return super.getLuck();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getLuck());
		}
	}

	@Override
	public boolean canUseCommandBlock() {
		if (m_realPlayer == null) {
			return super.canUseCommandBlock();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canUseCommandBlock());
		}
	}

	@Override
	public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
		if (m_realPlayer == null) {
			super.openGui(mod, modGuiId, world, x, y, z);
		} else {
 			syncToRealPlayer();
			m_realPlayer.openGui(mod, modGuiId, world, x, y, z);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public BlockPos getBedLocation(int dimension) {
		if (m_realPlayer == null) {
			return super.getBedLocation(dimension);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getBedLocation(dimension));
		}
	}

	@Override
	public boolean isSpawnForced(int dimension) {
		if (m_realPlayer == null) {
			return super.isSpawnForced(dimension);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isSpawnForced(dimension);
		}
	}

	@Override
	public void setSpawnChunk(BlockPos pos, boolean forced, int dimension) {
		if (m_realPlayer == null) {
			super.setSpawnChunk(pos, forced, dimension);
		} else {
			syncToRealPlayer();
			m_realPlayer.setSpawnChunk(pos, forced, dimension);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public float getDefaultEyeHeight() {
		if (m_realPlayer == null) {
			return super.getDefaultEyeHeight();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getDefaultEyeHeight());
		}
	}

	@Override
	public String getDisplayNameString() {
		if (m_realPlayer == null) {
			return super.getDisplayNameString();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getDisplayNameString());
		}
	}

	@Override
	public void refreshDisplayName() {
		if (m_realPlayer == null) {
			super.refreshDisplayName();
		} else {
			syncToRealPlayer();
			m_realPlayer.refreshDisplayName();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void addPrefix(ITextComponent prefix) {
		if (m_realPlayer == null) {
			super.addPrefix(prefix);
		} else {
			syncToRealPlayer();
			m_realPlayer.addPrefix(prefix);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void addSuffix(ITextComponent suffix) {
		if (m_realPlayer == null) {
			super.addSuffix(suffix);
		} else {
			syncToRealPlayer();
			m_realPlayer.addSuffix(suffix);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public Collection<ITextComponent> getPrefixes() {
		if (m_realPlayer == null) {
			return super.getPrefixes();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPrefixes());
		}
	}

	@Override
	public Collection<ITextComponent> getSuffixes() {
		if (m_realPlayer == null) {
			return super.getSuffixes();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getSuffixes());
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (m_realPlayer == null) {
			return super.getCapability(capability, facing);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCapability(capability, facing));
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (m_realPlayer == null) {
			return super.hasCapability(capability, facing);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.hasCapability(capability, facing));
		}
	}

	@Override
	public boolean hasSpawnDimension() {
		if (m_realPlayer == null) {
			return super.hasSpawnDimension();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.hasSpawnDimension());
		}
	}

	@Override
	public int getSpawnDimension() {
		if (m_realPlayer == null) {
			return super.getSpawnDimension();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getSpawnDimension());
		}
	}

	@Override
	public void setSpawnDimension(Integer dimension) {
		if (m_realPlayer == null) {
			super.setSpawnDimension(dimension);
		} else {
			syncToRealPlayer();
			m_realPlayer.setSpawnDimension(dimension);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onKillCommand() {
		if (m_realPlayer == null) {
			super.onKillCommand();
		} else {
			syncToRealPlayer();
			m_realPlayer.onKillCommand();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean canBreatheUnderwater() {
		if (m_realPlayer == null) {
			return super.canBreatheUnderwater();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canBreatheUnderwater());
		}
	}

	@Override
	public void onEntityUpdate() {
		if (m_realPlayer == null) {
			super.onEntityUpdate();
		} else {
			syncToRealPlayer();
			m_realPlayer.onEntityUpdate();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isChild() {
		if (m_realPlayer == null) {
			return super.isChild();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isChild();
		}
	}

	@Override
	public Random getRNG() {
		if (m_realPlayer == null) {
			return super.getRNG();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRNG());
		}
	}

	@Override
	public EntityLivingBase getRevengeTarget() {
		if (m_realPlayer == null) {
			return super.getRevengeTarget();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRevengeTarget());
		}
	}

	@Override
	public int getRevengeTimer() {
		if (m_realPlayer == null) {
			return super.getRevengeTimer();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRevengeTimer());
		}
	}

	@Override
	public void setRevengeTarget(EntityLivingBase livingBase) {
		if (m_realPlayer == null) {
			super.setRevengeTarget(livingBase);
		} else {
			syncToRealPlayer();
			m_realPlayer.setRevengeTarget(livingBase);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public EntityLivingBase getLastAttackedEntity() {
		if (m_realPlayer == null) {
			return super.getLastAttackedEntity();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getLastAttackedEntity());
		}
	}

	@Override
	public int getLastAttackedEntityTime() {
		if (m_realPlayer == null) {
			return super.getLastAttackedEntityTime();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getLastAttackedEntityTime());
		}
	}

	@Override
	public void setLastAttackedEntity(Entity entityIn) {
		if (m_realPlayer == null) {
			super.setLastAttackedEntity(entityIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.setLastAttackedEntity(entityIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int getIdleTime() {
		if (m_realPlayer == null) {
			return super.getIdleTime();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getIdleTime());
		}
	}

	@Override
	public void clearActivePotions() {
		if (m_realPlayer == null) {
			super.clearActivePotions();
		} else {
			syncToRealPlayer();
			m_realPlayer.clearActivePotions();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		if (m_realPlayer == null) {
			return super.getActivePotionEffects();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getActivePotionEffects());
		}
	}

	@Override
	public Map<Potion, PotionEffect> getActivePotionMap() {
		if (m_realPlayer == null) {
			return super.getActivePotionMap();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getActivePotionMap());
		}
	}

	@Override
	public boolean isPotionActive(Potion potionIn) {
		if (m_realPlayer == null) {
			return super.isPotionActive(potionIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isPotionActive(potionIn);
		}
	}

	@Override
	public PotionEffect getActivePotionEffect(Potion potionIn) {
		if (m_realPlayer == null) {
			return super.getActivePotionEffect(potionIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getActivePotionEffect(potionIn));
		}
	}

	@Override
	public void addPotionEffect(PotionEffect potioneffectIn) {
		if (m_realPlayer == null) {
			super.addPotionEffect(potioneffectIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.addPotionEffect(potioneffectIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isPotionApplicable(PotionEffect potioneffectIn) {
		if (m_realPlayer == null) {
			return super.isPotionApplicable(potioneffectIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isPotionApplicable(potioneffectIn);
		}
	}

	@Override
	public boolean isEntityUndead() {
		if (m_realPlayer == null) {
			return super.isEntityUndead();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isEntityUndead();
		}
	}

	@Override
	public PotionEffect removeActivePotionEffect(Potion potioneffectin) {
		if (m_realPlayer == null) {
			return super.removeActivePotionEffect(potioneffectin);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.removeActivePotionEffect(potioneffectin));
		}
	}

	@Override
	public void removePotionEffect(Potion potionIn) {
		if (m_realPlayer == null) {
			super.removePotionEffect(potionIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.removePotionEffect(potionIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void heal(float healAmount) {
		if (m_realPlayer == null) {
			super.heal(healAmount);
		} else {
			syncToRealPlayer();
			m_realPlayer.heal(healAmount);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setHealth(float health) {
		if (m_realPlayer == null) {
			super.setHealth(health);
		} else {
			syncToRealPlayer();
			m_realPlayer.setHealth(health);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public DamageSource getLastDamageSource() {
		if (m_realPlayer == null) {
			return super.getLastDamageSource();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getLastDamageSource());
		}
	}

	@Override
	public void renderBrokenItemStack(ItemStack stack) {
		if (m_realPlayer == null) {
			super.renderBrokenItemStack(stack);
		} else {
			syncToRealPlayer();
			m_realPlayer.renderBrokenItemStack(stack);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
		if (m_realPlayer == null) {
			super.knockBack(entityIn, strength, xRatio, zRatio);
		} else {
			syncToRealPlayer();
			m_realPlayer.knockBack(entityIn, strength, xRatio, zRatio);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isOnLadder() {
		if (m_realPlayer == null) {
			return super.isOnLadder();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isOnLadder();
		}
	}

	@Override
	public boolean isEntityAlive() {
		if (m_realPlayer == null) {
			return super.isEntityAlive();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isEntityAlive();
		}
	}

	@Override
	public void performHurtAnimation() {
		if (m_realPlayer == null) {
			super.performHurtAnimation();
		} else {
			syncToRealPlayer();
			m_realPlayer.performHurtAnimation();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int getTotalArmorValue() {
		if (m_realPlayer == null) {
			return super.getTotalArmorValue();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getTotalArmorValue());
		}
	}

	@Override
	public CombatTracker getCombatTracker() {
		if (m_realPlayer == null) {
			return super.getCombatTracker();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCombatTracker());
		}
	}

	@Override
	public EntityLivingBase getAttackingEntity() {
		if (m_realPlayer == null) {
			return super.getAttackingEntity();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAttackingEntity());
		}
	}

	@Override
	public void swingArm(EnumHand hand) {
		if (m_realPlayer == null) {
			super.swingArm(hand);
		} else {
			syncToRealPlayer();
			m_realPlayer.swingArm(hand);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public IAttributeInstance getEntityAttribute(IAttribute attribute) {
		if (m_realPlayer == null) {
			return super.getEntityAttribute(attribute);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getEntityAttribute(attribute));
		}
	}

	@Override
	public AbstractAttributeMap getAttributeMap() {
		if (m_realPlayer == null) {
			return super.getAttributeMap();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAttributeMap());
		}
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		if (m_realPlayer == null) {
			return super.getCreatureAttribute();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCreatureAttribute());
		}
	}

	@Override
	public ItemStack getHeldItemMainhand() {
		if (m_realPlayer == null) {
			return super.getHeldItemMainhand();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getHeldItemMainhand());
		}
	}

	@Override
	public ItemStack getHeldItemOffhand() {
		if (m_realPlayer == null) {
			return super.getHeldItemOffhand();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getHeldItemOffhand());
		}
	}

	@Override
	public ItemStack getHeldItem(EnumHand hand) {
		if (m_realPlayer == null) {
			return super.getHeldItem(hand);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getHeldItem(hand));
		}
	}

	@Override
	public void setHeldItem(EnumHand hand, ItemStack stack) {
		if (m_realPlayer == null) {
			super.setHeldItem(hand, stack);
		} else {
			syncToRealPlayer();
			m_realPlayer.setHeldItem(hand, stack);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean hasItemInSlot(EntityEquipmentSlot p_190630_1_) {
		if (m_realPlayer == null) {
			return super.hasItemInSlot(p_190630_1_);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.hasItemInSlot(p_190630_1_));
		}
	}

	@Override
	public void setSprinting(boolean sprinting) {
		if (m_realPlayer == null) {
			super.setSprinting(sprinting);
		} else {
			syncToRealPlayer();
			m_realPlayer.setSprinting(sprinting);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void dismountEntity(Entity entityIn) {
		if (m_realPlayer == null) {
			super.dismountEntity(entityIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.dismountEntity(entityIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setAIMoveSpeed(float speedIn) {
		if (m_realPlayer == null) {
			super.setAIMoveSpeed(speedIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.setAIMoveSpeed(speedIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.attackEntityAsMob(entityIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.attackEntityAsMob(entityIn));
		}
	}

	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
			int posRotationIncrements, boolean teleport) {
		if (m_realPlayer == null) {
			super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
		} else {
			syncToRealPlayer();
			m_realPlayer.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setJumping(boolean jumping) {
		if (m_realPlayer == null) {
			super.setJumping(jumping);
		} else {
			syncToRealPlayer();
			m_realPlayer.setJumping(jumping);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onItemPickup(Entity entityIn, int quantity) {
		if (m_realPlayer == null) {
			super.onItemPickup(entityIn, quantity);
		} else {
			syncToRealPlayer();
			m_realPlayer.onItemPickup(entityIn, quantity);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean canEntityBeSeen(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.canEntityBeSeen(entityIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canEntityBeSeen(entityIn));
		}
	}

	@Override
	public float getSwingProgress(float partialTickTime) {
		if (m_realPlayer == null) {
			return super.getSwingProgress(partialTickTime);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getSwingProgress(partialTickTime));
		}
	}

	@Override
	public boolean isServerWorld() {
		if (m_realPlayer == null) {
			return super.isServerWorld();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isServerWorld();
		}
	}

	@Override
	public boolean canBeCollidedWith() {
		if (m_realPlayer == null) {
			return super.canBeCollidedWith();
		} else {
			syncToRealPlayer();
			return m_realPlayer.canBeCollidedWith();
		}
	}

	@Override
	public boolean canBePushed() {
		if (m_realPlayer == null) {
			return super.canBePushed();
		} else {
			syncToRealPlayer();
			return m_realPlayer.canBePushed();
		}
	}

	@Override
	public float getRotationYawHead() {
		if (m_realPlayer == null) {
			return super.getRotationYawHead();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRotationYawHead());
		}
	}

	@Override
	public void setRotationYawHead(float rotation) {
		if (m_realPlayer == null) {
			super.setRotationYawHead(rotation);
		} else {
			syncToRealPlayer();
			m_realPlayer.setRotationYawHead(rotation);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setRenderYawOffset(float offset) {
		if (m_realPlayer == null) {
			super.setRenderYawOffset(offset);
		} else {
			syncToRealPlayer();
			m_realPlayer.setRenderYawOffset(offset);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void sendEnterCombat() {
		if (m_realPlayer == null) {
			super.sendEnterCombat();
		} else {
			syncToRealPlayer();
			m_realPlayer.sendEnterCombat();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void sendEndCombat() {
		if (m_realPlayer == null) {
			super.sendEndCombat();
		} else {
			syncToRealPlayer();
			m_realPlayer.sendEndCombat();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void curePotionEffects(ItemStack curativeItem) {
		if (m_realPlayer == null) {
			super.curePotionEffects(curativeItem);
		} else {
			syncToRealPlayer();
			m_realPlayer.curePotionEffects(curativeItem);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean shouldRiderFaceForward(EntityPlayer player) {
		if (m_realPlayer == null) {
			return super.shouldRiderFaceForward(player);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.shouldRiderFaceForward(player));
		}
	}

	@Override
	public boolean isHandActive() {
		if (m_realPlayer == null) {
			return super.isHandActive();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isHandActive();
		}
	}

	@Override
	public EnumHand getActiveHand() {
		if (m_realPlayer == null) {
			return super.getActiveHand();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getActiveHand());
		}
	}

	@Override
	public void setActiveHand(EnumHand hand) {
		if (m_realPlayer == null) {
			super.setActiveHand(hand);
		} else {
			syncToRealPlayer();
			m_realPlayer.setActiveHand(hand);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (m_realPlayer == null) {
			super.notifyDataManagerChange(key);
		} else {
			syncToRealPlayer();
			m_realPlayer.notifyDataManagerChange(key);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public ItemStack getActiveItemStack() {
		if (m_realPlayer == null) {
			return super.getActiveItemStack();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getActiveItemStack());
		}
	}

	@Override
	public int getItemInUseCount() {
		if (m_realPlayer == null) {
			return super.getItemInUseCount();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getItemInUseCount());
		}
	}

	@Override
	public int getItemInUseMaxCount() {
		if (m_realPlayer == null) {
			return super.getItemInUseMaxCount();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getItemInUseMaxCount());
		}
	}

	@Override
	public void stopActiveHand() {
		if (m_realPlayer == null) {
			super.stopActiveHand();
		} else {
			syncToRealPlayer();
			m_realPlayer.stopActiveHand();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void resetActiveHand() {
		if (m_realPlayer == null) {
			super.resetActiveHand();
		} else {
			syncToRealPlayer();
			m_realPlayer.resetActiveHand();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isActiveItemStackBlocking() {
		if (m_realPlayer == null) {
			return super.isActiveItemStackBlocking();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isActiveItemStackBlocking();
		}
	}

	@Override
	public boolean isElytraFlying() {
		if (m_realPlayer == null) {
			return super.isElytraFlying();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isElytraFlying();
		}
	}

	@Override
	public int getTicksElytraFlying() {
		if (m_realPlayer == null) {
			return super.getTicksElytraFlying();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getTicksElytraFlying());
		}
	}

	@Override
	public boolean attemptTeleport(double x, double y, double z) {
		if (m_realPlayer == null) {
			return super.attemptTeleport(x, y, z);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.attemptTeleport(x, y, z));
		}
	}

	@Override
	public boolean canBeHitWithPotion() {
		if (m_realPlayer == null) {
			return super.canBeHitWithPotion();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canBeHitWithPotion());
		}
	}

	@Override
	public boolean attackable() {
		if (m_realPlayer == null) {
			return super.attackable();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.attackable());
		}
	}

	@Override
	public void setPartying(BlockPos pos, boolean p_191987_2_) {
		if (m_realPlayer == null) {
			super.setPartying(pos, p_191987_2_);
		} else {
			syncToRealPlayer();
			m_realPlayer.setPartying(pos, p_191987_2_);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int getEntityId() {
		if (m_realPlayer == null) {
			return super.getEntityId();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getEntityId());
		}
	}

	@Override
	public void setEntityId(int id) {
		if (m_realPlayer == null) {
			super.setEntityId(id);
		} else {
			syncToRealPlayer();
			m_realPlayer.setEntityId(id);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public Set<String> getTags() {
		if (m_realPlayer == null) {
			return super.getTags();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getTags());
		}
	}

	@Override
	public boolean addTag(String tag) {
		if (m_realPlayer == null) {
			return super.addTag(tag);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.addTag(tag));
		}
	}

	@Override
	public boolean removeTag(String tag) {
		if (m_realPlayer == null) {
			return super.removeTag(tag);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.removeTag(tag));
		}
	}

	@Override
	public EntityDataManager getDataManager() {
		if (m_realPlayer == null) {
			return super.getDataManager();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getDataManager());
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
			syncToRealPlayer();
			m_realPlayer.setDropItemsWhenDead(dropWhenDead);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setPosition(double x, double y, double z) {
		if (m_realPlayer == null) {
			super.setPosition(x, y, z);
		} else {
			syncToRealPlayer();
			m_realPlayer.setPosition(x, y, z);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void turn(float yaw, float pitch) {
		if (m_realPlayer == null) {
			super.turn(yaw, pitch);
		} else {
			syncToRealPlayer();
			m_realPlayer.turn(yaw, pitch);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setFire(int seconds) {
		if (m_realPlayer == null) {
			super.setFire(seconds);
		} else {
			syncToRealPlayer();
			m_realPlayer.setFire(seconds);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void extinguish() {
		if (m_realPlayer == null) {
			super.extinguish();
		} else {
			syncToRealPlayer();
			m_realPlayer.extinguish();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isOffsetPositionInLiquid(double x, double y, double z) {
		if (m_realPlayer == null) {
			return super.isOffsetPositionInLiquid(x, y, z);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isOffsetPositionInLiquid(x, y, z);
		}
	}

	@Override
	public void move(MoverType type, double x, double y, double z) {
		if (m_realPlayer == null) {
			super.move(type, x, y, z);
		} else {
			syncToRealPlayer();
			m_realPlayer.move(type, x, y, z);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void resetPositionToBB() {
		if (m_realPlayer == null) {
			super.resetPositionToBB();
		} else {
			syncToRealPlayer();
			m_realPlayer.resetPositionToBB();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isSilent() {
		if (m_realPlayer == null) {
			return super.isSilent();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isSilent();
		}
	}

	@Override
	public void setSilent(boolean isSilent) {
		if (m_realPlayer == null) {
			super.setSilent(isSilent);
		} else {
			syncToRealPlayer();
			m_realPlayer.setSilent(isSilent);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean hasNoGravity() {
		if (m_realPlayer == null) {
			return super.hasNoGravity();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.hasNoGravity());
		}
	}

	@Override
	public void setNoGravity(boolean noGravity) {
		if (m_realPlayer == null) {
			super.setNoGravity(noGravity);
		} else {
			syncToRealPlayer();
			m_realPlayer.setNoGravity(noGravity);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		if (m_realPlayer == null) {
			return super.getCollisionBoundingBox();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCollisionBoundingBox());
		}
	}

	@Override
	public boolean isWet() {
		if (m_realPlayer == null) {
			return super.isWet();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isWet();
		}
	}

	@Override
	public boolean isInWater() {
		if (m_realPlayer == null) {
			return super.isInWater();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isInWater();
		}
	}

	@Override
	public boolean isOverWater() {
		if (m_realPlayer == null) {
			return super.isOverWater();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isOverWater();
		}
	}

	@Override
	public boolean handleWaterMovement() {
		if (m_realPlayer == null) {
			return super.handleWaterMovement();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.handleWaterMovement());
		}
	}

	@Override
	public void spawnRunningParticles() {
		if (m_realPlayer == null) {
			super.spawnRunningParticles();
		} else {
			syncToRealPlayer();
			m_realPlayer.spawnRunningParticles();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isInsideOfMaterial(Material materialIn) {
		if (m_realPlayer == null) {
			return super.isInsideOfMaterial(materialIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isInsideOfMaterial(materialIn);
		}
	}

	@Override
	public boolean isInLava() {
		if (m_realPlayer == null) {
			return super.isInLava();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isInLava();
		}
	}

	@Override
	public void moveRelative(float strafe, float up, float forward, float friction) {
		if (m_realPlayer == null) {
			super.moveRelative(strafe, up, forward, friction);
		} else {
			syncToRealPlayer();
			m_realPlayer.moveRelative(strafe, up, forward, friction);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int getBrightnessForRender() {
		if (m_realPlayer == null) {
			return super.getBrightnessForRender();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getBrightnessForRender());
		}
	}

	@Override
	public float getBrightness() {
		if (m_realPlayer == null) {
			return super.getBrightness();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getBrightness());
		}
	}

	@Override
	public void setWorld(World worldIn) {
		if (m_realPlayer == null) {
			super.setWorld(worldIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.setWorld(worldIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
		if (m_realPlayer == null) {
			super.setPositionAndRotation(x, y, z, yaw, pitch);
		} else {
			syncToRealPlayer();
			m_realPlayer.setPositionAndRotation(x, y, z, yaw, pitch);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void moveToBlockPosAndAngles(BlockPos pos, float rotationYawIn, float rotationPitchIn) {
		if (m_realPlayer == null) {
			super.moveToBlockPosAndAngles(pos, rotationYawIn, rotationPitchIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.moveToBlockPosAndAngles(pos, rotationYawIn, rotationPitchIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
		if (m_realPlayer == null) {
			super.setLocationAndAngles(x, y, z, yaw, pitch);
		} else {
			syncToRealPlayer();
			m_realPlayer.setLocationAndAngles(x, y, z, yaw, pitch);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		if (m_realPlayer == null) {
			super.onCollideWithPlayer(entityIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.onCollideWithPlayer(entityIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void addVelocity(double x, double y, double z) {
		if (m_realPlayer == null) {
			super.addVelocity(x, y, z);
		} else {
			syncToRealPlayer();
			m_realPlayer.addVelocity(x, y, z);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public Vec3d getPositionEyes(float partialTicks) {
		if (m_realPlayer == null) {
			return super.getPositionEyes(partialTicks);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPositionEyes(partialTicks));
		}
	}

	@Override
	public RayTraceResult rayTrace(double blockReachDistance, float partialTicks) {
		if (m_realPlayer == null) {
			return super.rayTrace(blockReachDistance, partialTicks);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.rayTrace(blockReachDistance, partialTicks));
		}
	}

	@Override
	public void awardKillScore(Entity p_191956_1_, int p_191956_2_, DamageSource p_191956_3_) {
		if (m_realPlayer == null) {
			super.awardKillScore(p_191956_1_, p_191956_2_, p_191956_3_);
		} else {
			syncToRealPlayer();
			m_realPlayer.awardKillScore(p_191956_1_, p_191956_2_, p_191956_3_);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isInRangeToRender3d(double x, double y, double z) {
		if (m_realPlayer == null) {
			return super.isInRangeToRender3d(x, y, z);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isInRangeToRender3d(x, y, z);
		}
	}

	@Override
	public boolean isInRangeToRenderDist(double distance) {
		if (m_realPlayer == null) {
			return super.isInRangeToRenderDist(distance);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isInRangeToRenderDist(distance);
		}
	}

	@Override
	public boolean writeToNBTAtomically(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			return super.writeToNBTAtomically(compound);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.writeToNBTAtomically(compound));
		}
	}

	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			return super.writeToNBTOptional(compound);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.writeToNBTOptional(compound));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			return super.writeToNBT(compound);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.writeToNBT(compound));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (m_realPlayer == null) {
			super.readFromNBT(compound);
		} else {
			syncToRealPlayer();
			m_realPlayer.readFromNBT(compound);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public EntityItem dropItem(Item itemIn, int size) {
		if (m_realPlayer == null) {
			return super.dropItem(itemIn, size);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.dropItem(itemIn, size));
		}
	}

	@Override
	public EntityItem dropItemWithOffset(Item itemIn, int size, float offsetY) {
		if (m_realPlayer == null) {
			return super.dropItemWithOffset(itemIn, size, offsetY);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.dropItemWithOffset(itemIn, size, offsetY));
		}
	}

	@Override
	public EntityItem entityDropItem(ItemStack stack, float offsetY) {
		if (m_realPlayer == null) {
			return super.entityDropItem(stack, offsetY);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.entityDropItem(stack, offsetY));
		}
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if (m_realPlayer == null) {
			return super.processInitialInteract(player, hand);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.processInitialInteract(player, hand));
		}
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.getCollisionBox(entityIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCollisionBox(entityIn));
		}
	}

	@Override
	public void updatePassenger(Entity passenger) {
		if (m_realPlayer == null) {
			super.updatePassenger(passenger);
		} else {
			syncToRealPlayer();
			m_realPlayer.updatePassenger(passenger);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void applyOrientationToEntity(Entity entityToUpdate) {
		if (m_realPlayer == null) {
			super.applyOrientationToEntity(entityToUpdate);
		} else {
			syncToRealPlayer();
			m_realPlayer.applyOrientationToEntity(entityToUpdate);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public double getMountedYOffset() {
		if (m_realPlayer == null) {
			return super.getMountedYOffset();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getMountedYOffset());
		}
	}

	@Override
	public boolean startRiding(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.startRiding(entityIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.startRiding(entityIn));
		}
	}

	@Override
	public boolean startRiding(Entity entityIn, boolean force) {
		if (m_realPlayer == null) {
			return super.startRiding(entityIn, force);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.startRiding(entityIn, force));
		}
	}

	@Override
	public void removePassengers() {
		if (m_realPlayer == null) {
			super.removePassengers();
		} else {
			syncToRealPlayer();
			m_realPlayer.removePassengers();
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public float getCollisionBorderSize() {
		if (m_realPlayer == null) {
			return super.getCollisionBorderSize();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCollisionBorderSize());
		}
	}

	@Override
	public Vec3d getForward() {
		if (m_realPlayer == null) {
			return super.getForward();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getForward());
		}
	}

	@Override
	public void setPortal(BlockPos pos) {
		if (m_realPlayer == null) {
			super.setPortal(pos);
		} else {
			syncToRealPlayer();
			m_realPlayer.setPortal(pos);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setVelocity(double x, double y, double z) {
		if (m_realPlayer == null) {
			super.setVelocity(x, y, z);
		} else {
			syncToRealPlayer();
			m_realPlayer.setVelocity(x, y, z);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public Iterable<ItemStack> getEquipmentAndArmor() {
		if (m_realPlayer == null) {
			return super.getEquipmentAndArmor();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getEquipmentAndArmor());
		}
	}

	@Override
	public boolean isBurning() {
		if (m_realPlayer == null) {
			return super.isBurning();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isBurning();
		}
	}

	@Override
	public boolean isRiding() {
		if (m_realPlayer == null) {
			return super.isRiding();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isRiding();
		}
	}

	@Override
	public boolean isBeingRidden() {
		if (m_realPlayer == null) {
			return super.isBeingRidden();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isBeingRidden();
		}
	}

	@Override
	public boolean isSneaking() {
		if (m_realPlayer == null) {
			return super.isSneaking();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isSneaking();
		}
	}

	@Override
	public void setSneaking(boolean sneaking) {
		if (m_realPlayer == null) {
			super.setSneaking(sneaking);
		} else {
			syncToRealPlayer();
			m_realPlayer.setSneaking(sneaking);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isSprinting() {
		if (m_realPlayer == null) {
			return super.isSprinting();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isSprinting();
		}
	}

	@Override
	public boolean isGlowing() {
		if (m_realPlayer == null) {
			return super.isGlowing();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isGlowing();
		}
	}

	@Override
	public void setGlowing(boolean glowingIn) {
		if (m_realPlayer == null) {
			super.setGlowing(glowingIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.setGlowing(glowingIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isInvisible() {
		if (m_realPlayer == null) {
			return super.isInvisible();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isInvisible();
		}
	}

	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.isOnSameTeam(entityIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isOnSameTeam(entityIn);
		}
	}

	@Override
	public boolean isOnScoreboardTeam(Team teamIn) {
		if (m_realPlayer == null) {
			return super.isOnScoreboardTeam(teamIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isOnScoreboardTeam(teamIn);
		}
	}

	@Override
	public void setInvisible(boolean invisible) {
		if (m_realPlayer == null) {
			super.setInvisible(invisible);
		} else {
			syncToRealPlayer();
			m_realPlayer.setInvisible(invisible);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public int getAir() {
		if (m_realPlayer == null) {
			return super.getAir();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAir());
		}
	}

	@Override
	public void setAir(int air) {
		if (m_realPlayer == null) {
			super.setAir(air);
		} else {
			syncToRealPlayer();
			m_realPlayer.setAir(air);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void onStruckByLightning(EntityLightningBolt lightningBolt) {
		if (m_realPlayer == null) {
			super.onStruckByLightning(lightningBolt);
		} else {
			syncToRealPlayer();
			m_realPlayer.onStruckByLightning(lightningBolt);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public Entity[] getParts() {
		if (m_realPlayer == null) {
			return super.getParts();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getParts());
		}
	}

	@Override
	public boolean isEntityEqual(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.isEntityEqual(entityIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isEntityEqual(entityIn);
		}
	}

	@Override
	public boolean canBeAttackedWithItem() {
		if (m_realPlayer == null) {
			return super.canBeAttackedWithItem();
		} else {
			syncToRealPlayer();
			return m_realPlayer.canBeAttackedWithItem();
		}
	}

	@Override
	public boolean hitByEntity(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.hitByEntity(entityIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.hitByEntity(entityIn));
		}
	}

	@Override
	public String toString() {
		if (m_realPlayer == null) {
			return super.toString();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.toString());
		}
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		if (m_realPlayer == null) {
			return super.isEntityInvulnerable(source);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isEntityInvulnerable(source);
		}
	}

	@Override
	public boolean getIsInvulnerable() {
		if (m_realPlayer == null) {
			return super.getIsInvulnerable();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getIsInvulnerable());
		}
	}

	@Override
	public void setEntityInvulnerable(boolean isInvulnerable) {
		if (m_realPlayer == null) {
			super.setEntityInvulnerable(isInvulnerable);
		} else {
			syncToRealPlayer();
			m_realPlayer.setEntityInvulnerable(isInvulnerable);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void copyLocationAndAnglesFrom(Entity entityIn) {
		if (m_realPlayer == null) {
			super.copyLocationAndAnglesFrom(entityIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.copyLocationAndAnglesFrom(entityIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public Entity changeDimension(int dimensionIn) {
		if (m_realPlayer == null) {
			return super.changeDimension(dimensionIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.changeDimension(dimensionIn));
		}
	}

	@Override
	public boolean isNonBoss() {
		if (m_realPlayer == null) {
			return super.isNonBoss();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isNonBoss();
		}
	}

	@Override
	public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
		if (m_realPlayer == null) {
			return super.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn));
		}
	}

	@Override
	public boolean canExplosionDestroyBlock(Explosion explosionIn, World worldIn, BlockPos pos,
			IBlockState blockStateIn, float p_174816_5_) {
		if (m_realPlayer == null) {
			return super.canExplosionDestroyBlock(explosionIn, worldIn, pos, blockStateIn, p_174816_5_);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canExplosionDestroyBlock(explosionIn, worldIn, pos, blockStateIn, p_174816_5_));
		}
	}

	@Override
	public int getMaxFallHeight() {
		if (m_realPlayer == null) {
			return super.getMaxFallHeight();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getMaxFallHeight());
		}
	}

	@Override
	public Vec3d getLastPortalVec() {
		if (m_realPlayer == null) {
			return super.getLastPortalVec();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getLastPortalVec());
		}
	}

	@Override
	public EnumFacing getTeleportDirection() {
		if (m_realPlayer == null) {
			return super.getTeleportDirection();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getTeleportDirection());
		}
	}

	@Override
	public boolean doesEntityNotTriggerPressurePlate() {
		if (m_realPlayer == null) {
			return super.doesEntityNotTriggerPressurePlate();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.doesEntityNotTriggerPressurePlate());
		}
	}

	@Override
	public void addEntityCrashInfo(CrashReportCategory category) {
		if (m_realPlayer == null) {
			super.addEntityCrashInfo(category);
		} else {
			syncToRealPlayer();
			m_realPlayer.addEntityCrashInfo(category);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void setUniqueId(UUID uniqueIdIn) {
		if (m_realPlayer == null) {
			super.setUniqueId(uniqueIdIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.setUniqueId(uniqueIdIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean canRenderOnFire() {
		if (m_realPlayer == null) {
			return super.canRenderOnFire();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canRenderOnFire());
		}
	}

	@Override
	public UUID getUniqueID() {
		if (m_realPlayer == null) {
			return super.getUniqueID();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getUniqueID());
		}
	}

	@Override
	public String getCachedUniqueIdString() {
		if (m_realPlayer == null) {
			return super.getCachedUniqueIdString();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCachedUniqueIdString());
		}
	}

	@Override
	public void setCustomNameTag(String name) {
		if (m_realPlayer == null) {
			super.setCustomNameTag(name);
		} else {
			syncToRealPlayer();
			m_realPlayer.setCustomNameTag(name);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public String getCustomNameTag() {
		if (m_realPlayer == null) {
			return super.getCustomNameTag();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCustomNameTag());
		}
	}

	@Override
	public boolean hasCustomName() {
		if (m_realPlayer == null) {
			return super.hasCustomName();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.hasCustomName());
		}
	}

	@Override
	public void setAlwaysRenderNameTag(boolean alwaysRenderNameTag) {
		if (m_realPlayer == null) {
			super.setAlwaysRenderNameTag(alwaysRenderNameTag);
		} else {
			syncToRealPlayer();
			m_realPlayer.setAlwaysRenderNameTag(alwaysRenderNameTag);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean getAlwaysRenderNameTag() {
		if (m_realPlayer == null) {
			return super.getAlwaysRenderNameTag();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAlwaysRenderNameTag());
		}
	}

	@Override
	public void setPositionAndUpdate(double x, double y, double z) {
		if (m_realPlayer == null) {
			super.setPositionAndUpdate(x, y, z);
		} else {
			syncToRealPlayer();
			m_realPlayer.setPositionAndUpdate(x, y, z);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public EnumFacing getHorizontalFacing() {
		if (m_realPlayer == null) {
			return super.getHorizontalFacing();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getHorizontalFacing());
		}
	}

	@Override
	public EnumFacing getAdjustedHorizontalFacing() {
		if (m_realPlayer == null) {
			return super.getAdjustedHorizontalFacing();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getAdjustedHorizontalFacing());
		}
	}

	@Override
	public boolean isSpectatedByPlayer(EntityPlayerMP player) {
		if (m_realPlayer == null) {
			return super.isSpectatedByPlayer(player);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isSpectatedByPlayer(player);
		}
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		if (m_realPlayer == null) {
			return super.getEntityBoundingBox();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getEntityBoundingBox());
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (m_realPlayer == null) {
			return super.getRenderBoundingBox();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRenderBoundingBox());
		}
	}

	@Override
	public void setEntityBoundingBox(AxisAlignedBB bb) {
		if (m_realPlayer == null) {
			super.setEntityBoundingBox(bb);
		} else {
			syncToRealPlayer();
			m_realPlayer.setEntityBoundingBox(bb);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean isOutsideBorder() {
		if (m_realPlayer == null) {
			return super.isOutsideBorder();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isOutsideBorder();
		}
	}

	@Override
	public void setOutsideBorder(boolean outsideBorder) {
		if (m_realPlayer == null) {
			super.setOutsideBorder(outsideBorder);
		} else {
			syncToRealPlayer();
			m_realPlayer.setOutsideBorder(outsideBorder);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void sendMessage(ITextComponent component) {
		if (m_realPlayer == null) {
			super.sendMessage(component);
		} else {
			syncToRealPlayer();
			m_realPlayer.sendMessage(component);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public boolean canUseCommand(int permLevel, String commandName) {
		if (m_realPlayer == null) {
			return super.canUseCommand(permLevel, commandName);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canUseCommand(permLevel, commandName));
		}
	}

	@Override
	public Entity getCommandSenderEntity() {
		if (m_realPlayer == null) {
			return super.getCommandSenderEntity();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCommandSenderEntity());
		}
	}

	@Override
	public void setCommandStat(Type type, int amount) {
		if (m_realPlayer == null) {
			super.setCommandStat(type, amount);
		} else {
			syncToRealPlayer();
			m_realPlayer.setCommandStat(type, amount);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public MinecraftServer getServer() {
		if (m_realPlayer == null) {
			return super.getServer();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getServer());
		}
	}

	@Override
	public CommandResultStats getCommandStats() {
		if (m_realPlayer == null) {
			return super.getCommandStats();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getCommandStats());
		}
	}

	@Override
	public void setCommandStats(Entity entityIn) {
		if (m_realPlayer == null) {
			super.setCommandStats(entityIn);
		} else {
			syncToRealPlayer();
			m_realPlayer.setCommandStats(entityIn);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		if (m_realPlayer == null) {
			return super.applyPlayerInteraction(player, vec, hand);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.applyPlayerInteraction(player, vec, hand));
		}
	}

	@Override
	public boolean isImmuneToExplosions() {
		if (m_realPlayer == null) {
			return super.isImmuneToExplosions();
		} else {
			syncToRealPlayer();
			return m_realPlayer.isImmuneToExplosions();
		}
	}

	@Override
	public NBTTagCompound getEntityData() {
		if (m_realPlayer == null) {
			return super.getEntityData();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getEntityData());
		}
	}

	@Override
	public boolean shouldRiderSit() {
		if (m_realPlayer == null) {
			return super.shouldRiderSit();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.shouldRiderSit());
		}
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		if (m_realPlayer == null) {
			return super.getPickedResult(target);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPickedResult(target));
		}
	}

	@Override
	public UUID getPersistentID() {
		if (m_realPlayer == null) {
			return super.getPersistentID();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPersistentID());
		}
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		if (m_realPlayer == null) {
			return super.shouldRenderInPass(pass);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.shouldRenderInPass(pass));
		}
	}

	@Override
	public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
		if (m_realPlayer == null) {
			return super.isCreatureType(type, forSpawnCount);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isCreatureType(type, forSpawnCount);
		}
	}

	@Override
	public boolean canRiderInteract() {
		if (m_realPlayer == null) {
			return super.canRiderInteract();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canRiderInteract());
		}
	}

	@Override
	public boolean shouldDismountInWater(Entity rider) {
		if (m_realPlayer == null) {
			return super.shouldDismountInWater(rider);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.shouldDismountInWater(rider));
		}
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if (m_realPlayer == null) {
			super.deserializeNBT(nbt);
		} else {
			syncToRealPlayer();
			m_realPlayer.deserializeNBT(nbt);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		if (m_realPlayer == null) {
			return super.serializeNBT();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.serializeNBT());
		}
	}

	@Override
	public boolean canTrample(World world, Block block, BlockPos pos, float fallDistance) {
		if (m_realPlayer == null) {
			return super.canTrample(world, block, pos, fallDistance);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canTrample(world, block, pos, fallDistance));
		}
	}

	@Override
	public void addTrackingPlayer(EntityPlayerMP player) {
		if (m_realPlayer == null) {
			super.addTrackingPlayer(player);
		} else {
			syncToRealPlayer();
			m_realPlayer.addTrackingPlayer(player);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		if (m_realPlayer == null) {
			super.removeTrackingPlayer(player);
		} else {
			syncToRealPlayer();
			m_realPlayer.removeTrackingPlayer(player);
			syncPublicFieldsFromReal();
		}
	}

	@Override
	public float getRotatedYaw(Rotation transformRotation) {
		if (m_realPlayer == null) {
			return super.getRotatedYaw(transformRotation);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRotatedYaw(transformRotation));
		}
	}

	@Override
	public float getMirroredYaw(Mirror transformMirror) {
		if (m_realPlayer == null) {
			return super.getMirroredYaw(transformMirror);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getMirroredYaw(transformMirror));
		}
	}

	@Override
	public boolean ignoreItemEntityData() {
		if (m_realPlayer == null) {
			return super.ignoreItemEntityData();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.ignoreItemEntityData());
		}
	}

	@Override
	public boolean setPositionNonDirty() {
		if (m_realPlayer == null) {
			return super.setPositionNonDirty();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.setPositionNonDirty());
		}
	}

	@Override
	public Entity getControllingPassenger() {
		if (m_realPlayer == null) {
			return super.getControllingPassenger();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getControllingPassenger());
		}
	}

	@Override
	public List<Entity> getPassengers() {
		if (m_realPlayer == null) {
			return super.getPassengers();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPassengers());
		}
	}

	@Override
	public boolean isPassenger(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.isPassenger(entityIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isPassenger(entityIn);
		}
	}

	@Override
	public Collection<Entity> getRecursivePassengers() {
		if (m_realPlayer == null) {
			return super.getRecursivePassengers();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRecursivePassengers());
		}
	}

	@Override
	public <T extends Entity> Collection<T> getRecursivePassengersByType(Class<T> entityClass) {
		if (m_realPlayer == null) {
			return super.getRecursivePassengersByType(entityClass);
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRecursivePassengersByType(entityClass));
		}
	}

	@Override
	public Entity getLowestRidingEntity() {
		if (m_realPlayer == null) {
			return super.getLowestRidingEntity();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getLowestRidingEntity());
		}
	}

	@Override
	public boolean isRidingSameEntity(Entity entityIn) {
		if (m_realPlayer == null) {
				return super.isRidingSameEntity(entityIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isRidingSameEntity(entityIn);
		}
	}

	@Override
	public boolean isRidingOrBeingRiddenBy(Entity entityIn) {
		if (m_realPlayer == null) {
			return super.isRidingOrBeingRiddenBy(entityIn);
		} else {
			syncToRealPlayer();
			return m_realPlayer.isRidingOrBeingRiddenBy(entityIn);
		}
	}

	@Override
	public boolean canPassengerSteer() {
		if (m_realPlayer == null) {
			return super.canPassengerSteer();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.canPassengerSteer());
		}
	}

	@Override
	public Entity getRidingEntity() {
		if (m_realPlayer == null) {
			return super.getRidingEntity();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getRidingEntity());
		}
	}

	@Override
	public EnumPushReaction getPushReaction() {
		if (m_realPlayer == null) {
			return super.getPushReaction();
		} else {
			syncToRealPlayer();
			return syncPublicFieldsFromRealAndReturn(m_realPlayer.getPushReaction());
		}
	}


}
