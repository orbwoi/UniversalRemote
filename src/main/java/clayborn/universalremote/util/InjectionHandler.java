package clayborn.universalremote.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

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

	@SuppressWarnings("unchecked")
	public static <R,T> T readFieldOfType(Class<R> r, R o, Class<T> t) throws IllegalAccessException
	{

        for (Field f : FieldUtils.getAllFields(r))
		{
        	if (!Modifier.isStatic(f.getModifiers()) && f.getType().equals(t))
        	{
        		return (T) FieldUtils.readField(f, o, true);
        	}
		}

        throw new IllegalAccessException();
	}

	@SuppressWarnings("unchecked")
	public static <C,T> C readParameterizedFieldOfType(Object o, Class<C> collection, Class<T> genertic) throws IllegalAccessException
	{

        for (Field f : FieldUtils.getAllFields(o.getClass()))
		{
        	if (!Modifier.isStatic(f.getModifiers()) && f.getType().equals(collection) && f.getGenericType().equals(genertic))
        	{
        		return (C) FieldUtils.readField(f, o, true);
        	}
		}

        throw new IllegalAccessException();
	}

	@SuppressWarnings("unchecked")
	public static <R, C, T> C readParameterizedFieldOfType(Class<R> r, R o, Class<C> collection, ParameterizedType genertic) throws IllegalAccessException
	{

        for (Field f : FieldUtils.getAllFields(r))
		{
        	if (!Modifier.isStatic(f.getModifiers()) && f.getType().equals(collection) && f.getGenericType().equals(genertic))
        	{
        		return (C) FieldUtils.readField(f, o, true);
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
				copyField(f, dest, origin);
			}
		}
	}

	public static <T> void copyAllPublicFieldsFrom(T dest, T origin, Class<T> c)
	{
		for (Field f : FieldUtils.getAllFields(c))
		{
			int modifiers = f.getModifiers();
			if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers))
			{
				copyField(f, dest, origin);
			}
		}
	}

	// also copy final fields
	public static <T> void copyAllFieldsFromEx(T dest, T origin, Class<T> c)
	{
		for (Field f : FieldUtils.getAllFields(c))
		{
			if (!Modifier.isStatic(f.getModifiers()))
			{
				copyField(f, dest, origin);
			}
		}
	}

	public interface IComparisonCallback
	{
		public void FoundDifference(Field f, Object oldValue, Object newValue);
	}

	public static <T> void comparePublicFields(T backup, T modified, Class<T> c, IComparisonCallback callback)
	{
		for (Field f : FieldUtils.getAllFields(c))
		{
			int modifiers = f.getModifiers();
			if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers))
			{
				try {
					Object newValue = FieldUtils.readField(f, modified);
					Object oldValue = FieldUtils.readField(f, backup);
					if (newValue != null && !newValue.equals(oldValue))
					{
						callback.FoundDifference(f, oldValue, newValue);
					}
					else if (oldValue != null && !oldValue.equals(newValue))
					{
						callback.FoundDifference(f, oldValue, newValue);
					}
					// else they are both null so they are the same
				} catch (IllegalAccessException e) {
					Util.logger.info("Unable to read field {} of {}.", f.getName(), c.getName());
				} catch (Exception e) {
					String s = String.format("Error trying to force read field %s of %s!", f.getName(), c.getName());
					Util.logger.logException(s, e);
				}

			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T, V> V readField(Field f, T instance)throws IllegalAccessException
	{
		return (V) FieldUtils.readField(f, instance, true);
	}

	public static <T, V> void writeField(Field f, T instance, V value)throws IllegalAccessException
	{
		FieldUtils.writeField(f, instance, value, true);
	}

	public static <T> void copyField(Field f, T dest, T origin)
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
