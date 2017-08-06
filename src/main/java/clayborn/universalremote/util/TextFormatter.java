package clayborn.universalremote.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class TextFormatter {

	public static ITextComponent translateAndStyle(String unlocalized, TextFormatting color)
	{
		return translateAndStyle(color, false, unlocalized);
	}
	
	public static ITextComponent translateAndStyle(TextFormatting color, boolean italic, String unlocalized)
	{
		TextComponentTranslation t = new TextComponentTranslation(unlocalized);
		Style s = new Style();
		s.setColor(color);
		s.setItalic(italic);
		t.setStyle(s);
    	
    	return t;
	}
	
	public static ITextComponent translateAndStyle(TextFormatting color, String unlocalized, Object... args)
	{
		TextComponentTranslation t = new TextComponentTranslation(unlocalized, args);
		Style s = new Style();
		s.setColor(color);
		t.setStyle(s);
    	
    	return t;
	}
	
	public static ITextComponent style(TextFormatting color, String unstyledString)
	{
		return style(color, false, unstyledString);
	}
	
	public static ITextComponent style(TextFormatting color, boolean italic, String unstyledString)
	{
		TextComponentString t = new TextComponentString(unstyledString);
		Style s = new Style();
		s.setColor(color);
		s.setItalic(italic);
		t.setStyle(s);
    	
    	return t;
	}
	
	public static ITextComponent style(TextFormatting color, String unstyledString, Object... args)
	{
		TextComponentString t = new TextComponentString(String.format(unstyledString, args));
		Style s = new Style();
		s.setColor(color);
		t.setStyle(s);
    	
    	return t;
	}
	
	public static ITextComponent translate(String unlocalized, Object... args)
	{
		return new TextComponentTranslation(unlocalized, args);
	}
	
	public static ITextComponent format(String unformatted, Object... args)
	{
		return new TextComponentString(String.format(unformatted, args));
	}
	
	public static ITextComponent addHoverText(ITextComponent component, ITextComponent hoverComponent)
	{
		Style s = component.getStyle();
		s.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent));
		component.setStyle(s);
		return component;
	}
	
	public static ITextComponent addURLClick(ITextComponent component, String url)
	{
		Style s = component.getStyle();
		s.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
		component.setStyle(s);
		return component;
	}

}
