package clayborn.universalremote.world;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class WorldProviderProxyClient extends WorldProvider {

	protected WorldProvider m_realProvider;
	protected String m_modPrefix;
	protected int m_fakeDim;

	public WorldProviderProxyClient (WorldProvider realProvider)
	{
		m_realProvider = realProvider;
		m_fakeDim = 0;
		m_modPrefix = null;

		InjectionHandler.copyAllFieldsFrom(this, m_realProvider, WorldProvider.class);
	}

	public void setData(int fakeDim, String modClass)
	{
		m_fakeDim = fakeDim;
		m_modPrefix = Util.getClassDomainFromName(modClass);
	}

	public boolean hasData()
	{
		return m_modPrefix != null;
	}

	public void clearData()
	{
		m_fakeDim = 0;
		m_modPrefix = null;
	}

	/* Modified Functions */

	@Override
	public int getDimension() {
		if(m_realProvider != null) {
			if (Util.isPrefixInCallStack(m_modPrefix))
			{
				return m_fakeDim;
			} else {
				return m_realProvider.getDimension();
			}
		} else {
			return super.getDimension();
		}
	}

	/* Proxy Functions */

	// NOTE: the if m_realProvider != null in each function is to handle the case
	// where the super constructor calls this member function during object construction

	@Override
	public IChunkGenerator createChunkGenerator() {
		if(m_realProvider != null) {
			return m_realProvider.createChunkGenerator();
		} else {
			return super.createChunkGenerator();
		}
	}

	@Override
	public boolean canCoordinateBeSpawn(int x, int z) {
		if(m_realProvider != null) {
			return m_realProvider.canCoordinateBeSpawn(x, z);
		} else {
			return super.canCoordinateBeSpawn(x, z);
		}
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		if(m_realProvider != null) {
			return m_realProvider.calculateCelestialAngle(worldTime, partialTicks);
		} else {
			return super.calculateCelestialAngle(worldTime, partialTicks);
		}
	}

	@Override
	public int getMoonPhase(long worldTime) {
		if(m_realProvider != null) {
			return m_realProvider.getMoonPhase(worldTime);
		} else {
			return super.getMoonPhase(worldTime);
		}
	}

	@Override
	public boolean isSurfaceWorld() {
		if(m_realProvider != null) {
			return m_realProvider.isSurfaceWorld();
		} else {
			return super.isSurfaceWorld();
		}
	}

	@Override
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		if(m_realProvider != null) {
			return m_realProvider.calcSunriseSunsetColors(celestialAngle, partialTicks);
		} else {
			return super.calcSunriseSunsetColors(celestialAngle, partialTicks);
		}
	}

	@Override
	public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
		if(m_realProvider != null) {
			return m_realProvider.getFogColor(p_76562_1_, p_76562_2_);
		} else {
			return super.getFogColor(p_76562_1_, p_76562_2_);
		}
	}

	@Override
	public boolean canRespawnHere() {
		if(m_realProvider != null) {
			return m_realProvider.canRespawnHere();
		} else {
			return super.canRespawnHere();
		}
	}

	@Override
	public float getCloudHeight() {
		if(m_realProvider != null) {
			return m_realProvider.getCloudHeight();
		} else {
			return super.getCloudHeight();
		}
	}

	@Override
	public boolean isSkyColored() {
		if(m_realProvider != null) {
			return m_realProvider.isSkyColored();
		} else {
			return super.isSkyColored();
		}
	}

	@Override
	public BlockPos getSpawnCoordinate() {
		if(m_realProvider != null) {
			return m_realProvider.getSpawnCoordinate();
		} else {
			return super.getSpawnCoordinate();
		}
	}

	@Override
	public int getAverageGroundLevel() {
		if(m_realProvider != null) {
			return m_realProvider.getAverageGroundLevel();
		} else {
			return super.getAverageGroundLevel();
		}
	}

	@Override
	public double getVoidFogYFactor() {
		if(m_realProvider != null) {
			return m_realProvider.getVoidFogYFactor();
		} else {
			return super.getVoidFogYFactor();
		}
	}

	@Override
	public boolean doesXZShowFog(int x, int z) {
		if(m_realProvider != null) {
			return m_realProvider.doesXZShowFog(x, z);
		} else {
			return super.doesXZShowFog(x, z);
		}
	}

	@Override
	public BiomeProvider getBiomeProvider() {
		if(m_realProvider != null) {
			return m_realProvider.getBiomeProvider();
		} else {
			return super.getBiomeProvider();
		}
	}

	@Override
	public boolean doesWaterVaporize() {
		if(m_realProvider != null) {
			return m_realProvider.doesWaterVaporize();
		} else {
			return super.doesWaterVaporize();
		}
	}

	@Override
	public boolean hasSkyLight() {
		if(m_realProvider != null) {
			return m_realProvider.hasSkyLight();
		} else {
			return super.hasSkyLight();
		}
	}

	@Override
	public boolean isNether() {
		if(m_realProvider != null) {
			return m_realProvider.isNether();
		} else {
			return super.isNether();
		}
	}

	@Override
	public float[] getLightBrightnessTable() {
		if(m_realProvider != null) {
			return m_realProvider.getLightBrightnessTable();
		} else {
			return super.getLightBrightnessTable();
		}
	}

	@Override
	public WorldBorder createWorldBorder() {
		if(m_realProvider != null) {
			return m_realProvider.createWorldBorder();
		} else {
			return super.createWorldBorder();
		}
	}

	@Override
	public void setDimension(int dim) {
		if(m_realProvider != null) {
			m_realProvider.setDimension(dim);
		} else {
			super.setDimension(dim);
		}
	}

	@Override
	public String getSaveFolder() {
		if(m_realProvider != null) {
			return m_realProvider.getSaveFolder();
		} else {
			return super.getSaveFolder();
		}
	}

	@Override
	public double getMovementFactor() {
		if(m_realProvider != null) {
			return m_realProvider.getMovementFactor();
		} else {
			return super.getMovementFactor();
		}
	}

	@Override
	public IRenderHandler getSkyRenderer() {
		if(m_realProvider != null) {
			return m_realProvider.getSkyRenderer();
		} else {
			return super.getSkyRenderer();
		}
	}

	@Override
	public void setSkyRenderer(IRenderHandler skyRenderer) {
		if(m_realProvider != null) {
			m_realProvider.setSkyRenderer(skyRenderer);
		} else {
			super.setSkyRenderer(skyRenderer);
		}
	}

	@Override
	public IRenderHandler getCloudRenderer() {
		if(m_realProvider != null) {
			return m_realProvider.getCloudRenderer();
		} else {
			return super.getCloudRenderer();
		}
	}

	@Override
	public void setCloudRenderer(IRenderHandler renderer) {
		if(m_realProvider != null) {
			m_realProvider.setCloudRenderer(renderer);
		} else {
			super.setCloudRenderer(renderer);
		}
	}

	@Override
	public IRenderHandler getWeatherRenderer() {
		if(m_realProvider != null) {
			return m_realProvider.getWeatherRenderer();
		} else {
			return super.getWeatherRenderer();
		}
	}

	@Override
	public void setWeatherRenderer(IRenderHandler renderer) {
		if(m_realProvider != null) {
			m_realProvider.setWeatherRenderer(renderer);
		} else {
			super.setWeatherRenderer(renderer);
		}
	}

	@Override
	public void getLightmapColors(float partialTicks, float sunBrightness, float skyLight, float blockLight,
			float[] colors) {
		if(m_realProvider != null) {
			m_realProvider.getLightmapColors(partialTicks, sunBrightness, skyLight, blockLight, colors);
		} else {
			super.getLightmapColors(partialTicks, sunBrightness, skyLight, blockLight, colors);
		}
	}

	@Override
	public BlockPos getRandomizedSpawnPoint() {
		if(m_realProvider != null) {
			return m_realProvider.getRandomizedSpawnPoint();
		} else {
			return super.getRandomizedSpawnPoint();
		}
	}

	@Override
	public boolean shouldMapSpin(String entity, double x, double z, double rotation) {
		if(m_realProvider != null) {
			return m_realProvider.shouldMapSpin(entity, x, z, rotation);
		} else {
			return super.shouldMapSpin(entity, x, z, rotation);
		}
	}

	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		if(m_realProvider != null) {
			return m_realProvider.getRespawnDimension(player);
		} else {
			return super.getRespawnDimension(player);
		}
	}

	@Override
	public ICapabilityProvider initCapabilities() {
		if(m_realProvider != null) {
			return m_realProvider.initCapabilities();
		} else {
			return super.initCapabilities();
		}
	}

	@Override
	public Biome getBiomeForCoords(BlockPos pos) {
		if(m_realProvider != null) {
			return m_realProvider.getBiomeForCoords(pos);
		} else {
			return super.getBiomeForCoords(pos);
		}
	}

	@Override
	public boolean isDaytime() {
		if(m_realProvider != null) {
			return m_realProvider.isDaytime();
		} else {
			return super.isDaytime();
		}
	}

	@Override
	public float getSunBrightnessFactor(float par1) {
		if(m_realProvider != null) {
			return m_realProvider.getSunBrightnessFactor(par1);
		} else {
			return super.getSunBrightnessFactor(par1);
		}
	}

	@Override
	public float getCurrentMoonPhaseFactor() {
		if(m_realProvider != null) {
			return m_realProvider.getCurrentMoonPhaseFactor();
		} else {
			return super.getCurrentMoonPhaseFactor();
		}
	}

	@Override
	public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
		if(m_realProvider != null) {
			return m_realProvider.getSkyColor(cameraEntity, partialTicks);
		} else {
			return super.getSkyColor(cameraEntity, partialTicks);
		}
	}

	@Override
	public Vec3d getCloudColor(float partialTicks) {
		if(m_realProvider != null) {
			return m_realProvider.getCloudColor(partialTicks);
		} else {
			return super.getCloudColor(partialTicks);
		}
	}

	@Override
	public float getSunBrightness(float par1) {
		if(m_realProvider != null) {
			return m_realProvider.getSunBrightness(par1);
		} else {
			return super.getSunBrightness(par1);
		}
	}

	@Override
	public float getStarBrightness(float par1) {
		if(m_realProvider != null) {
			return m_realProvider.getStarBrightness(par1);
		} else {
			return super.getStarBrightness(par1);
		}
	}

	@Override
	public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
		if(m_realProvider != null) {
			m_realProvider.setAllowedSpawnTypes(allowHostile, allowPeaceful);
		} else {
			super.setAllowedSpawnTypes(allowHostile, allowPeaceful);
		}
	}

	@Override
	public void calculateInitialWeather() {
		if(m_realProvider != null) {
			m_realProvider.calculateInitialWeather();
		} else {
			super.calculateInitialWeather();
		}
	}

	@Override
	public void updateWeather() {
		if(m_realProvider != null) {
			m_realProvider.updateWeather();
		} else {
			super.updateWeather();
		}
	}

	@Override
	public boolean canBlockFreeze(BlockPos pos, boolean byWater) {
		if(m_realProvider != null) {
			return m_realProvider.canBlockFreeze(pos, byWater);
		} else {
			return super.canBlockFreeze(pos, byWater);
		}
	}

	@Override
	public boolean canSnowAt(BlockPos pos, boolean checkLight) {
		if(m_realProvider != null) {
			return m_realProvider.canSnowAt(pos, checkLight);
		} else {
			return super.canSnowAt(pos, checkLight);
		}
	}

	@Override
	public void setWorldTime(long time) {
		if(m_realProvider != null) {
			m_realProvider.setWorldTime(time);
		} else {
			super.setWorldTime(time);
		}
	}

	@Override
	public long getSeed() {
		if(m_realProvider != null) {
			return m_realProvider.getSeed();
		} else {
			return super.getSeed();
		}
	}

	@Override
	public long getWorldTime() {
		if(m_realProvider != null) {
			return m_realProvider.getWorldTime();
		} else {
			return super.getWorldTime();
		}
	}

	@Override
	public BlockPos getSpawnPoint() {
		if(m_realProvider != null) {
			return m_realProvider.getSpawnPoint();
		} else {
			return super.getSpawnPoint();
		}
	}

	@Override
	public void setSpawnPoint(BlockPos pos) {
		if(m_realProvider != null) {
			m_realProvider.setSpawnPoint(pos);
		} else {
			super.setSpawnPoint(pos);
		}
	}

	@Override
	public boolean canMineBlock(EntityPlayer player, BlockPos pos) {
		if(m_realProvider != null) {
			return m_realProvider.canMineBlock(player, pos);
		} else {
			return super.canMineBlock(player, pos);
		}
	}

	@Override
	public boolean isBlockHighHumidity(BlockPos pos) {
		if(m_realProvider != null) {
			return m_realProvider.isBlockHighHumidity(pos);
		} else {
			return super.isBlockHighHumidity(pos);
		}
	}

	@Override
	public int getHeight() {
		if(m_realProvider != null) {
			return m_realProvider.getHeight();
		} else {
			return super.getHeight();
		}
	}

	@Override
	public int getActualHeight() {
		if(m_realProvider != null) {
			return m_realProvider.getActualHeight();
		} else {
			return super.getActualHeight();
		}
	}

	@Override
	public double getHorizon() {
		if(m_realProvider != null) {
			return m_realProvider.getHorizon();
		} else {
			return super.getHorizon();
		}
	}

	@Override
	public void resetRainAndThunder() {
		if(m_realProvider != null) {
			m_realProvider.resetRainAndThunder();
		} else {
			super.resetRainAndThunder();
		}
	}

	@Override
	public boolean canDoLightning(Chunk chunk) {
		if(m_realProvider != null) {
			return m_realProvider.canDoLightning(chunk);
		} else {
			return super.canDoLightning(chunk);
		}
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		if(m_realProvider != null) {
			return m_realProvider.canDoRainSnowIce(chunk);
		} else {
			return super.canDoRainSnowIce(chunk);
		}
	}

	@Override
	public void onPlayerAdded(EntityPlayerMP player) {
		if(m_realProvider != null) {
			m_realProvider.onPlayerAdded(player);
		} else {
			super.onPlayerAdded(player);
		}
	}

	@Override
	public void onPlayerRemoved(EntityPlayerMP player) {
		if(m_realProvider != null) {
			m_realProvider.onPlayerRemoved(player);
		} else {
			super.onPlayerRemoved(player);
		}
	}

	@Override
	public DimensionType getDimensionType() {
		if(m_realProvider != null) {
			return m_realProvider.getDimensionType();
		} else {
			return null;
		}
	}

	@Override
	public void onWorldSave() {
		if(m_realProvider != null) {
			m_realProvider.onWorldSave();
		} else {
			super.onWorldSave();
		}
	}

	@Override
	public void onWorldUpdateEntities() {
		if(m_realProvider != null) {
			m_realProvider.onWorldUpdateEntities();
		} else {
			super.onWorldUpdateEntities();
		}
	}

	@Override
	public boolean canDropChunk(int x, int z) {
		if(m_realProvider != null) {
			return m_realProvider.canDropChunk(x, z);
		} else {
			return super.canDropChunk(x, z);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(m_realProvider != null) {
			return m_realProvider.equals(obj);
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		if(m_realProvider != null) {
			return m_realProvider.hashCode();
		} else {
			return super.hashCode();
		}
	}

	@Override
	public String toString() {
		if(m_realProvider != null) {
			return m_realProvider.toString();
		} else {
			return super.toString();
		}
	}

}
