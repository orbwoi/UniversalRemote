package clayborn.universalremote.version;

import clayborn.universalremote.settings.Strings;
import clayborn.universalremote.util.Logger;
import clayborn.universalremote.util.TextFormatter;
import clayborn.universalremote.util.Util;

public class UniversalRemoteVersionProvider implements VersionTracker.IVersionProvider {

	@Override
	public String getModId() {
		return Strings.MODID;
	}

	@Override
	public String getUnlocalizedName() {
		return Strings.UNLOCALIZEDNAME;
	}

	@Override
	public String getLocalizedName() {
		return TextFormatter.translate("itemGroup.universalremotetab").getUnformattedText();
	}

	@Override
	public String getVersion() {
		// TODO: improve this parsing
		String[] versions = Strings.VERSION.split("-");
		if (versions.length > 1)
		{
			return versions[1];
		}
		
		return Strings.VERSION;
	}

	@Override
	public String getVersionCheckUrl() {
		return Strings.VERSIONURL;
	}

	@Override
	public Logger getLogger() {
		return Util.logger;
	}

}
