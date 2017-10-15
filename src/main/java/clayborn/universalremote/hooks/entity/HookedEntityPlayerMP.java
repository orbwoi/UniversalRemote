package clayborn.universalremote.hooks.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.mojang.authlib.GameProfile;

import clayborn.universalremote.hooks.world.WorldServerProxy;
import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

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

    public static HookedEntityPlayerMP CreateFromExisting(EntityPlayerMP original)
    {
		HookedEntityPlayerMP returnedPlayer = new HookedEntityPlayerMP(original.mcServer, (WorldServer) original.world, original.getGameProfile(), original.interactionManager);

		CapabilityDispatcher caps = null;

		// backup the player capabilities
		try {
			caps = InjectionHandler.readFieldOfType(returnedPlayer, CapabilityDispatcher.class);
		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to capture CapabilityDispatcher from created player.", e);
		}

		InjectionHandler.copyAllFieldsFromEx(returnedPlayer, original, EntityPlayerMP.class);

		// fix dependent references
		returnedPlayer.interactionManager.player = returnedPlayer;
		returnedPlayer.inventory.player = returnedPlayer;

		// write player capabilities
		if (caps != null)
		{
			try {
				InjectionHandler.writeFieldOfType(returnedPlayer, caps, CapabilityDispatcher.class);
			} catch (IllegalAccessException e) {
				Util.logger.logException("Unable to write CapabilityDispatcher to new hooked player.", e);
			}
		}

		return returnedPlayer;
    }

	public HookedEntityPlayerMP(MinecraftServer server, WorldServer worldIn, GameProfile profile,
			PlayerInteractionManager interactionManagerIn) {
		super(server, worldIn, profile, interactionManagerIn);
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

}
