package clayborn.universalremote.network;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import clayborn.universalremote.util.InjectionHandler;
import clayborn.universalremote.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.internal.FMLMessage;

// blasted package level protection...

public class OpenGuiWrapper extends FMLMessage.OpenGui {
	
	static Field f_windowId, f_modId, f_modGuiId, f_x, f_y, f_z;	
	static Method m_fromBytes, m_toBytes;
	
	@SuppressWarnings("rawtypes")
	public static void findFields()
	{
		int fakeWindowid = 0x99;
		String fakeModId = "xyzzy";
		int fakeModGuiId = 0x88;
		int fakeX =        0x77;
		int fakeY =        0x66;
		int fakeZ =        0x55;
		
		ByteBuf buf = Unpooled.buffer(50);
		
        buf.writeInt(fakeWindowid);
        ByteBufUtils.writeUTF8String(buf, fakeModId);
        buf.writeInt(fakeModGuiId);
        buf.writeInt(fakeX);
        buf.writeInt(fakeY);
        buf.writeInt(fakeZ);
        
        FMLMessage.OpenGui gui = new FMLMessage.OpenGui();
		
		// call both fromByte AND toBytes
        
        // note: fromBytes will write more stuff but will be ignored
        // since our values are first when toBytes is called
		
		for (Method m : FMLMessage.OpenGui.class.getDeclaredMethods())
		{
			if (m.getReturnType().equals(Void.TYPE))
			{
				Class[] params = m.getParameterTypes();
				if (params.length == 1 && params[0].equals(ByteBuf.class))
				{
					try {
						int index = buf.writerIndex();
						m.setAccessible(true);
						m.invoke(gui, buf);
						if (buf.writerIndex() > index)
						{
							m_toBytes = m;
						}
						else
						{
							m_fromBytes = m;
						}
					}
					catch (InvocationTargetException e)
					{
						if (e.getTargetException() instanceof NullPointerException)
						{
							m_toBytes = m;
						} else {
							Util.logger.logException("Failed to init OpenGuiWrapper fields!", e.getTargetException());
							return;
						}
					} 
					catch (IllegalAccessException | IllegalArgumentException e)
					{
						Util.logger.logException("Failed to init OpenGuiWrapper fields!", e);
						return;
					} 
				}
			}
		}
        
        // okay now all the fields are set... find them by value!
        
		for (Field f : FMLMessage.OpenGui.class.getDeclaredFields())
		{
			
			if (f.getType().equals(int.class))
			{
				// okay it's one of the int fields
				int value = 0;
				try {
					f.setAccessible(true);
					value = f.getInt(gui);
				} catch (IllegalAccessException e) {
					Util.logger.logException("Failed to init OpenGuiWrapper fields!", e);
					return;
				}
				
				if (value == fakeWindowid)
				{
					f_windowId = f;
				}
				else if (value == fakeModGuiId)
				{
					f_modGuiId = f;
				}
				else if (value == fakeX)
				{
					f_x = f;
				}
				else if (value == fakeY)
				{
					f_y = f;
				}
				else if (value == fakeZ)
				{
					f_z = f;
				}
				else
				{
					Util.logger.error("Unknown field {} in OpenGui!", f.getName());
				}
			}
			
			if (f.getType().equals(String.class))
			{
				f.setAccessible(true);
				f_modId = f;
			}
		}
	}
	
	public OpenGuiWrapper()
	{
		super();
	}
	
	public OpenGuiWrapper(FMLMessage.OpenGui other)
	{
        InjectionHandler.copyAllFieldsFrom(this, other, FMLMessage.OpenGui.class);
	}
	
	public int getWindowId() throws IllegalAccessException
	{
		return f_windowId.getInt(this);
	}
	
	public String getModId() throws IllegalAccessException
	{
		return (String) f_modId.get(this);
	}
	
	public int getModGuiId() throws IllegalAccessException
	{
		return f_modGuiId.getInt(this);
	}
	
	public int getX() throws IllegalAccessException
	{
		return f_x.getInt(this);
	}
	
	public int getY() throws IllegalAccessException
	{
		return f_y.getInt(this);
	}
	
	public int getZ() throws IllegalAccessException
	{
		return f_z.getInt(this);
	}
	
    void toBytes(ByteBuf buf)
    {
        try {
        	m_toBytes.invoke(this, buf);
		} catch (IllegalAccessException | InvocationTargetException e) {
			Util.logger.logException("Failed write OpenGui bytes!", e);
		}
    }

    void fromBytes(ByteBuf buf)
    {
    	try {
    		m_fromBytes.invoke(this, buf);
		} catch (IllegalAccessException | InvocationTargetException e) {
			Util.logger.logException("Failed read OpenGui bytes!", e);
		}
    }
	
}
