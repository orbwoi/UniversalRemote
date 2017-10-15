package clayborn.universalremote.hooks.world;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.util.concurrent.ListenableFuture;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.capabilities.Capability;

public class WorldServerProxy extends WorldServer {

	private WorldServer m_realWorld;
	private WorldServer m_proxyWorld;
	private String m_modPrefix;

	public WorldServerProxy(WorldServer realWorld, WorldServer proxyWorld, String modClass) {
		super(proxyWorld.getMinecraftServer(), proxyWorld.getSaveHandler(), proxyWorld.getWorldInfo(), proxyWorld.provider.getDimension(), proxyWorld.profiler);

		// fix the dimension manager!
		net.minecraftforge.common.DimensionManager.setWorld(this.provider.getDimension(), proxyWorld, proxyWorld.getMinecraftServer());

		m_realWorld = realWorld;
		m_proxyWorld = proxyWorld;
		m_modPrefix = Util.getClassDomainFromName(modClass);

		InjectionHandler.copyAllFieldsFrom(this, m_realWorld, WorldServer.class);

		try {
			InjectionHandler.writeFieldOfType(
					this,
					new WorldProviderProxyServer(m_realWorld.provider, m_proxyWorld.provider, modClass),
					WorldProvider.class);
		} catch (IllegalAccessException e) {
			Util.logger.logException("Unable to set WorldProviderProxyServer", e);
		}
	}

	public WorldServer GetRealWorld()
	{
		return m_realWorld;
	}

	public WorldServer GetProxyWorld()
	{
		return m_proxyWorld;
	}

	/* Modified Functions */

	/* Proxy Functions */

	// NOTE: the if m_realWorld != null in each function is to handle the case
	// where the super constructor calls this member function during object construction

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getTileEntity(pos);
		} else if (m_realWorld != null ) {
			return m_realWorld.getTileEntity(pos);
		} else {
			return super.getTileEntity(pos);
		}
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getBlockState(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getBlockState(pos);
		} else {
			return super.getBlockState(pos);
		}
	}

	@Override
	public void markTileEntitiesInChunkForRemoval(Chunk chunk) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.markTileEntitiesInChunkForRemoval(chunk);
		} else if (m_realWorld != null) {
			m_realWorld.markTileEntitiesInChunkForRemoval(chunk);
		} else {
			super.markTileEntitiesInChunkForRemoval(chunk);
		}
	}

	@Override
	public SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType creatureType, BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSpawnListEntryForTypeAt(creatureType, pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getSpawnListEntryForTypeAt(creatureType, pos);
		} else {
			return super.getSpawnListEntryForTypeAt(creatureType, pos);
		}
	}

	@Override
	public boolean canCreatureTypeSpawnHere(EnumCreatureType creatureType, SpawnListEntry spawnListEntry,
			BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canCreatureTypeSpawnHere(creatureType, spawnListEntry, pos);
		} else if (m_realWorld != null) {
			return m_realWorld.canCreatureTypeSpawnHere(creatureType, spawnListEntry, pos);
		} else {
			return super.canCreatureTypeSpawnHere(creatureType, spawnListEntry, pos);
		}
	}

	@Override
	public boolean areAllPlayersAsleep() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.areAllPlayersAsleep();
		} else if (m_realWorld != null) {
			return m_realWorld.areAllPlayersAsleep();
		} else {
			return super.areAllPlayersAsleep();
		}
	}

	@Override
	public void resetUpdateEntityTick() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.resetUpdateEntityTick();
		} else if (m_realWorld != null) {
			m_realWorld.resetUpdateEntityTick();
		} else {
			super.resetUpdateEntityTick();
		}
	}

	@Override
	public BlockPos getSpawnCoordinate() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSpawnCoordinate();
		} else if (m_realWorld != null) {
			return m_realWorld.getSpawnCoordinate();
		} else {
			return super.getSpawnCoordinate();
		}
	}

	@Override
	public void saveAllChunks(boolean all, IProgressUpdate progressCallback) throws MinecraftException {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.saveAllChunks(all, progressCallback);
		} else if (m_realWorld != null) {
			m_realWorld.saveAllChunks(all, progressCallback);
		} else {
			super.saveAllChunks(all, progressCallback);
		}
	}

	@Override
	public void flushToDisk() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.flushToDisk();
		} else if (m_realWorld != null) {
			m_realWorld.flushToDisk();
		} else {
			super.flushToDisk();
		}
	}

	@Override
	public void flush() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.flush();
		} else if (m_realWorld != null) {
			m_realWorld.flush();
		} else {
			super.flush();
		}
	}

	@Override
	public EntityTracker getEntityTracker() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getEntityTracker();
		} else if (m_realWorld != null) {
			return m_realWorld.getEntityTracker();
		} else {
			return super.getEntityTracker();
		}
	}

	@Override
	public PlayerChunkMap getPlayerChunkMap() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPlayerChunkMap();
		} else if (m_realWorld != null) {
			return m_realWorld.getPlayerChunkMap();
		} else {
			return super.getPlayerChunkMap();
		}
	}

	@Override
	public Teleporter getDefaultTeleporter() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getDefaultTeleporter();
		} else if (m_realWorld != null) {
			return m_realWorld.getDefaultTeleporter();
		} else {
			return super.getDefaultTeleporter();
		}
	}

	@Override
	public TemplateManager getStructureTemplateManager() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getStructureTemplateManager();
		} else if (m_realWorld != null) {
			return m_realWorld.getStructureTemplateManager();
		} else {
			return super.getStructureTemplateManager();
		}
	}

	@Override
	public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord,
			int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed,
			int... particleArguments) {
		if (m_proxyWorld != null) {
			m_proxyWorld.spawnParticle(particleType, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed,
					particleArguments);
		} else {
			super.spawnParticle(particleType, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed,
					particleArguments);
		}
	}

	@Override
	public void spawnParticle(EnumParticleTypes particleType, boolean longDistance, double xCoord, double yCoord,
			double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed,
			int... particleArguments) {
		if (m_proxyWorld != null) {
			m_proxyWorld.spawnParticle(particleType, longDistance, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset,
					particleSpeed, particleArguments);
		} else {
			super.spawnParticle(particleType, longDistance, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset,
					particleSpeed, particleArguments);
		}
	}

	@Override
	public void spawnParticle(EntityPlayerMP player, EnumParticleTypes particle, boolean longDistance, double x,
			double y, double z, int count, double xOffset, double yOffset, double zOffset, double speed,
			int... arguments) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.spawnParticle(player, particle, longDistance, x, y, z, count, xOffset, yOffset, zOffset, speed, arguments);
		} else if (m_realWorld != null) {
			m_realWorld.spawnParticle(player, particle, longDistance, x, y, z, count, xOffset, yOffset, zOffset, speed, arguments);
		} else {
			super.spawnParticle(player, particle, longDistance, x, y, z, count, xOffset, yOffset, zOffset, speed, arguments);
		}
	}

	@Override
	public Entity getEntityFromUuid(UUID uuid) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getEntityFromUuid(uuid);
		} else if (m_realWorld != null) {
			return m_realWorld.getEntityFromUuid(uuid);
		} else {
			return super.getEntityFromUuid(uuid);
		}
	}

	@Override
	public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.addScheduledTask(runnableToSchedule);
		} else if (m_realWorld != null) {
			return m_realWorld.addScheduledTask(runnableToSchedule);
		} else {
			return super.addScheduledTask(runnableToSchedule);
		}
	}

	@Override
	public boolean isCallingFromMinecraftThread() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isCallingFromMinecraftThread();
		} else if (m_realWorld != null) {
			return m_realWorld.isCallingFromMinecraftThread();
		} else {
			return super.isCallingFromMinecraftThread();
		}
	}

	@Override
	public AdvancementManager getAdvancementManager() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getAdvancementManager();
		} else if (m_realWorld != null) {
			return m_realWorld.getAdvancementManager();
		} else {
			return super.getAdvancementManager();
		}
	}

	@Override
	public FunctionManager getFunctionManager() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getFunctionManager();
		} else if (m_realWorld != null) {
			return m_realWorld.getFunctionManager();
		} else {
			return super.getFunctionManager();
		}
	}

	@Override
	public File getChunkSaveLocation() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getChunkSaveLocation();
		} else if (m_realWorld != null) {
			return m_realWorld.getChunkSaveLocation();
		} else {
			return super.getChunkSaveLocation();
		}
	}

	@Override
	public World init() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.init();
		} else if (m_realWorld != null) {
			return m_realWorld.init();
		} else {
			return super.init();
		}
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getBiome(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getBiome(pos);
		} else {
			return super.getBiome(pos);
		}
	}

	@Override
	public Biome getBiomeForCoordsBody(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getBiomeForCoordsBody(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getBiomeForCoordsBody(pos);
		} else {
			return super.getBiomeForCoordsBody(pos);
		}
	}

	@Override
	public BiomeProvider getBiomeProvider() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getBiomeProvider();
		} else if (m_realWorld != null) {
			return m_realWorld.getBiomeProvider();
		} else {
			return super.getBiomeProvider();
		}
	}

	@Override
	public void initialize(WorldSettings settings) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.initialize(settings);
		} else if (m_realWorld != null) {
			m_realWorld.initialize(settings);
		} else {
			super.initialize(settings);
		}
	}

	@Override
	public MinecraftServer getMinecraftServer() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getMinecraftServer();
		} else if (m_realWorld != null) {
			return m_realWorld.getMinecraftServer();
		} else {
			return super.getMinecraftServer();
		}
	}

	@Override
	public void setInitialSpawnLocation() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setInitialSpawnLocation();
		} else if (m_realWorld != null) {
			m_realWorld.setInitialSpawnLocation();
		} else {
			super.setInitialSpawnLocation();
		}
	}

	@Override
	public IBlockState getGroundAboveSeaLevel(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getGroundAboveSeaLevel(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getGroundAboveSeaLevel(pos);
		} else {
			return super.getGroundAboveSeaLevel(pos);
		}
	}

	@Override
	public boolean isValid(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isValid(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isValid(pos);
		} else {
			return super.isValid(pos);
		}
	}

	@Override
	public boolean isOutsideBuildHeight(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isOutsideBuildHeight(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isOutsideBuildHeight(pos);
		} else {
			return super.isOutsideBuildHeight(pos);
		}
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isAirBlock(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isAirBlock(pos);
		} else {
			return super.isAirBlock(pos);
		}
	}

	@Override
	public boolean isBlockLoaded(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockLoaded(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockLoaded(pos);
		} else {
			return super.isBlockLoaded(pos);
		}
	}

	@Override
	public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockLoaded(pos, allowEmpty);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockLoaded(pos, allowEmpty);
		} else {
			return super.isBlockLoaded(pos, allowEmpty);
		}
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int radius) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isAreaLoaded(center, radius);
		} else if (m_realWorld != null) {
			return m_realWorld.isAreaLoaded(center, radius);
		} else {
			return super.isAreaLoaded(center, radius);
		}
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isAreaLoaded(center, radius, allowEmpty);
		} else if (m_realWorld != null) {
			return m_realWorld.isAreaLoaded(center, radius, allowEmpty);
		} else {
			return super.isAreaLoaded(center, radius, allowEmpty);
		}
	}

	@Override
	public boolean isAreaLoaded(BlockPos from, BlockPos to) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isAreaLoaded(from, to);
		} else if (m_realWorld != null) {
			return m_realWorld.isAreaLoaded(from, to);
		} else {
			return super.isAreaLoaded(from, to);
		}
	}

	@Override
	public boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isAreaLoaded(from, to, allowEmpty);
		} else if (m_realWorld != null) {
			return m_realWorld.isAreaLoaded(from, to, allowEmpty);
		} else {
			return super.isAreaLoaded(from, to, allowEmpty);
		}
	}

	@Override
	public boolean isAreaLoaded(StructureBoundingBox box) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isAreaLoaded(box);
		} else if (m_realWorld != null) {
			return m_realWorld.isAreaLoaded(box);
		} else {
			return super.isAreaLoaded(box);
		}
	}

	@Override
	public boolean isAreaLoaded(StructureBoundingBox box, boolean allowEmpty) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isAreaLoaded(box, allowEmpty);
		} else if (m_realWorld != null) {
			return m_realWorld.isAreaLoaded(box, allowEmpty);
		} else {
			return super.isAreaLoaded(box, allowEmpty);
		}
	}

	@Override
	public Chunk getChunkFromBlockCoords(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getChunkFromBlockCoords(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getChunkFromBlockCoords(pos);
		} else {
			return super.getChunkFromBlockCoords(pos);
		}
	}

	@Override
	public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getChunkFromChunkCoords(chunkX, chunkZ);
		} else if (m_realWorld != null) {
			return m_realWorld.getChunkFromChunkCoords(chunkX, chunkZ);
		} else {
			return super.getChunkFromChunkCoords(chunkX, chunkZ);
		}
	}

	@Override
	public boolean isChunkGeneratedAt(int x, int z) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isChunkGeneratedAt(x, z);
		} else if (m_realWorld != null) {
			return m_realWorld.isChunkGeneratedAt(x, z);
		} else {
			return super.isChunkGeneratedAt(x, z);
		}
	}

	@Override
	public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.setBlockState(pos, newState, flags);
		} else if (m_realWorld != null) {
			return m_realWorld.setBlockState(pos, newState, flags);
		} else {
			return super.setBlockState(pos, newState, flags);
		}
	}

	@Override
	public void markAndNotifyBlock(BlockPos pos, Chunk chunk, IBlockState iblockstate, IBlockState newState,
			int flags) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
		} else if (m_realWorld != null) {
			m_realWorld.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
		} else {
			super.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
		}
	}

	@Override
	public boolean setBlockToAir(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.setBlockToAir(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.setBlockToAir(pos);
		} else {
			return super.setBlockToAir(pos);
		}
	}

	@Override
	public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.destroyBlock(pos, dropBlock);
		} else if (m_realWorld != null) {
			return m_realWorld.destroyBlock(pos, dropBlock);
		} else {
			return super.destroyBlock(pos, dropBlock);
		}
	}

	@Override
	public boolean setBlockState(BlockPos pos, IBlockState state) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.setBlockState(pos, state);
		} else if (m_realWorld != null) {
			return m_realWorld.setBlockState(pos, state);
		} else {
			return super.setBlockState(pos, state);
		}
	}

	@Override
	public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.notifyBlockUpdate(pos, oldState, newState, flags);
		} else if (m_realWorld != null) {
			m_realWorld.notifyBlockUpdate(pos, oldState, newState, flags);
		} else {
			super.notifyBlockUpdate(pos, oldState, newState, flags);
		}
	}

	@Override
	public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean p_175722_3_) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.notifyNeighborsRespectDebug(pos, blockType, p_175722_3_);
		} else if (m_realWorld != null) {
			m_realWorld.notifyNeighborsRespectDebug(pos, blockType, p_175722_3_);
		} else {
			super.notifyNeighborsRespectDebug(pos, blockType, p_175722_3_);
		}
	}

	@Override
	public void markBlocksDirtyVertical(int x1, int z1, int x2, int z2) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.markBlocksDirtyVertical(x1, z1, x2, z2);
		} else if (m_realWorld != null) {
			m_realWorld.markBlocksDirtyVertical(x1, z1, x2, z2);
		} else {
			super.markBlocksDirtyVertical(x1, z1, x2, z2);
		}
	}

	@Override
	public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.markBlockRangeForRenderUpdate(rangeMin, rangeMax);
		} else if (m_realWorld != null) {
			m_realWorld.markBlockRangeForRenderUpdate(rangeMin, rangeMax);
		} else {
			super.markBlockRangeForRenderUpdate(rangeMin, rangeMax);
		}
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
		} else if (m_realWorld != null) {
			m_realWorld.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
		} else {
			super.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
		}
	}

	@Override
	public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.updateObservingBlocksAt(pos, blockType);
		} else if (m_realWorld != null) {
			m_realWorld.updateObservingBlocksAt(pos, blockType);
		} else {
			super.updateObservingBlocksAt(pos, blockType);
		}
	}

	@Override
	public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean updateObservers) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.notifyNeighborsOfStateChange(pos, blockType, updateObservers);
		} else if (m_realWorld != null) {
			m_realWorld.notifyNeighborsOfStateChange(pos, blockType, updateObservers);
		} else {
			super.notifyNeighborsOfStateChange(pos, blockType, updateObservers);
		}
	}

	@Override
	public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
		} else if (m_realWorld != null) {
			m_realWorld.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
		} else {
			super.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
		}
	}

	@Override
	public void neighborChanged(BlockPos pos, Block p_190524_2_, BlockPos p_190524_3_) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.neighborChanged(pos, p_190524_2_, p_190524_3_);
		} else if (m_realWorld != null) {
			m_realWorld.neighborChanged(pos, p_190524_2_, p_190524_3_);
		} else {
			super.neighborChanged(pos, p_190524_2_, p_190524_3_);
		}
	}

	@Override
	public void observedNeighborChanged(BlockPos pos, Block p_190529_2_, BlockPos p_190529_3_) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.observedNeighborChanged(pos, p_190529_2_, p_190529_3_);
		} else if (m_realWorld != null) {
			m_realWorld.observedNeighborChanged(pos, p_190529_2_, p_190529_3_);
		} else {
			super.observedNeighborChanged(pos, p_190529_2_, p_190529_3_);
		}
	}

	@Override
	public boolean isBlockTickPending(BlockPos pos, Block blockType) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockTickPending(pos, blockType);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockTickPending(pos, blockType);
		} else {
			return super.isBlockTickPending(pos, blockType);
		}
	}

	@Override
	public boolean canSeeSky(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canSeeSky(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.canSeeSky(pos);
		} else {
			return super.canSeeSky(pos);
		}
	}

	@Override
	public boolean canBlockSeeSky(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canBlockSeeSky(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.canBlockSeeSky(pos);
		} else {
			return super.canBlockSeeSky(pos);
		}
	}

	@Override
	public int getLight(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLight(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getLight(pos);
		} else {
			return super.getLight(pos);
		}
	}

	@Override
	public int getLightFromNeighbors(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLightFromNeighbors(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getLightFromNeighbors(pos);
		} else {
			return super.getLightFromNeighbors(pos);
		}
	}

	@Override
	public int getLight(BlockPos pos, boolean checkNeighbors) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLight(pos, checkNeighbors);
		} else if (m_realWorld != null) {
			return m_realWorld.getLight(pos, checkNeighbors);
		} else {
			return super.getLight(pos, checkNeighbors);
		}
	}

	@Override
	public BlockPos getHeight(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getHeight(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getHeight(pos);
		} else {
			return super.getHeight(pos);
		}
	}

	@Override
	public int getHeight(int x, int z) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getHeight(x, z);
		} else if (m_realWorld != null) {
			return m_realWorld.getHeight(x, z);
		} else {
			return super.getHeight(x, z);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getChunksLowestHorizon(int x, int z) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getChunksLowestHorizon(x, z);
		} else if (m_realWorld != null) {
			return m_realWorld.getChunksLowestHorizon(x, z);
		} else {
			return super.getChunksLowestHorizon(x, z);
		}
	}

	@Override
	public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLightFromNeighborsFor(type, pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getLightFromNeighborsFor(type, pos);
		} else {
			return super.getLightFromNeighborsFor(type, pos);
		}
	}

	@Override
	public int getLightFor(EnumSkyBlock type, BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLightFor(type, pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getLightFor(type, pos);
		} else {
			return super.getLightFor(type, pos);
		}
	}

	@Override
	public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setLightFor(type, pos, lightValue);
		} else if (m_realWorld != null) {
			m_realWorld.setLightFor(type, pos, lightValue);
		} else {
			super.setLightFor(type, pos, lightValue);
		}
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.notifyLightSet(pos);
		} else if (m_realWorld != null) {
			m_realWorld.notifyLightSet(pos);
		} else {
			super.notifyLightSet(pos);
		}
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCombinedLight(pos, lightValue);
		} else if (m_realWorld != null) {
			return m_realWorld.getCombinedLight(pos, lightValue);
		} else {
			return super.getCombinedLight(pos, lightValue);
		}
	}

	@Override
	public float getLightBrightness(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLightBrightness(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getLightBrightness(pos);
		} else {
			return super.getLightBrightness(pos);
		}
	}

	@Override
	public boolean isDaytime() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isDaytime();
		} else if (m_realWorld != null) {
			return m_realWorld.isDaytime();
		} else {
			return super.isDaytime();
		}
	}

	@Override
	public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.rayTraceBlocks(start, end);
		} else if (m_realWorld != null) {
			return m_realWorld.rayTraceBlocks(start, end);
		} else {
			return super.rayTraceBlocks(start, end);
		}
	}

	@Override
	public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.rayTraceBlocks(start, end, stopOnLiquid);
		} else if (m_realWorld != null) {
			return m_realWorld.rayTraceBlocks(start, end, stopOnLiquid);
		} else {
			return super.rayTraceBlocks(start, end, stopOnLiquid);
		}
	}

	@Override
	public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
			boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		} else if (m_realWorld != null) {
			return m_realWorld.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		} else {
			return super.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		}
	}

	@Override
	public void playSound(EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume,
			float pitch) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.playSound(player, pos, soundIn, category, volume, pitch);
		} else if (m_realWorld != null) {
			m_realWorld.playSound(player, pos, soundIn, category, volume, pitch);
		} else {
			super.playSound(player, pos, soundIn, category, volume, pitch);
		}
	}

	@Override
	public void playSound(EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category,
			float volume, float pitch) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.playSound(player, x, y, z, soundIn, category, volume, pitch);
		} else if (m_realWorld != null) {
			m_realWorld.playSound(player, x, y, z, soundIn, category, volume, pitch);
		} else {
			super.playSound(player, x, y, z, soundIn, category, volume, pitch);
		}
	}

	@Override
	public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume,
			float pitch, boolean distanceDelay) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
		} else if (m_realWorld != null) {
			m_realWorld.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
		} else {
			super.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
		}
	}

	@Override
	public void playRecord(BlockPos blockPositionIn, SoundEvent soundEventIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.playRecord(blockPositionIn, soundEventIn);
		} else if (m_realWorld != null) {
			m_realWorld.playRecord(blockPositionIn, soundEventIn);
		} else {
			super.playRecord(blockPositionIn, soundEventIn);
		}
	}

	@Override
	public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord,
			double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
		} else if (m_realWorld != null) {
			m_realWorld.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
		} else {
			super.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
		}
	}

	@Override
	public void spawnAlwaysVisibleParticle(int p_190523_1_, double p_190523_2_, double p_190523_4_, double p_190523_6_,
			double p_190523_8_, double p_190523_10_, double p_190523_12_, int... p_190523_14_) {
		if (m_proxyWorld != null) {
			m_proxyWorld.spawnAlwaysVisibleParticle(p_190523_1_, p_190523_2_, p_190523_4_, p_190523_6_, p_190523_8_, p_190523_10_,
					p_190523_12_, p_190523_14_);
		} else {
			super.spawnAlwaysVisibleParticle(p_190523_1_, p_190523_2_, p_190523_4_, p_190523_6_, p_190523_8_, p_190523_10_,
					p_190523_12_, p_190523_14_);
		}

	}

	@Override
	public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord,
			double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
		} else if (m_realWorld != null) {
			m_realWorld.spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
		} else {
			super.spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
		}
	}

	@Override
	public boolean addWeatherEffect(Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.addWeatherEffect(entityIn);
		} else if (m_realWorld != null) {
			return m_realWorld.addWeatherEffect(entityIn);
		} else {
			return super.addWeatherEffect(entityIn);
		}
	}

	@Override
	public boolean spawnEntity(Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.spawnEntity(entityIn);
		} else if (m_realWorld != null) {
			return m_realWorld.spawnEntity(entityIn);
		} else {
			return super.spawnEntity(entityIn);
		}
	}

	@Override
	public void onEntityAdded(Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.onEntityAdded(entityIn);
		} else if (m_realWorld != null) {
			m_realWorld.onEntityAdded(entityIn);
		} else {
			super.onEntityAdded(entityIn);
		}
	}

	@Override
	public void onEntityRemoved(Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.onEntityRemoved(entityIn);
		} else if (m_realWorld != null) {
			m_realWorld.onEntityRemoved(entityIn);
		} else {
			super.onEntityRemoved(entityIn);
		}
	}

	@Override
	public void removeEntity(Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.removeEntity(entityIn);
		} else if (m_realWorld != null) {
			m_realWorld.removeEntity(entityIn);
		} else {
			super.removeEntity(entityIn);
		}
	}

	@Override
	public void removeEntityDangerously(Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.removeEntityDangerously(entityIn);
		} else if (m_realWorld != null) {
			m_realWorld.removeEntityDangerously(entityIn);
		} else {
			super.removeEntityDangerously(entityIn);
		}
	}

	@Override
	public void addEventListener(IWorldEventListener listener) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.addEventListener(listener);
		} else if (m_realWorld != null) {
			m_realWorld.addEventListener(listener);
		} else {
			super.addEventListener(listener);
		}
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(Entity entityIn, AxisAlignedBB aabb) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCollisionBoxes(entityIn, aabb);
		} else if (m_realWorld != null) {
			return m_realWorld.getCollisionBoxes(entityIn, aabb);
		} else {
			return super.getCollisionBoxes(entityIn, aabb);
		}
	}

	@Override
	public void removeEventListener(IWorldEventListener listener) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.removeEventListener(listener);
		} else if (m_realWorld != null) {
			m_realWorld.removeEventListener(listener);
		} else {
			super.removeEventListener(listener);
		}
	}

	@Override
	public boolean isInsideWorldBorder(Entity p_191503_1_) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isInsideWorldBorder(p_191503_1_);
		} else if (m_realWorld != null) {
			return m_realWorld.isInsideWorldBorder(p_191503_1_);
		} else {
			return super.isInsideWorldBorder(p_191503_1_);
		}
	}

	@Override
	public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.collidesWithAnyBlock(bbox);
		} else if (m_realWorld != null) {
			return m_realWorld.collidesWithAnyBlock(bbox);
		} else {
			return super.collidesWithAnyBlock(bbox);
		}
	}

	@Override
	public int calculateSkylightSubtracted(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.calculateSkylightSubtracted(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.calculateSkylightSubtracted(partialTicks);
		} else {
			return super.calculateSkylightSubtracted(partialTicks);
		}
	}

	@Override
	public float getSunBrightnessFactor(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSunBrightnessFactor(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getSunBrightnessFactor(partialTicks);
		} else {
			return super.getSunBrightnessFactor(partialTicks);
		}
	}

	@Override
	public float getSunBrightness(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSunBrightness(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getSunBrightness(partialTicks);
		} else {
			return super.getSunBrightness(partialTicks);
		}
	}

	@Override
	public float getSunBrightnessBody(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSunBrightnessBody(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getSunBrightnessBody(partialTicks);
		} else {
			return super.getSunBrightnessBody(partialTicks);
		}
	}

	@Override
	public Vec3d getSkyColor(Entity entityIn, float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSkyColor(entityIn, partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getSkyColor(entityIn, partialTicks);
		} else {
			return super.getSkyColor(entityIn, partialTicks);
		}
	}

	@Override
	public Vec3d getSkyColorBody(Entity entityIn, float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSkyColorBody(entityIn, partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getSkyColorBody(entityIn, partialTicks);
		} else {
			return super.getSkyColorBody(entityIn, partialTicks);
		}
	}

	@Override
	public float getCelestialAngle(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCelestialAngle(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getCelestialAngle(partialTicks);
		} else {
			return super.getCelestialAngle(partialTicks);
		}
	}

	@Override
	public int getMoonPhase() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getMoonPhase();
		} else if (m_realWorld != null) {
			return m_realWorld.getMoonPhase();
		} else {
			return super.getMoonPhase();
		}
	}

	@Override
	public float getCurrentMoonPhaseFactor() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCurrentMoonPhaseFactor();
		} else if (m_realWorld != null) {
			return m_realWorld.getCurrentMoonPhaseFactor();
		} else {
			return super.getCurrentMoonPhaseFactor();
		}
	}

	@Override
	public float getCurrentMoonPhaseFactorBody() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCurrentMoonPhaseFactorBody();
		} else if (m_realWorld != null) {
			return m_realWorld.getCurrentMoonPhaseFactorBody();
		} else {
			return super.getCurrentMoonPhaseFactorBody();
		}
	}

	@Override
	public float getCelestialAngleRadians(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCelestialAngleRadians(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getCelestialAngleRadians(partialTicks);
		} else {
			return super.getCelestialAngleRadians(partialTicks);
		}
	}

	@Override
	public Vec3d getCloudColour(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCloudColour(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getCloudColour(partialTicks);
		} else {
			return super.getCloudColour(partialTicks);
		}
	}

	@Override
	public Vec3d getCloudColorBody(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCloudColorBody(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getCloudColorBody(partialTicks);
		} else {
			return super.getCloudColorBody(partialTicks);
		}
	}

	@Override
	public Vec3d getFogColor(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getFogColor(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getFogColor(partialTicks);
		} else {
			return super.getFogColor(partialTicks);
		}
	}

	@Override
	public BlockPos getPrecipitationHeight(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPrecipitationHeight(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getPrecipitationHeight(pos);
		} else {
			return super.getPrecipitationHeight(pos);
		}
	}

	@Override
	public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getTopSolidOrLiquidBlock(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getTopSolidOrLiquidBlock(pos);
		} else {
			return super.getTopSolidOrLiquidBlock(pos);
		}
	}

	@Override
	public float getStarBrightness(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getStarBrightness(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getStarBrightness(partialTicks);
		} else {
			return super.getStarBrightness(partialTicks);
		}
	}

	@Override
	public float getStarBrightnessBody(float partialTicks) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getStarBrightnessBody(partialTicks);
		} else if (m_realWorld != null) {
			return m_realWorld.getStarBrightnessBody(partialTicks);
		} else {
			return super.getStarBrightnessBody(partialTicks);
		}
	}

	@Override
	public boolean isUpdateScheduled(BlockPos pos, Block blk) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isUpdateScheduled(pos, blk);
		} else if (m_realWorld != null) {
			return m_realWorld.isUpdateScheduled(pos, blk);
		} else {
			return super.isUpdateScheduled(pos, blk);
		}
	}

	@Override
	public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.scheduleUpdate(pos, blockIn, delay);
		} else if (m_realWorld != null) {
			m_realWorld.scheduleUpdate(pos, blockIn, delay);
		} else {
			super.scheduleUpdate(pos, blockIn, delay);
		}
	}

	@Override
	public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.updateBlockTick(pos, blockIn, delay, priority);
		} else if (m_realWorld != null) {
			m_realWorld.updateBlockTick(pos, blockIn, delay, priority);
		} else {
			super.updateBlockTick(pos, blockIn, delay, priority);
		}
	}

	@Override
	public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.scheduleBlockUpdate(pos, blockIn, delay, priority);
		} else if (m_realWorld != null) {
			m_realWorld.scheduleBlockUpdate(pos, blockIn, delay, priority);
		} else {
			super.scheduleBlockUpdate(pos, blockIn, delay, priority);
		}
	}

	@Override
	public void updateEntities() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.updateEntities();
		} else if (m_realWorld != null) {
			m_realWorld.updateEntities();
		} else {
			super.updateEntities();
		}
	}

	@Override
	public boolean addTileEntity(TileEntity tile) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.addTileEntity(tile);
		} else if (m_realWorld != null) {
			return m_realWorld.addTileEntity(tile);
		} else {
			return super.addTileEntity(tile);
		}
	}

	@Override
	public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.addTileEntities(tileEntityCollection);
		} else if (m_realWorld != null) {
			m_realWorld.addTileEntities(tileEntityCollection);
		} else {
			super.addTileEntities(tileEntityCollection);
		}
	}

	@Override
	public void updateEntity(Entity ent) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.updateEntity(ent);
		} else if (m_realWorld != null) {
			m_realWorld.updateEntity(ent);
		} else {
			super.updateEntity(ent);
		}
	}

	@Override
	public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.updateEntityWithOptionalForce(entityIn, forceUpdate);
		} else if (m_realWorld != null) {
			m_realWorld.updateEntityWithOptionalForce(entityIn, forceUpdate);
		} else {
			super.updateEntityWithOptionalForce(entityIn, forceUpdate);
		}
	}

	@Override
	public boolean checkNoEntityCollision(AxisAlignedBB bb) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.checkNoEntityCollision(bb);
		} else if (m_realWorld != null) {
			return m_realWorld.checkNoEntityCollision(bb);
		} else {
			return super.checkNoEntityCollision(bb);
		}
	}

	@Override
	public boolean checkNoEntityCollision(AxisAlignedBB bb, Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.checkNoEntityCollision(bb, entityIn);
		} else if (m_realWorld != null) {
			return m_realWorld.checkNoEntityCollision(bb, entityIn);
		} else {
			return super.checkNoEntityCollision(bb, entityIn);
		}
	}

	@Override
	public boolean checkBlockCollision(AxisAlignedBB bb) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.checkBlockCollision(bb);
		} else if (m_realWorld != null) {
			return m_realWorld.checkBlockCollision(bb);
		} else {
			return super.checkBlockCollision(bb);
		}
	}

	@Override
	public boolean containsAnyLiquid(AxisAlignedBB bb) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.containsAnyLiquid(bb);
		} else if (m_realWorld != null) {
			return m_realWorld.containsAnyLiquid(bb);
		} else {
			return super.containsAnyLiquid(bb);
		}
	}

	@Override
	public boolean isFlammableWithin(AxisAlignedBB bb) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isFlammableWithin(bb);
		} else if (m_realWorld != null) {
			return m_realWorld.isFlammableWithin(bb);
		} else {
			return super.isFlammableWithin(bb);
		}
	}

	@Override
	public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.handleMaterialAcceleration(bb, materialIn, entityIn);
		} else if (m_realWorld != null) {
			return m_realWorld.handleMaterialAcceleration(bb, materialIn, entityIn);
		} else {
			return super.handleMaterialAcceleration(bb, materialIn, entityIn);
		}
	}

	@Override
	public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isMaterialInBB(bb, materialIn);
		} else if (m_realWorld != null) {
			return m_realWorld.isMaterialInBB(bb, materialIn);
		} else {
			return super.isMaterialInBB(bb, materialIn);
		}
	}

	@Override
	public Explosion createExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isSmoking) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.createExplosion(entityIn, x, y, z, strength, isSmoking);
		} else if (m_realWorld != null) {
			return m_realWorld.createExplosion(entityIn, x, y, z, strength, isSmoking);
		} else {
			return super.createExplosion(entityIn, x, y, z, strength, isSmoking);
		}
	}

	@Override
	public Explosion newExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isFlaming,
			boolean isSmoking) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
		} else if (m_realWorld != null) {
			return m_realWorld.newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
		} else {
			return super.newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
		}
	}

	@Override
	public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getBlockDensity(vec, bb);
		} else if (m_realWorld != null) {
			return m_realWorld.getBlockDensity(vec, bb);
		} else {
			return super.getBlockDensity(vec, bb);
		}
	}

	@Override
	public boolean extinguishFire(EntityPlayer player, BlockPos pos, EnumFacing side) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.extinguishFire(player, pos, side);
		} else if (m_realWorld != null) {
			return m_realWorld.extinguishFire(player, pos, side);
		} else {
			return super.extinguishFire(player, pos, side);
		}
	}

	@Override
	public String getDebugLoadedEntities() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getDebugLoadedEntities();
		} else if (m_realWorld != null) {
			return m_realWorld.getDebugLoadedEntities();
		} else {
			return super.getDebugLoadedEntities();
		}
	}

	@Override
	public String getProviderName() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getProviderName();
		} else if (m_realWorld != null) {
			return m_realWorld.getProviderName();
		} else {
			return super.getProviderName();
		}
	}

	@Override
	public void setTileEntity(BlockPos pos, TileEntity tileEntityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setTileEntity(pos, tileEntityIn);
		} else if (m_realWorld != null) {
			m_realWorld.setTileEntity(pos, tileEntityIn);
		} else {
			super.setTileEntity(pos, tileEntityIn);
		}
	}

	@Override
	public void removeTileEntity(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.removeTileEntity(pos);
		} else if (m_realWorld != null) {
			m_realWorld.removeTileEntity(pos);
		} else {
			super.removeTileEntity(pos);
		}
	}

	@Override
	public void markTileEntityForRemoval(TileEntity tileEntityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.markTileEntityForRemoval(tileEntityIn);
		} else if (m_realWorld != null) {
			m_realWorld.markTileEntityForRemoval(tileEntityIn);
		} else {
			super.markTileEntityForRemoval(tileEntityIn);
		}
	}

	@Override
	public boolean isBlockFullCube(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockFullCube(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockFullCube(pos);
		} else {
			return super.isBlockFullCube(pos);
		}
	}

	@Override
	public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockNormalCube(pos, _default);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockNormalCube(pos, _default);
		} else {
			return super.isBlockNormalCube(pos, _default);
		}
	}

	@Override
	public void calculateInitialSkylight() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.calculateInitialSkylight();
		} else if (m_realWorld != null) {
			m_realWorld.calculateInitialSkylight();
		} else {
			super.calculateInitialSkylight();
		}
	}

	@Override
	public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setAllowedSpawnTypes(hostile, peaceful);
		} else if (m_realWorld != null) {
			m_realWorld.setAllowedSpawnTypes(hostile, peaceful);
		} else {
			super.setAllowedSpawnTypes(hostile, peaceful);
		}
	}

	@Override
	public void tick() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.tick();
		} else if (m_realWorld != null) {
			m_realWorld.tick();
		} else {
			super.tick();
		}
	}

	@Override
	public void calculateInitialWeatherBody() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.calculateInitialWeatherBody();
		} else if (m_realWorld != null) {
			m_realWorld.calculateInitialWeatherBody();
		} else {
			super.calculateInitialWeatherBody();
		}
	}

	@Override
	public void updateWeatherBody() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.updateWeatherBody();
		} else if (m_realWorld != null) {
			m_realWorld.updateWeatherBody();
		} else {
			super.updateWeatherBody();
		}
	}

	@Override
	public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.immediateBlockTick(pos, state, random);
		} else if (m_realWorld != null) {
			m_realWorld.immediateBlockTick(pos, state, random);
		} else {
			super.immediateBlockTick(pos, state, random);
		}
	}

	@Override
	public boolean canBlockFreezeWater(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canBlockFreezeWater(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.canBlockFreezeWater(pos);
		} else {
			return super.canBlockFreezeWater(pos);
		}
	}

	@Override
	public boolean canBlockFreezeNoWater(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canBlockFreezeNoWater(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.canBlockFreezeNoWater(pos);
		} else {
			return super.canBlockFreezeNoWater(pos);
		}
	}

	@Override
	public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canBlockFreeze(pos, noWaterAdj);
		} else if (m_realWorld != null) {
			return m_realWorld.canBlockFreeze(pos, noWaterAdj);
		} else {
			return super.canBlockFreeze(pos, noWaterAdj);
		}
	}

	@Override
	public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canBlockFreezeBody(pos, noWaterAdj);
		} else if (m_realWorld != null) {
			return m_realWorld.canBlockFreezeBody(pos, noWaterAdj);
		} else {
			return super.canBlockFreezeBody(pos, noWaterAdj);
		}
	}

	@Override
	public boolean canSnowAt(BlockPos pos, boolean checkLight) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canSnowAt(pos, checkLight);
		} else if (m_realWorld != null) {
			return m_realWorld.canSnowAt(pos, checkLight);
		} else {
			return super.canSnowAt(pos, checkLight);
		}
	}

	@Override
	public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canSnowAtBody(pos, checkLight);
		} else if (m_realWorld != null) {
			return m_realWorld.canSnowAtBody(pos, checkLight);
		} else {
			return super.canSnowAtBody(pos, checkLight);
		}
	}

	@Override
	public boolean checkLight(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.checkLight(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.checkLight(pos);
		} else {
			return super.checkLight(pos);
		}
	}

	@Override
	public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.checkLightFor(lightType, pos);
		} else if (m_realWorld != null) {
			return m_realWorld.checkLightFor(lightType, pos);
		} else {
			return super.checkLightFor(lightType, pos);
		}
	}

	@Override
	public boolean tickUpdates(boolean runAllPending) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.tickUpdates(runAllPending);
		} else if (m_realWorld != null) {
			return m_realWorld.tickUpdates(runAllPending);
		} else {
			return super.tickUpdates(runAllPending);
		}
	}

	@Override
	public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean remove) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPendingBlockUpdates(chunkIn, remove);
		} else if (m_realWorld != null) {
			return m_realWorld.getPendingBlockUpdates(chunkIn, remove);
		} else {
			return super.getPendingBlockUpdates(chunkIn, remove);
		}
	}

	@Override
	public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean remove) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPendingBlockUpdates(structureBB, remove);
		} else if (m_realWorld != null) {
			return m_realWorld.getPendingBlockUpdates(structureBB, remove);
		} else {
			return super.getPendingBlockUpdates(structureBB, remove);
		}
	}

	@Override
	public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entityIn, AxisAlignedBB bb) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
		} else if (m_realWorld != null) {
			return m_realWorld.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
		} else {
			return super.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
		}
	}

	@Override
	public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox,
			Predicate<? super Entity> predicate) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
		} else if (m_realWorld != null) {
			return m_realWorld.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
		} else {
			return super.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
		}
	}

	@Override
	public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getEntities(entityType, filter);
		} else if (m_realWorld != null) {
			return m_realWorld.getEntities(entityType, filter);
		} else {
			return super.getEntities(entityType, filter);
		}
	}

	@Override
	public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPlayers(playerType, filter);
		} else if (m_realWorld != null) {
			return m_realWorld.getPlayers(playerType, filter);
		} else {
			return super.getPlayers(playerType, filter);
		}
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getEntitiesWithinAABB(classEntity, bb);
		} else if (m_realWorld != null) {
			return m_realWorld.getEntitiesWithinAABB(classEntity, bb);
		} else {
			return super.getEntitiesWithinAABB(classEntity, bb);
		}
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb,
			Predicate<? super T> filter) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getEntitiesWithinAABB(clazz, aabb, filter);
		} else if (m_realWorld != null) {
			return m_realWorld.getEntitiesWithinAABB(clazz, aabb, filter);
		} else {
			return super.getEntitiesWithinAABB(clazz, aabb, filter);
		}
	}

	@Override
	public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB aabb,
			T closestTo) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.findNearestEntityWithinAABB(entityType, aabb, closestTo);
		} else if (m_realWorld != null) {
			return m_realWorld.findNearestEntityWithinAABB(entityType, aabb, closestTo);
		} else {
			return super.findNearestEntityWithinAABB(entityType, aabb, closestTo);
		}
	}

	@Override
	public Entity getEntityByID(int id) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getEntityByID(id);
		} else if (m_realWorld != null) {
			return m_realWorld.getEntityByID(id);
		} else {
			return super.getEntityByID(id);
		}
	}

	@Override
	public List<Entity> getLoadedEntityList() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLoadedEntityList();
		} else if (m_realWorld != null) {
			return m_realWorld.getLoadedEntityList();
		} else {
			return super.getLoadedEntityList();
		}
	}

	@Override
	public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.markChunkDirty(pos, unusedTileEntity);
		} else if (m_realWorld != null) {
			m_realWorld.markChunkDirty(pos, unusedTileEntity);
		} else {
			super.markChunkDirty(pos, unusedTileEntity);
		}
	}

	@Override
	public int countEntities(Class<?> entityType) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.countEntities(entityType);
		} else if (m_realWorld != null) {
			return m_realWorld.countEntities(entityType);
		} else {
			return super.countEntities(entityType);
		}
	}

	@Override
	public void loadEntities(Collection<Entity> entityCollection) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.loadEntities(entityCollection);
		} else if (m_realWorld != null) {
			m_realWorld.loadEntities(entityCollection);
		} else {
			super.loadEntities(entityCollection);
		}
	}

	@Override
	public void unloadEntities(Collection<Entity> entityCollection) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.unloadEntities(entityCollection);
		} else if (m_realWorld != null) {
			m_realWorld.unloadEntities(entityCollection);
		} else {
			super.unloadEntities(entityCollection);
		}
	}

	@Override
	public boolean mayPlace(Block blockIn, BlockPos pos, boolean p_190527_3_, EnumFacing sidePlacedOn, Entity placer) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.mayPlace(blockIn, pos, p_190527_3_, sidePlacedOn, placer);
		} else if (m_realWorld != null) {
			return m_realWorld.mayPlace(blockIn, pos, p_190527_3_, sidePlacedOn, placer);
		} else {
			return super.mayPlace(blockIn, pos, p_190527_3_, sidePlacedOn, placer);
		}
	}

	@Override
	public int getSeaLevel() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSeaLevel();
		} else if (m_realWorld != null) {
			return m_realWorld.getSeaLevel();
		} else {
			return super.getSeaLevel();
		}
	}

	@Override
	public void setSeaLevel(int seaLevelIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setSeaLevel(seaLevelIn);
		} else if (m_realWorld != null) {
			m_realWorld.setSeaLevel(seaLevelIn);
		} else {
			super.setSeaLevel(seaLevelIn);
		}
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getStrongPower(pos, direction);
		} else if (m_realWorld != null) {
			return m_realWorld.getStrongPower(pos, direction);
		} else {
			return super.getStrongPower(pos, direction);
		}
	}

	@Override
	public WorldType getWorldType() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getWorldType();
		} else if (m_realWorld != null) {
			return m_realWorld.getWorldType();
		} else {
			return super.getWorldType();
		}
	}

	@Override
	public int getStrongPower(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getStrongPower(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getStrongPower(pos);
		} else {
			return super.getStrongPower(pos);
		}
	}

	@Override
	public boolean isSidePowered(BlockPos pos, EnumFacing side) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isSidePowered(pos, side);
		} else if (m_realWorld != null) {
			return m_realWorld.isSidePowered(pos, side);
		} else {
			return super.isSidePowered(pos, side);
		}
	}

	@Override
	public int getRedstonePower(BlockPos pos, EnumFacing facing) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getRedstonePower(pos, facing);
		} else if (m_realWorld != null) {
			return m_realWorld.getRedstonePower(pos, facing);
		} else {
			return super.getRedstonePower(pos, facing);
		}
	}

	@Override
	public boolean isBlockPowered(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockPowered(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockPowered(pos);
		} else {
			return super.isBlockPowered(pos);
		}
	}

	@Override
	public int isBlockIndirectlyGettingPowered(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockIndirectlyGettingPowered(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockIndirectlyGettingPowered(pos);
		} else {
			return super.isBlockIndirectlyGettingPowered(pos);
		}
	}

	@Override
	public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getClosestPlayerToEntity(entityIn, distance);
		} else if (m_realWorld != null) {
			return m_realWorld.getClosestPlayerToEntity(entityIn, distance);
		} else {
			return super.getClosestPlayerToEntity(entityIn, distance);
		}
	}

	@Override
	public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getNearestPlayerNotCreative(entityIn, distance);
		} else if (m_realWorld != null) {
			return m_realWorld.getNearestPlayerNotCreative(entityIn, distance);
		} else {
			return super.getNearestPlayerNotCreative(entityIn, distance);
		}
	}

	@Override
	public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getClosestPlayer(posX, posY, posZ, distance, spectator);
		} else if (m_realWorld != null) {
			return m_realWorld.getClosestPlayer(posX, posY, posZ, distance, spectator);
		} else {
			return super.getClosestPlayer(posX, posY, posZ, distance, spectator);
		}
	}

	@Override
	public EntityPlayer getClosestPlayer(double x, double y, double z, double p_190525_7_,
			Predicate<Entity> p_190525_9_) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getClosestPlayer(x, y, z, p_190525_7_, p_190525_9_);
		} else if (m_realWorld != null) {
			return m_realWorld.getClosestPlayer(x, y, z, p_190525_7_, p_190525_9_);
		} else {
			return super.getClosestPlayer(x, y, z, p_190525_7_, p_190525_9_);
		}
	}

	@Override
	public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isAnyPlayerWithinRangeAt(x, y, z, range);
		} else if (m_realWorld != null) {
			return m_realWorld.isAnyPlayerWithinRangeAt(x, y, z, range);
		} else {
			return super.isAnyPlayerWithinRangeAt(x, y, z, range);
		}
	}

	@Override
	public EntityPlayer getNearestAttackablePlayer(Entity entityIn, double maxXZDistance, double maxYDistance) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
		} else if (m_realWorld != null) {
			return m_realWorld.getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
		} else {
			return super.getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
		}
	}

	@Override
	public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance);
		} else if (m_realWorld != null) {
			return m_realWorld.getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance);
		} else {
			return super.getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance);
		}
	}

	@Override
	public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance,
			double maxYDistance, Function<EntityPlayer, Double> playerToDouble, Predicate<EntityPlayer> p_184150_12_) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
		} else if (m_realWorld != null) {
			return m_realWorld.getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
		} else {
			return super.getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
		}
	}

	@Override
	public EntityPlayer getPlayerEntityByName(String name) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPlayerEntityByName(name);
		} else if (m_realWorld != null) {
			return m_realWorld.getPlayerEntityByName(name);
		} else {
			return super.getPlayerEntityByName(name);
		}
	}

	@Override
	public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPlayerEntityByUUID(uuid);
		} else if (m_realWorld != null) {
			return m_realWorld.getPlayerEntityByUUID(uuid);
		} else {
			return super.getPlayerEntityByUUID(uuid);
		}
	}

	@Override
	public void sendQuittingDisconnectingPacket() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.sendQuittingDisconnectingPacket();
		} else if (m_realWorld != null) {
			m_realWorld.sendQuittingDisconnectingPacket();
		} else {
			super.sendQuittingDisconnectingPacket();
		}
	}

	@Override
	public void checkSessionLock() throws MinecraftException {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.checkSessionLock();
		} else if (m_realWorld != null) {
			m_realWorld.checkSessionLock();
		} else {
			super.checkSessionLock();
		}
	}

	@Override
	public void setTotalWorldTime(long worldTime) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setTotalWorldTime(worldTime);
		} else if (m_realWorld != null) {
			m_realWorld.setTotalWorldTime(worldTime);
		} else {
			super.setTotalWorldTime(worldTime);
		}
	}

	@Override
	public long getSeed() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSeed();
		} else if (m_realWorld != null) {
			return m_realWorld.getSeed();
		} else {
			return super.getSeed();
		}
	}

	@Override
	public long getTotalWorldTime() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getTotalWorldTime();
		} else if (m_realWorld != null) {
			return m_realWorld.getTotalWorldTime();
		} else {
			return super.getTotalWorldTime();
		}
	}

	@Override
	public long getWorldTime() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getWorldTime();
		} else if (m_realWorld != null) {
			return m_realWorld.getWorldTime();
		} else {
			return super.getWorldTime();
		}
	}

	@Override
	public void setWorldTime(long time) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setWorldTime(time);
		} else if (m_realWorld != null) {
			m_realWorld.setWorldTime(time);
		} else {
			super.setWorldTime(time);
		}
	}

	@Override
	public BlockPos getSpawnPoint() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSpawnPoint();
		} else if (m_realWorld != null) {
			return m_realWorld.getSpawnPoint();
		} else {
			return super.getSpawnPoint();
		}
	}

	@Override
	public void setSpawnPoint(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setSpawnPoint(pos);
		} else if (m_realWorld != null) {
			m_realWorld.setSpawnPoint(pos);
		} else {
			super.setSpawnPoint(pos);
		}
	}

	@Override
	public void joinEntityInSurroundings(Entity entityIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.joinEntityInSurroundings(entityIn);
		} else if (m_realWorld != null) {
			m_realWorld.joinEntityInSurroundings(entityIn);
		} else {
			super.joinEntityInSurroundings(entityIn);
		}
	}

	@Override
	public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockModifiable(player, pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockModifiable(player, pos);
		} else {
			return super.isBlockModifiable(player, pos);
		}
	}

	@Override
	public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.canMineBlockBody(player, pos);
		} else if (m_realWorld != null) {
			return m_realWorld.canMineBlockBody(player, pos);
		} else {
			return super.canMineBlockBody(player, pos);
		}
	}

	@Override
	public void setEntityState(Entity entityIn, byte state) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setEntityState(entityIn, state);
		} else if (m_realWorld != null) {
			m_realWorld.setEntityState(entityIn, state);
		} else {
			super.setEntityState(entityIn, state);
		}
	}

	@Override
	public ChunkProviderServer getChunkProvider() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getChunkProvider();
		} else if (m_realWorld != null) {
			return m_realWorld.getChunkProvider();
		} else {
			return super.getChunkProvider();
		}
	}

	@Override
	public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.addBlockEvent(pos, blockIn, eventID, eventParam);
		} else if (m_realWorld != null) {
			m_realWorld.addBlockEvent(pos, blockIn, eventID, eventParam);
		} else {
			super.addBlockEvent(pos, blockIn, eventID, eventParam);
		}
	}

	@Override
	public ISaveHandler getSaveHandler() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSaveHandler();
		} else if (m_realWorld != null) {
			return m_realWorld.getSaveHandler();
		} else {
			return super.getSaveHandler();
		}
	}

	@Override
	public WorldInfo getWorldInfo() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getWorldInfo();
		} else if (m_realWorld != null) {
			return m_realWorld.getWorldInfo();
		} else {
			return super.getWorldInfo();
		}
	}

	@Override
	public GameRules getGameRules() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getGameRules();
		} else if (m_realWorld != null) {
			return m_realWorld.getGameRules();
		} else {
			return super.getGameRules();
		}
	}

	@Override
	public void updateAllPlayersSleepingFlag() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.updateAllPlayersSleepingFlag();
		} else if (m_realWorld != null) {
			m_realWorld.updateAllPlayersSleepingFlag();
		} else {
			super.updateAllPlayersSleepingFlag();
		}
	}

	@Override
	public float getThunderStrength(float delta) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getThunderStrength(delta);
		} else if (m_realWorld != null) {
			return m_realWorld.getThunderStrength(delta);
		} else {
			return super.getThunderStrength(delta);
		}
	}

	@Override
	public void setThunderStrength(float strength) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setThunderStrength(strength);
		} else if (m_realWorld != null) {
			m_realWorld.setThunderStrength(strength);
		} else {
			super.setThunderStrength(strength);
		}
	}

	@Override
	public float getRainStrength(float delta) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getRainStrength(delta);
		} else if (m_realWorld != null) {
			return m_realWorld.getRainStrength(delta);
		} else {
			return super.getRainStrength(delta);
		}
	}

	@Override
	public void setRainStrength(float strength) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setRainStrength(strength);
		} else if (m_realWorld != null) {
			m_realWorld.setRainStrength(strength);
		} else {
			super.setRainStrength(strength);
		}
	}

	@Override
	public boolean isThundering() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isThundering();
		} else if (m_realWorld != null) {
			return m_realWorld.isThundering();
		} else {
			return super.isThundering();
		}
	}

	@Override
	public boolean isRaining() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isRaining();
		} else if (m_realWorld != null) {
			return m_realWorld.isRaining();
		} else {
			return super.isRaining();
		}
	}

	@Override
	public boolean isRainingAt(BlockPos position) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isRainingAt(position);
		} else if (m_realWorld != null) {
			return m_realWorld.isRainingAt(position);
		} else {
			return super.isRainingAt(position);
		}
	}

	@Override
	public boolean isBlockinHighHumidity(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isBlockinHighHumidity(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.isBlockinHighHumidity(pos);
		} else {
			return super.isBlockinHighHumidity(pos);
		}
	}

	@Override
	public MapStorage getMapStorage() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getMapStorage();
		} else if (m_realWorld != null) {
			return m_realWorld.getMapStorage();
		} else {
			return super.getMapStorage();
		}
	}

	@Override
	public void setData(String dataID, WorldSavedData worldSavedDataIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setData(dataID, worldSavedDataIn);
		} else if (m_realWorld != null) {
			m_realWorld.setData(dataID, worldSavedDataIn);
		} else {
			super.setData(dataID, worldSavedDataIn);
		}
	}

	@Override
	public WorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataID) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.loadData(clazz, dataID);
		} else if (m_realWorld != null) {
			return m_realWorld.loadData(clazz, dataID);
		} else {
			return super.loadData(clazz, dataID);
		}
	}

	@Override
	public int getUniqueDataId(String key) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getUniqueDataId(key);
		} else if (m_realWorld != null) {
			return m_realWorld.getUniqueDataId(key);
		} else {
			return super.getUniqueDataId(key);
		}
	}

	@Override
	public void playBroadcastSound(int id, BlockPos pos, int data) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.playBroadcastSound(id, pos, data);
		} else if (m_realWorld != null) {
			m_realWorld.playBroadcastSound(id, pos, data);
		} else {
			super.playBroadcastSound(id, pos, data);
		}
	}

	@Override
	public void playEvent(int type, BlockPos pos, int data) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.playEvent(type, pos, data);
		} else if (m_realWorld != null) {
			m_realWorld.playEvent(type, pos, data);
		} else {
			super.playEvent(type, pos, data);
		}
	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos pos, int data) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.playEvent(player, type, pos, data);
		} else if (m_realWorld != null) {
			m_realWorld.playEvent(player, type, pos, data);
		} else {
			super.playEvent(player, type, pos, data);
		}
	}

	@Override
	public int getHeight() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getHeight();
		} else if (m_realWorld != null) {
			return m_realWorld.getHeight();
		} else {
			return super.getHeight();
		}
	}

	@Override
	public int getActualHeight() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getActualHeight();
		} else if (m_realWorld != null) {
			return m_realWorld.getActualHeight();
		} else {
			return super.getActualHeight();
		}
	}

	@Override
	public Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
		} else if (m_realWorld != null) {
			return m_realWorld.setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
		} else {
			return super.setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
		}
	}

	@Override
	public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.addWorldInfoToCrashReport(report);
		} else if (m_realWorld != null) {
			return m_realWorld.addWorldInfoToCrashReport(report);
		} else {
			return super.addWorldInfoToCrashReport(report);
		}
	}

	@Override
	public double getHorizon() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getHorizon();
		} else if (m_realWorld != null) {
			return m_realWorld.getHorizon();
		} else {
			return super.getHorizon();
		}
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.sendBlockBreakProgress(breakerId, pos, progress);
		} else if (m_realWorld != null) {
			m_realWorld.sendBlockBreakProgress(breakerId, pos, progress);
		} else {
			super.sendBlockBreakProgress(breakerId, pos, progress);
		}
	}

	@Override
	public Calendar getCurrentDate() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCurrentDate();
		} else if (m_realWorld != null) {
			return m_realWorld.getCurrentDate();
		} else {
			return super.getCurrentDate();
		}
	}

	@Override
	public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ,
			NBTTagCompound compund) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.makeFireworks(x, y, z, motionX, motionY, motionZ, compund);
		} else if (m_realWorld != null) {
			m_realWorld.makeFireworks(x, y, z, motionX, motionY, motionZ, compund);
		} else {
			super.makeFireworks(x, y, z, motionX, motionY, motionZ, compund);
		}
	}

	@Override
	public Scoreboard getScoreboard() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getScoreboard();
		} else if (m_realWorld != null) {
			return m_realWorld.getScoreboard();
		} else {
			return super.getScoreboard();
		}
	}

	@Override
	public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.updateComparatorOutputLevel(pos, blockIn);
		} else if (m_realWorld != null) {
			m_realWorld.updateComparatorOutputLevel(pos, blockIn);
		} else {
			super.updateComparatorOutputLevel(pos, blockIn);
		}
	}

	@Override
	public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getDifficultyForLocation(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getDifficultyForLocation(pos);
		} else {
			return super.getDifficultyForLocation(pos);
		}
	}

	@Override
	public EnumDifficulty getDifficulty() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getDifficulty();
		} else if (m_realWorld != null) {
			return m_realWorld.getDifficulty();
		} else {
			return super.getDifficulty();
		}
	}

	@Override
	public int getSkylightSubtracted() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getSkylightSubtracted();
		} else if (m_realWorld != null) {
			return m_realWorld.getSkylightSubtracted();
		} else {
			return super.getSkylightSubtracted();
		}
	}

	@Override
	public void setSkylightSubtracted(int newSkylightSubtracted) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setSkylightSubtracted(newSkylightSubtracted);
		} else if (m_realWorld != null) {
			m_realWorld.setSkylightSubtracted(newSkylightSubtracted);
		} else {
			super.setSkylightSubtracted(newSkylightSubtracted);
		}
	}

	@Override
	public int getLastLightningBolt() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLastLightningBolt();
		} else if (m_realWorld != null) {
			return m_realWorld.getLastLightningBolt();
		} else {
			return super.getLastLightningBolt();
		}
	}

	@Override
	public void setLastLightningBolt(int lastLightningBoltIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.setLastLightningBolt(lastLightningBoltIn);
		} else if (m_realWorld != null) {
			m_realWorld.setLastLightningBolt(lastLightningBoltIn);
		} else {
			super.setLastLightningBolt(lastLightningBoltIn);
		}
	}

	@Override
	public VillageCollection getVillageCollection() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getVillageCollection();
		} else if (m_realWorld != null) {
			return m_realWorld.getVillageCollection();
		} else {
			return super.getVillageCollection();
		}
	}

	@Override
	public WorldBorder getWorldBorder() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getWorldBorder();
		} else if (m_realWorld != null) {
			return m_realWorld.getWorldBorder();
		} else {
			return super.getWorldBorder();
		}
	}

	@Override
	public boolean isSpawnChunk(int x, int z) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isSpawnChunk(x, z);
		} else if (m_realWorld != null) {
			return m_realWorld.isSpawnChunk(x, z);
		} else {
			return super.isSpawnChunk(x, z);
		}
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isSideSolid(pos, side);
		} else if (m_realWorld != null) {
			return m_realWorld.isSideSolid(pos, side);
		} else {
			return super.isSideSolid(pos, side);
		}
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.isSideSolid(pos, side, _default);
		} else if (m_realWorld != null) {
			return m_realWorld.isSideSolid(pos, side, _default);
		} else {
			return super.isSideSolid(pos, side, _default);
		}
	}

	@Override
	public ImmutableSetMultimap<ChunkPos, Ticket> getPersistentChunks() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPersistentChunks();
		} else if (m_realWorld != null) {
			return m_realWorld.getPersistentChunks();
		} else {
			return super.getPersistentChunks();
		}
	}

	@Override
	public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPersistentChunkIterable(chunkIterator);
		} else if (m_realWorld != null) {
			return m_realWorld.getPersistentChunkIterable(chunkIterator);
		} else {
			return super.getPersistentChunkIterable(chunkIterator);
		}
	}

	@Override
	public int getBlockLightOpacity(BlockPos pos) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getBlockLightOpacity(pos);
		} else if (m_realWorld != null) {
			return m_realWorld.getBlockLightOpacity(pos);
		} else {
			return super.getBlockLightOpacity(pos);
		}
	}

	@Override
	public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.countEntities(type, forSpawnCount);
		} else if (m_realWorld != null) {
			return m_realWorld.countEntities(type, forSpawnCount);
		} else {
			return super.countEntities(type, forSpawnCount);
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.hasCapability(capability, facing);
		} else if (m_realWorld != null) {
			return m_realWorld.hasCapability(capability, facing);
		} else {
			return super.hasCapability(capability, facing);
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getCapability(capability, facing);
		} else if (m_realWorld != null) {
			return m_realWorld.getCapability(capability, facing);
		} else {
			return super.getCapability(capability, facing);
		}
	}

	@Override
	public MapStorage getPerWorldStorage() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getPerWorldStorage();
		} else if (m_realWorld != null) {
			return m_realWorld.getPerWorldStorage();
		} else {
			return super.getPerWorldStorage();
		}
	}

	@Override
	public void sendPacketToServer(Packet<?> packetIn) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			m_proxyWorld.sendPacketToServer(packetIn);
		} else if (m_realWorld != null) {
			m_realWorld.sendPacketToServer(packetIn);
		} else {
			super.sendPacketToServer(packetIn);
		}
	}

	@Override
	public LootTableManager getLootTableManager() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.getLootTableManager();
		} else if (m_realWorld != null) {
			return m_realWorld.getLootTableManager();
		} else {
			return super.getLootTableManager();
		}
	}

	@Override
	public BlockPos findNearestStructure(String p_190528_1_, BlockPos p_190528_2_, boolean p_190528_3_) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.findNearestStructure(p_190528_1_, p_190528_2_, p_190528_3_);
		} else if (m_realWorld != null) {
			return m_realWorld.findNearestStructure(p_190528_1_, p_190528_2_, p_190528_3_);
		} else {
			return super.findNearestStructure(p_190528_1_, p_190528_2_, p_190528_3_);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.equals(obj);
		} else if (m_realWorld != null) {
			return m_realWorld.equals(obj);
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.hashCode();
		} else if (m_realWorld != null) {
			return m_realWorld.hashCode();
		} else {
			return super.hashCode();
		}
	}

	@Override
	public String toString() {
		if (m_proxyWorld != null && Util.isPrefixInCallStack(m_modPrefix)) {
			return m_proxyWorld.toString();
		} else if (m_realWorld != null) {
			return m_realWorld.toString();
		} else {
			return super.toString();
		}
	}

}
