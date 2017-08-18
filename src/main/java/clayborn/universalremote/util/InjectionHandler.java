package clayborn.universalremote.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.reflect.FieldUtils;

public class InjectionHandler {

	@SuppressWarnings("unchecked")
	public static <T> T readFieldOfType(Object o, Class<T> t) throws IllegalAccessException
	{
		
        for (Field f : FieldUtils.getAllFields(o.getClass()))
		{
        	if (!Modifier.isStatic(f.getModifiers()) && f.getType().equals(t))
        	{
        		return (T) FieldUtils.readField(f, o, true);
        	}
		}
        
        throw new IllegalAccessException();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T readStaticFieldOfType(Class c, Class<T> t) throws IllegalAccessException
	{
		
        for (Field f : FieldUtils.getAllFields(c))
		{
        	if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(t))
        	{
        		return (T) FieldUtils.readStaticField(f, true);
        	}
		}
        
        throw new IllegalAccessException();
	}
	
	@SuppressWarnings({ })
	public static <T> void writeFieldOfType(Object o, T t, Class<T> c) throws IllegalAccessException
	{

        for (Field f : FieldUtils.getAllFields(o.getClass()))
		{
        	if (!Modifier.isStatic(f.getModifiers()) && f.getType().equals(c))
        	{
        		FieldUtils.writeField(f, o, t, true);
        		return;
        	}
		}
        
        throw new IllegalAccessException();
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static <T> void writeStaticFieldOfType(Class d, T t, Class<T> c) throws IllegalAccessException
	{
		
        for (Field f : FieldUtils.getAllFields(d))
		{
        	if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(c))
        	{
        		FieldUtils.writeStaticField(f, t, true);
        		return;
        	}
		}
        
        throw new IllegalAccessException();
	}
	
	public static <T> void writeAllFieldsOfType(Object o, T t, Class<T> c) throws IllegalAccessException
	{
		boolean wroteSomething = false;		
		
        for (Field f : FieldUtils.getAllFields(o.getClass()))
		{
        	if (!Modifier.isStatic(f.getModifiers()) && f.getType().equals(c))
        	{
        		FieldUtils.writeField(f, o, t, true);
        		wroteSomething = true;
        	}
		}
                
        if (!wroteSomething) 
        	throw new IllegalAccessException();
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static <T> void writeAllStaticFieldsOfType(Class d, T t, Class c) throws IllegalAccessException
	{
		boolean wroteSomething = false;
		
        for (Field f : FieldUtils.getAllFields(d))
		{
        	if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(c))
        	{
        		FieldUtils.writeStaticField(f, t, true);
        		wroteSomething = true;
        	}
		}
        
        if (!wroteSomething) 
        	throw new IllegalAccessException();
	}
	
	public static <T> void copyAllFieldsFrom(T dest, T origin, Class<T> c)
	{
		for (Field f : FieldUtils.getAllFields(c))
		{
			if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers()))
			{
				try {
					FieldUtils.writeField(f, dest, FieldUtils.readField(f, origin, true), true);
				} catch (IllegalAccessException e) {
					Util.logger.info("Unable to force copy field {} of {}.", f.getName(), dest.getClass().getName());
				} catch (Exception e) {
					String s = String.format("Error trying to force copy field %s of %s!", f.getName(), dest.getClass().getName());
					Util.logger.logException(s, e);
				}
			}
		}
	}
	
}
