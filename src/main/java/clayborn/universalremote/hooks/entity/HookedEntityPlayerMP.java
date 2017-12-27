package clayborn.universalremote.hooks.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.mojang.authlib.GameProfile;

import clayborn.universalremote.hooks.world.WorldServerProxy;
import clayborn.universalremote.util.Util;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class HookedEntityPlayerMP extends EntityPlayerMP {

	private String m_modPrefix;

    private double m_RemotePosX;
    private double m_RemotePosY;
    private double m_RemotePosZ;

    private BlockPos m_TargetBlockPos;

    // reserved for later use if needed
    private float m_RemoteRotationPitch;
    private float m_RemoteRotationYaw;

    @SuppressWarnings("unchecked")
	private static final Set<String> PLAYERENTITYWORLDPROXYEXCEPTIONLIST =
		new HashSet<String>(Arrays.asList(
    	     new String[] {"ic2"}
    	));

    @SuppressWarnings("unchecked")
	private static final Set<String> PROXYWORLDEXCEPTIONLIST =
		new HashSet<String>(Arrays.asList(
    	     new String[] {"net.minecraft"}
    	));

//	private final List<Integer> entityRemoveQueue;
//	private final PlayerAdvancements advancements;

	@SuppressWarnings("unchecked")
	public HookedEntityPlayerMP(MinecraftServer server, WorldServer worldIn, GameProfile profile,
			PlayerInteractionManager interactionManagerIn) throws IllegalAccessException {
		super(server, worldIn, profile, interactionManagerIn);

//		entityRemoveQueue = InjectionHandler.readFieldOfType(EntityPlayerMP.class, this, List.class);
//		advancements= InjectionHandler.readFieldOfType(EntityPlayerMP.class, this, PlayerAdvancements.class);
	}

	public void SetRemoteFilter(String modClass, WorldServer proxyWorld, BlockPos targetPos,
			double playerX, double playerY, double playerZ, float pitch, float yaw)
	{
		m_modPrefix = Util.getClassDomainFromName(modClass);

		m_RemotePosX = playerX;
		m_RemotePosY = playerY;
		m_RemotePosZ = playerZ;

		m_TargetBlockPos = targetPos;

		m_RemoteRotationPitch = pitch;
		m_RemoteRotationYaw = yaw;

		if (!PROXYWORLDEXCEPTIONLIST.contains(m_modPrefix))
		{
			this.world = new WorldServerProxy((WorldServer)this.world, proxyWorld, modClass);
		}
	}

	public void ClearRemoteFilter()
	{

		if (this.world instanceof WorldServerProxy)
		{
			this.world = ((WorldServerProxy)this.world).GetRealWorld();
		} else if (PROXYWORLDEXCEPTIONLIST.contains(m_modPrefix) ) {
			// NOP
		} else if (m_modPrefix != null) {
			//uh ho...
			Util.logger.error("ClearRemoteFilter could not clear player.world because it was not an instance of WorldServerProxy.");
		}

		m_modPrefix = null;
	}

	@Override
	public World getEntityWorld() {
		if (m_modPrefix != null && !PROXYWORLDEXCEPTIONLIST.contains(m_modPrefix) &&
			PLAYERENTITYWORLDPROXYEXCEPTIONLIST.contains(m_modPrefix) &&
			Util.isPrefixInCallStack(m_modPrefix) )
			{
				if (this.world instanceof WorldServerProxy)
				{
					return ((WorldServerProxy)this.world).GetProxyWorld();
				} else {
					//uh ho...
					Util.logger.error("Could not apply PLAYERENTITYWORLDPROXYEXCEPTIONLIST because player.world was not instance of WorldServerProxy!");
					return super.getEntityWorld();
				}
			}
			else
			{
				return super.getEntityWorld();
			}
	}

	@Override
	public double getDistanceSq(double x, double y, double z) {
		if (m_modPrefix != null &&
			x > m_TargetBlockPos.getX() - 1 && x < m_TargetBlockPos.getX() + 1 &&
			y > m_TargetBlockPos.getY() - 1 && y < m_TargetBlockPos.getY() + 1 &&
			z > m_TargetBlockPos.getZ() - 1 && z < m_TargetBlockPos.getZ() + 1 &&
			Util.isPrefixInCallStack(m_modPrefix))
		{
	        double d0 = m_RemotePosX - x;
	        double d1 = m_RemotePosY - y;
	        double d2 = m_RemotePosZ - z;
	        return d0 * d0 + d1 * d1 + d2 * d2;
		}
		else
		{
			return super.getDistanceSq(x, y, z);
		}
	}

	@Override
	public double getDistanceSq(BlockPos pos) {
		if (m_modPrefix != null &&
			pos.getX() == m_TargetBlockPos.getX() &&
			pos.getY() == m_TargetBlockPos.getY() &&
			pos.getZ() == m_TargetBlockPos.getZ() &&
			Util.isPrefixInCallStack(m_modPrefix))
		{
			return pos.distanceSq(m_RemotePosX, m_RemotePosY, m_RemotePosZ);
		}
		else
		{
			return super.getDistanceSq(pos);
		}
	}

	@Override
	public double getDistanceSqToCenter(BlockPos pos) {
		if (m_modPrefix != null &&
			pos.getX() == m_TargetBlockPos.getX() &&
			pos.getY() == m_TargetBlockPos.getY() &&
			pos.getZ() == m_TargetBlockPos.getZ() &&
			Util.isPrefixInCallStack(m_modPrefix))
		{
			return pos.distanceSqToCenter(m_RemotePosX, m_RemotePosY, m_RemotePosZ);
		}
		else
		{
			return super.getDistanceSqToCenter(pos);
		}

	}

	// removed for now -- there is a second check in EntityPlayer::OnUpdate()!

//	private static Field respawnInvulnerabilityTicksField = ReflectionHelper.findField(EntityPlayerMP.class, "respawnInvulnerabilityTicks", "field_147101_bU");
//	private static Field levitationStartPosField = ReflectionHelper.findField(EntityPlayerMP.class, "levitationStartPos", "field_193107_ct");
//	private static Field levitatingSinceField = ReflectionHelper.findField(EntityPlayerMP.class, "levitatingSince", "field_193108_cu");
//
//	@Override
//	public void onUpdate() {
//	    this.interactionManager.updateBlockRemoving();
//
//	    int respawnInvulnerabilityTicks;
//		try {
//			respawnInvulnerabilityTicks = InjectionHandler.readField(respawnInvulnerabilityTicksField, this);
//	        --respawnInvulnerabilityTicks;
//	        InjectionHandler.writeField(respawnInvulnerabilityTicksField, this, respawnInvulnerabilityTicks);
//		} catch (IllegalAccessException e) {
//			Util.logger.logException("Unable to set respawnInvulnerabilityTicks for EntityPlayerMP.onUpdate()", e);
//		}
//
//        if (this.hurtResistantTime > 0)
//        {
//            --this.hurtResistantTime;
//        }
//
//        this.openContainer.detectAndSendChanges();
//
//        // just in case they check location directly...
//        double oldPosX = this.posX;
//        double oldPosY = this.posY;
//        double oldPosZ = this.posZ;
//
//        if (this.m_modPrefix != null)
//        {
//        	this.posX = m_RemotePosX;
//        	this.posY = m_RemotePosY;
//        	this.posZ = m_RemotePosZ;
//        }
//
//        if (!this.world.isRemote && this.openContainer != null && !this.openContainer.canInteractWith(this))
//        {
//            // fix the location for closeScreen, just in case
//            if (this.m_modPrefix != null)
//            {
//            	this.posX = oldPosX;
//            	this.posY = oldPosY;
//            	this.posZ = oldPosZ;
//            }
//
//            this.closeScreen();
//            this.openContainer = this.inventoryContainer;
//        }
//
//        // fix the location
//        if (this.m_modPrefix != null)
//        {
//        	this.posX = oldPosX;
//        	this.posY = oldPosY;
//        	this.posZ = oldPosZ;
//        }
//
//        while (!this.entityRemoveQueue.isEmpty())
//        {
//            int i = Math.min(this.entityRemoveQueue.size(), Integer.MAX_VALUE);
//            int[] aint = new int[i];
//            Iterator<Integer> iterator = this.entityRemoveQueue.iterator();
//            int j = 0;
//
//            while (iterator.hasNext() && j < i)
//            {
//                aint[j++] = iterator.next().intValue();
//                iterator.remove();
//            }
//
//            this.connection.sendPacket(new SPacketDestroyEntities(aint));
//        }
//
//        Entity entity = this.getSpectatingEntity();
//
//        if (entity != this)
//        {
//            if (entity.isEntityAlive())
//            {
//                this.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
//                this.mcServer.getPlayerList().serverUpdateMovingPlayer(this);
//
//                if (this.isSneaking())
//                {
//                    this.setSpectatingEntity(this);
//                }
//            }
//            else
//            {
//                this.setSpectatingEntity(this);
//            }
//        }
//
//        CriteriaTriggers.TICK.trigger(this);
//
//		try {
//
//	        Vec3d levitationStartPos = InjectionHandler.readField(levitationStartPosField, this);
//	        int levitatingSince = InjectionHandler.readField(levitatingSinceField, this);
//
//	        if (levitationStartPos != null)
//	        {
//	            CriteriaTriggers.LEVITATION.trigger(this, levitationStartPos, this.ticksExisted - levitatingSince);
//	        }
//
//		} catch (IllegalAccessException e) {
//			Util.logger.logException("Unable to read levitationStartPos/levitatingSince for EntityPlayerMP.onUpdate()", e);
//		}
//
//        this.advancements.flushDirty(this);
//	}



}
