package ac.uk.ucl.bioreactor.core.binding;

import java.lang.reflect.Field;

public class FieldBinding extends Binding {
	
	private final Field field;
	
	public FieldBinding(String alias, Class<?> type, Field field) {
		super(alias, type);
		this.field = field;
	}

	@Override
	protected void setValueImpl(Object instance, Object v) throws Exception {
		field.setAccessible(true);
		field.set(instance, v);
		field.setAccessible(false);
	}
	
	@Override
	public String toString() {
		return field.getDeclaringClass().getSimpleName() + "." + field.getName();
	}
}
