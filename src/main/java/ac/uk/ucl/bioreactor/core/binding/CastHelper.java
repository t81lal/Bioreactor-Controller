package ac.uk.ucl.bioreactor.core.binding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.SilentExitException;
import ac.uk.ucl.bioreactor.util.Logging.Level;

public class CastHelper {
	
	public static <T> BoundPropertySetter<T> fromSetter(Class<?> clazz, String methodName, Object instance, Class<T> type) {
		try {
			Method m = clazz.getDeclaredMethod(methodName, new Class<?>[] {type});
			return new BoundPropertySetter<T>() {
				@Override
				public Class<T> getType() {
					return type;
				}
				@Override
				public void setValue(T newVal) {
					try {
						m.invoke(instance, newVal);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						Logging.logThrowable(Level.ERROR, e);
					}
				}
			};
		} catch (NoSuchMethodException | SecurityException e) {
			Logging.fatalError(e);
			return null;
		}
	}
	
	public static <T> BoundPropertyGetter<T> fromGetter(Class<?> clazz, String methodName, Object instance, Class<T> type) {
		try {
			Method m = clazz.getDeclaredMethod(methodName, new Class<?>[] {});
			return new BoundPropertyGetter<T>() {
				@Override
				public Class<T> getType() {
					return type;
				}
				@SuppressWarnings("unchecked")
				@Override
				public T getValue() {
					try {
						return (T) m.invoke(instance);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						Logging.logThrowable(Level.ERROR, e);
						return null;
					}
				}
			};
		} catch (NoSuchMethodException | SecurityException e) {
			Logging.fatalError(e);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Object parseValue(Class<T> type, String v) throws Exception {
		Object val;
		try {
			if (type == Float.class || type == float.class) {
				val = Float.parseFloat(v);
			} else if (type == Integer.class || type == int.class) {
				val = Integer.parseInt(v);
			} else if (type == String.class) {
				val = v;
			} else if (type == Double.class || type == double.class) {
				val = Double.parseDouble(v);
			} else if (type == Long.class || type == long.class) {
				val = Long.parseLong(v);
			} else if (type == Boolean.class || type == boolean.class) {
				val = Boolean.parseBoolean(v);
			} else if (type == Character.class || type == char.class) {
				if (v.length() != 1) {
					throw new SilentExitException(String.format("\"%s\" is not a character", v));
				}
				val = v.charAt(0);
			} else if (type == Byte.class || type == byte.class) {
				val = Byte.valueOf(v);
			} else {
				throw new SilentExitException(String.format("Can't handle field of type %s", type));
			}
		} catch (NumberFormatException e) {
			throw new SilentExitException(String.format("\"%s\" is not of type %s", v, type));
		}
		return (T)val;
	}
}
