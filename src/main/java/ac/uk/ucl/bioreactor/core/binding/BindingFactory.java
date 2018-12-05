package ac.uk.ucl.bioreactor.core.binding;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BindingFactory {

	public static Binding makeFieldBinding(String alias, Class<?> clazz, String fieldName) throws Exception {
		Field f = clazz.getDeclaredField(fieldName);
		return new FieldBinding(alias, f.getType(), f);
	}

	public static Binding makeSetterBinding(String alias, Class<?> type, Class<?> clazz, String methodName) throws Exception {
		Method m = clazz.getDeclaredMethod(methodName, type);
		return new MethodBinding(alias, type, m);
	}
}
