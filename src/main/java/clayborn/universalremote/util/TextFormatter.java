package clayborn.universalremote.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class TextFormatter {

	public static ITextComponent translateAndStyle(String unlocalized, TextFormatting color)
	{
		return translateAndStyle(unlocalized, color, false);
	}
	
	public static ITextComponent translateAndStyle(String unlocalized, TextFormatting color, boolean italic)
	{
		TextComponentTranslation t = new TextComponentTranslation(unlocalized);
		Style s = new Style();
		s.setColor(color);
		s.setItalic(italic);
		t.setStyle(s);
    	
    	return t;
	}
	
	public static ITextComponent style(String unstyledString, TextFormatting color)
	{
		return translateAndStyle(unstyledString, color, false);
	}
	
	public static ITextComponent style(String unstyledString, TextFormatting color, boolean italic)
	{
		TextComponentString t = new TextComponentString(unstyledString);
		Style s = new Style();
		s.setColor(color);
		s.setItalic(italic);
		t.setStyle(s);
    	
    	return t;
	}
	
}
