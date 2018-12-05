package ac.uk.ucl.bioreactor.core.binding;

import java.lang.reflect.Method;

public class MethodBinding extends Binding {
	
	private final Method method;

	public MethodBinding(String alias, Class<?> type, Method method) {
		super(alias, type);
		this.method = method;
	}

	@Override
	protected void setValueImpl(Object instance, Object v) throws Exception {
		method.setAccessible(true);
		method.invoke(instance, v);
		method.setAccessible(false);
	}

	@Override
	public String toString() {
		return method.getDeclaringClass().getSimpleName() + "." + method.getName();
	}
}
