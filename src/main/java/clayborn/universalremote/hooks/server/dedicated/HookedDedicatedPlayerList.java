package clayborn.universalremote.hooks.server.dedicated;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import clayborn.universalremote.hooks.entity.EntityPlayerMPProxy;
import clayborn.universalremote.hooks.entity.HookedEntityPlayerMP;
import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.management.DemoPlayerInteractionManager;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class HookedDedicatedPlayerList extends DedicatedPlayerList {

	private final MinecraftServer mcServer;
	private GameType gameType;
	private final List<EntityPlayerMP> playerEntityList;
	private final Map<UUID, EntityPlayerMP> uuidToPlayerMap;

	@SuppressWarnings("unchecked")
	public HookedDedicatedPlayerList(DedicatedPlayerList oldList) throws IllegalAccessException, NoSuchFieldException, SecurityException {
		super(oldList.getServerInstance());

		InjectionHandler.copyAllFieldsFromEx(this, oldList, DedicatedPlayerList.class);

		mcServer = InjectionHandler.readFieldOfType(PlayerList.class, this, MinecraftServer.class);
		gameType = InjectionHandler.readFieldOfType(PlayerList.class, this, GameType.class);
		playerEntityList = InjectionHandler.readFieldOfType(PlayerList.class, this, List.class);
		uuidToPlayerMap = InjectionHandler.readFieldOfType(PlayerList.class, this, Map.class);
	}

    /**
     * also checks for multiple logins across servers
     */
	@Override
    public EntityPlayerMP createPlayerForUser(GameProfile profile)
    {
        UUID uuid = EntityPlayer.getUUID(profile);
        List<EntityPlayerMP> list = Lists.<EntityPlayerMP>newArrayList();

        for (int i = 0; i < this.playerEntityList.size(); ++i)
        {
            EntityPlayerMP entityplayermp = this.playerEntityList.get(i);

            if (entityplayermp.getUniqueID().equals(uuid))
            {
                list.add(entityplayermp);
            }
        }

        EntityPlayerMP entityplayermp2 = this.uuidToPlayerMap.get(profile.getId());

        if (entityplayermp2 != null && !list.contains(entityplayermp2))
        {
            list.add(entityplayermp2);
        }

        for (EntityPlayerMP entityplayermp1 : list)
        {
            entityplayermp1.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login", new Object[0]));
        }

        PlayerInteractionManager playerinteractionmanager;

        if (this.mcServer.isDemo())
        {
            playerinteractionmanager = new DemoPlayerInteractionManager(this.mcServer.getWorld(0));
        }
        else
        {
            playerinteractionmanager = new PlayerInteractionManager(this.mcServer.getWorld(0));
        }

        try {
			return new HookedEntityPlayerMP(this.mcServer, this.mcServer.getWorld(0), profile, playerinteractionmanager);
		} catch (IllegalAccessException e) {
			Util.logger.logException("CRITICAL ERROR creating HookedEntityPlayerMP!", e);
			return null;
		}
    }

    private void setPlayerGameTypeBasedOnOther(EntityPlayerMP target, EntityPlayerMP source, World worldIn)
    {
        if (source != null)
        {
            target.interactionManager.setGameType(source.interactionManager.getGameType());
        }
        else if (this.gameType != null)
        {
            target.interactionManager.setGameType(this.gameType);
        }

        target.interactionManager.initializeGameType(worldIn.getWorldInfo().getGameType());
    }

    /**
     * Destroys the given player entity and recreates another in the given dimension. Used when respawning after death
     * or returning from the End
     */
	@Override
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd)
    {
        World world = mcServer.getWorld(dimension);
        if (world == null)
        {
            dimension = playerIn.getSpawnDimension();
        }
        else if (!world.provider.canRespawnHere())
        {
            dimension = world.provider.getRespawnDimension(playerIn);
        }
        if (mcServer.getWorld(dimension) == null) dimension = 0;

        playerIn.getServerWorld().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerWorld().getEntityTracker().untrack(playerIn);
        playerIn.getServerWorld().getPlayerChunkMap().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        this.mcServer.getWorld(playerIn.dimension).removeEntityDangerously(playerIn);
        BlockPos blockpos = playerIn.getBedLocation(dimension);
        boolean flag = playerIn.isSpawnForced(dimension);
        playerIn.dimension = dimension;
        PlayerInteractionManager playerinteractionmanager;

        if (this.mcServer.isDemo())
        {
            playerinteractionmanager = new DemoPlayerInteractionManager(this.mcServer.getWorld(playerIn.dimension));
        }
        else
        {
            playerinteractionmanager = new PlayerInteractionManager(this.mcServer.getWorld(playerIn.dimension));
        }

        EntityPlayerMP entityplayermp;

        try {
        	entityplayermp = new HookedEntityPlayerMP(this.mcServer, this.mcServer.getWorld(playerIn.dimension), playerIn.getGameProfile(), playerinteractionmanager);
		} catch (IllegalAccessException e) {
			Util.logger.logException("CRITICAL ERROR creating HookedEntityPlayerMP!", e);
			return null;
		}

        entityplayermp.connection = playerIn.connection;
        entityplayermp.copyFrom(playerIn, conqueredEnd);
        entityplayermp.dimension = dimension;
        entityplayermp.setEntityId(playerIn.getEntityId());
        entityplayermp.setCommandStats(playerIn);
        entityplayermp.setPrimaryHand(playerIn.getPrimaryHand());

        for (String s : playerIn.getTags())
        {
            entityplayermp.addTag(s);
        }

        WorldServer worldserver = this.mcServer.getWorld(playerIn.dimension);
        this.setPlayerGameTypeBasedOnOther(entityplayermp, playerIn, worldserver);

        if (blockpos != null)
        {
            BlockPos blockpos1 = EntityPlayer.getBedSpawnLocation(this.mcServer.getWorld(playerIn.dimension), blockpos, flag);

            if (blockpos1 != null)
            {
                entityplayermp.setLocationAndAngles(blockpos1.getX() + 0.5F, blockpos1.getY() + 0.1F, blockpos1.getZ() + 0.5F, 0.0F, 0.0F);
                entityplayermp.setSpawnPoint(blockpos, flag);
            }
            else
            {
                entityplayermp.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
            }
        }

        worldserver.getChunkProvider().provideChunk((int)entityplayermp.posX >> 4, (int)entityplayermp.posZ >> 4);

        while (!worldserver.getCollisionBoxes(entityplayermp, entityplayermp.getEntityBoundingBox()).isEmpty() && entityplayermp.posY < 256.0D)
        {
            entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ);
        }

        entityplayermp.connection.sendPacket(new SPacketRespawn(entityplayermp.dimension, entityplayermp.world.getDifficulty(), entityplayermp.world.getWorldInfo().getTerrainType(), entityplayermp.interactionManager.getGameType()));
        BlockPos blockpos2 = worldserver.getSpawnPoint();
        entityplayermp.connection.setPlayerLocation(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
        entityplayermp.connection.sendPacket(new SPacketSpawnPosition(blockpos2));
        entityplayermp.connection.sendPacket(new SPacketSetExperience(entityplayermp.experience, entityplayermp.experienceTotal, entityplayermp.experienceLevel));
        this.updateTimeAndWeatherForPlayer(entityplayermp, worldserver);
        this.updatePermissionLevel(entityplayermp);
        worldserver.getPlayerChunkMap().addPlayer(entityplayermp);
        worldserver.spawnEntity(entityplayermp);
        this.playerEntityList.add(entityplayermp);
        this.uuidToPlayerMap.put(entityplayermp.getUniqueID(), entityplayermp);
        entityplayermp.addSelfToInternalCraftingInventory();
        entityplayermp.setHealth(entityplayermp.getHealth());
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp, conqueredEnd);
        return entityplayermp;
    }

	@Override
	public void transferPlayerToDimension(EntityPlayerMP player, int dimensionIn, Teleporter teleporter) {
		if (player instanceof EntityPlayerMPProxy)
		{
			player = ((EntityPlayerMPProxy)player).getRealPlayer();
		}
		super.transferPlayerToDimension(player, dimensionIn, teleporter);
	}

}
