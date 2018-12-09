package ac.uk.ucl.bioreactor.core.binding;

public class SimpleBeanBinding<T> extends AbstractBeanBinding<T> {

	private T value;
	
	public SimpleBeanBinding(String name, Class<T> type) {
		this(name, type, null);
	}
	
	public SimpleBeanBinding(String name, Class<T> type, T defaultValue) {
		super(name, type);
		this.value = defaultValue;
	}

	@Override
	public void setValue(T newVal) {
		this.value= newVal;
	}

	@Override
	public T getValue() {
		return this.value;
	}

	@Override
	public boolean supportsGet() {
		return true;
	}

	@Override
	public boolean supportsSet() {
		return true;
	}
}
