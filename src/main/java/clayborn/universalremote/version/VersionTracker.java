package clayborn.universalremote.version;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import clayborn.universalremote.util.Logger;

import com.google.gson.Gson;

import clayborn.universalremote.util.TextFormatter;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Based on the version tracking by rubensworks in the CyclopsCore!
// Many thanks for that! :)

public class VersionTracker {

	public static class VersionData {
		public String version;
		public String downloadUrl;
	}
	
	public static interface IVersionProvider
	{
		String getModId();
		String getUnlocalizedName();
		String getLocalizedName();
		String getVersion();
		String getVersionCheckUrl();
		Logger getLogger();
	}
		
	// honestly they don't actually need to be concurrent...
	// as long as no one calls register after downloadVerions starts...
	private static final Map<String, IVersionProvider> m_providers = new ConcurrentHashMap<String, IVersionProvider>();
	private static final Map<String, VersionData> m_downloads = new ConcurrentHashMap<String, VersionData>();
	
    public static synchronized void register(IVersionProvider provider) {
    	m_providers.put(provider.getModId(), provider);
    }
    
    private static final Gson gson = new Gson();
    
    private static AtomicBoolean downloadStarted = new AtomicBoolean(false);
    private static AtomicBoolean downloadComplete = new AtomicBoolean(false);
    private static AtomicBoolean displayedInfo = new AtomicBoolean(false);
    
    // from https://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
    
    /**
     * Compares two version strings. 
     * 
     * Use this instead of String.compareTo() for a non-lexicographical 
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     * 
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     * 
     * @param str1 a string of ordinal numbers separated by decimal points. 
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2. 
     *         The result is a positive integer if str1 is _numerically_ greater than str2. 
     *         The result is zero if the strings are _numerically_ equal.
     */
    private static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
          i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }
    
    private static boolean isUpdateNeeded(IVersionProvider provider, VersionData data)
    {
    	try {
    		return versionCompare(provider.getVersion(), data.version) < 0;
    	} catch (Exception e) { // in developer mode the string is invalid
    		return true;
    	}
    }
    
    public static synchronized void downloadVersions()
    {
    	// only call this once!
    	if (downloadStarted.get()) return;    	
    	downloadStarted.set(true);
    	
    	Thread async = new Thread(new Runnable() {
            @Override
            public void run() {
            	
            	for( IVersionProvider provider : m_providers.values())
            	{                	
                    try {                    	
                    	                    	
                    	// try to download the current information
						URL url = new URL(provider.getVersionCheckUrl());
						String download = IOUtils.toString(url, Charset.forName("UTF-8"));
						
						// try to parse the json
						VersionData data = gson.fromJson(download, VersionData.class);
						
						// log some results
						if (isUpdateNeeded(provider, data))
						{
							provider.getLogger().warn(
									String.format("%s using out of date version '%s'. Newest version is '%s'.",
											provider.getUnlocalizedName(), provider.getVersion(), data.version));
						} else {
							provider.getLogger().info("%s is up to date.");
						}
						
						// store the result
						m_downloads.put(provider.getModId(), data);
						
					} catch (IOException e) {
						provider.getLogger().warn(String.format("Unable to retreive online version data for %s.", provider.getUnlocalizedName()));
					}
            	}
            	
            	downloadComplete.set(true);
            }
    	});
    	
    	async.run();
    }
    
    private static ITextComponent buildUpdateMessage(IVersionProvider provider, VersionData data)
    {
    	ITextComponent msg = TextFormatter.addHoverText(
    				TextFormatter.style(TextFormatting.DARK_RED, "[%s] ", provider.getLocalizedName()),
    			TextFormatter.format("%s -> %s", provider.getVersion(), data.version));
    	
    	msg.appendSibling(TextFormatter.translateAndStyle(TextFormatting.WHITE, "universalremote.version.update_available", data.version));
    	
    	msg.appendSibling (
    			TextFormatter.addURLClick(
	    			TextFormatter.addHoverText(
	    					TextFormatter.style(TextFormatting.GOLD, " [%s]", TextFormatter.translate("universalremote.version.download").getUnformattedText()), 
	    			TextFormatter.translate("universalremote.version.click_to_download")),
    			data.downloadUrl)
    			);
    	
    	return msg;
    }
    
	  /**
	  * When a player tick event is received.
	  * @param event The received event.
	  */
	 @SideOnly(Side.CLIENT)
	 @SubscribeEvent(priority = EventPriority.NORMAL)
	 public synchronized void onTick(TickEvent.PlayerTickEvent event) {
		 if(event.phase == TickEvent.Phase.END && downloadComplete.get() && !displayedInfo.get()) {
			 
         	for( IVersionProvider provider : m_providers.values())
         	{
         		if (m_downloads.containsKey(provider.getModId())) {
         			VersionData data = m_downloads.get(provider.getModId());
         			if (isUpdateNeeded(provider, data))
	         		{
	         			try {
	         				event.player.sendMessage(buildUpdateMessage(provider, data));
	                    } catch (NullPointerException e) {
		                    // The player SMP connection can rarely be null at this point,
		                    // let's retry in the next tick.
		                    return;
	                    }
         			}
         		}         		
         	}
         	
         	// we did it!
         	displayedInfo.set(true);
         	
         	// try to unregister this now unneeded event
			try {
				MinecraftForge.EVENT_BUS.unregister(this);
			} catch (Exception e) { }
		 }
	 }       
	
}
