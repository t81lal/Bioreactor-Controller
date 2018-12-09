package ac.uk.ucl.bioreactor.core.binding;

public class DualDelegatingBinding<T> extends AbstractBeanBinding<T> {

	private final BoundPropertyGetter<T> getter;
	private final BoundPropertySetter<T> setter;

	public DualDelegatingBinding(String name, Class<T> type, BoundPropertyGetter<T> getter,
			BoundPropertySetter<T> setter) {
		super(name, type);
		this.getter = getter;
		this.setter = setter;
		
		if(this == getter || this == setter) {
			throw new IllegalStateException("Illegal recursive property structure");
		}
	}

	@Override
	public void setValue(T newVal) {
		checkSupports(setter);
		setter.setValue(newVal);
	}

	@Override
	public T getValue() {
		checkSupports(getter);
		return getter.getValue();
	}
	
	private void checkSupports(BoundProperty<T> b) {
		if(b == null) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean supportsGet() {
		return getter != null;
	}

	@Override
	public boolean supportsSet() {
		return setter != null;
	}
}
