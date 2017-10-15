package clayborn.universalremote.network;

import java.io.IOException;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import clayborn.universalremote.hooks.network.OpenGuiWrapper;
import clayborn.universalremote.hooks.world.HookedClientWorld;
import clayborn.universalremote.items.ItemUniversalRemote;
import clayborn.universalremote.items.ItemUniversalRemote.ItemUniversalRemoteNBTParser;
import clayborn.universalremote.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.common.network.internal.FMLMessage.OpenGui;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RemoteGuiMessage implements IMessage {

	public static class OpenGuiFactory
	{
		public static FMLMessage.OpenGui create (int windowId, String modId, int modGuiId, int x, int y, int z)
        {
			try {
				return ConstructorUtils.invokeConstructor(FMLMessage.OpenGui.class, windowId, modId, modGuiId, x, y, z);
			} catch (Exception e) {
				Util.logger.logException("Unable to construct FMLMessage.OpenGui using factory!", e);
				return null;
			}
        }
	}

	// The params of the IMessageHandler are <REQ, REPLY>
	// This means that the first param is the packet you are receiving, and the second is the packet you are returning.
	// The returned packet can be used as a "response" from a sent packet.
	public static class RemoteGuiMessageHandler implements IMessageHandler<RemoteGuiMessage, IMessage> {

		// Do note that the default constructor is required, but implicitly defined in this case

		@SideOnly(Side.CLIENT)
		@Override public IMessage onMessage(RemoteGuiMessage message, MessageContext ctx) {

			if (ctx.side == Side.CLIENT)
			{
				Minecraft.getMinecraft().addScheduledTask(() -> {
					try {
						OpenGuiWrapper msg = message.getGui();

				        EntityPlayer player = FMLClientHandler.instance().getClient().player;

				        int x = msg.getX();
				        int y = msg.getY();
				        int z = msg.getZ();

				        IBlockState state = Block.getStateById(message.getBlockId());

				        if (player.world instanceof HookedClientWorld)
				        {
				        	((HookedClientWorld)player.world).SetRemoteGui(state, message.getUpdateTag(), message.getReadTag(), x, y, z, message.getDimensionId());
				        } else {
				        	Util.logger.error("Client world is not instance of RemoteGuiEnabledClientWorld!");
				        }

				        // WindowId of -1 means this was just a prepare message not a OpenGui replacement
				        if (msg.getWindowId() >= 0)
				        {

					        player.openGui(
					        		msg.getModId(),
					        		msg.getModGuiId(),
					        		player.world,
					        		x,
					        		y,
					        		z);

					        player.openContainer.windowId = msg.getWindowId();

				        } else {
				        	// This is a "prepare" message before server side activation

				        	NBTTagCompound remoteTag = message.getRemoteTag();
				        	ItemUniversalRemoteNBTParser myNBT = new ItemUniversalRemoteNBTParser(remoteTag);

				        	ItemUniversalRemote.ActivateBlock(player, state, myNBT, player.world);

				        }

					} catch (Exception e) {
						Util.logger.logException("Unable to process OpenRemoteGuiMessage!", e);
					}

				});
			}

			return null;

		}
	}


	private OpenGuiWrapper m_guiMsg;
	private int m_blockId;
	private NBTTagCompound m_updateTag;
	private NBTTagCompound m_readTag;
	private NBTTagCompound m_remoteTag;
	private int m_dimensionId;

	// A default constructor is always required
	public RemoteGuiMessage()
	{
		m_guiMsg = new OpenGuiWrapper();
		m_blockId = 0; //air
		m_updateTag = null;
		m_readTag = null;
		m_remoteTag = null;
		m_dimensionId = 0;
	}

	public RemoteGuiMessage(OpenGui msg, int blockId, NBTTagCompound updateTag, NBTTagCompound readTag, NBTTagCompound remoteTag, int dimensionId) {
		m_guiMsg = new OpenGuiWrapper(msg);
		m_blockId = blockId;
		m_updateTag = updateTag;
		m_readTag = readTag;
		m_remoteTag = remoteTag;
		m_dimensionId = dimensionId;
	}

	public OpenGuiWrapper getGui() {
		return m_guiMsg;
	}

	public int getBlockId() {
		return m_blockId;
	}

	public NBTTagCompound getUpdateTag() {
		return m_updateTag;
	}

	public NBTTagCompound getReadTag() {
		return m_readTag;
	}

	public NBTTagCompound getRemoteTag() {
		return m_remoteTag;
	}

	public int getDimensionId() {
		return m_dimensionId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {

		PacketBuffer ibuf = new PacketBuffer(buf);

		m_guiMsg.fromBytes(ibuf);
		m_blockId = ibuf.readInt();

		try {
			m_updateTag = ibuf.readCompoundTag();
		} catch (IOException e) {
			Util.logger.logException("Unable to read OpenRemoteGuiMessage TE update NBT tag!", e);
		}

		try {
			m_readTag = ibuf.readCompoundTag();
		} catch (IOException e) {
			Util.logger.logException("Unable to read OpenRemoteGuiMessage TE read/write NBT tag!", e);
		}

		try {
			m_remoteTag = ibuf.readCompoundTag();
		} catch (IOException e) {
			Util.logger.logException("Unable to read OpenRemoteGuiMessage Universal Remote NBT tag!", e);
		}


		m_dimensionId = ibuf.readInt();

	}

	@Override
	public void toBytes(ByteBuf buf) {

		PacketBuffer ibuf = new PacketBuffer(buf);

		m_guiMsg.toBytes(ibuf);

		ibuf.writeInt(m_blockId);
		ibuf.writeCompoundTag(m_updateTag); // internally handles null
		ibuf.writeCompoundTag(m_readTag); // internally handles null
		ibuf.writeCompoundTag(m_remoteTag); // internally handles null
		ibuf.writeInt(m_dimensionId);

	}

}
