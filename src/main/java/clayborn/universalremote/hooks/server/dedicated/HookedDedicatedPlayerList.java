package clayborn.universalremote.hooks.server.dedicated;

import com.mojang.authlib.GameProfile;

import clayborn.universalremote.hooks.entity.HookedEntityPlayerMP;
import clayborn.universalremote.util.InjectionHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.dedicated.DedicatedPlayerList;

public class HookedDedicatedPlayerList extends DedicatedPlayerList {

	@SuppressWarnings("unchecked")
	public HookedDedicatedPlayerList(DedicatedPlayerList oldList) throws IllegalAccessException, NoSuchFieldException, SecurityException {
		super(oldList.getServerInstance());

		InjectionHandler.copyAllFieldsFromEx(this, oldList, DedicatedPlayerList.class);
	}

    /**
     * also checks for multiple logins across servers
     */
	@Override
    public EntityPlayerMP createPlayerForUser(GameProfile profile)
    {
		EntityPlayerMP tempPlayer = super.createPlayerForUser(profile);

		return HookedEntityPlayerMP.CreateFromExisting(tempPlayer);
    }

    /**
     * Destroys the given player entity and recreates another in the given dimension. Used when respawning after death
     * or returning from the End
     */
	@Override
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd)
    {
		EntityPlayerMP tempPlayer = super.recreatePlayerEntity(playerIn, dimension, conqueredEnd);

		return HookedEntityPlayerMP.CreateFromExisting(tempPlayer);
    }


}
