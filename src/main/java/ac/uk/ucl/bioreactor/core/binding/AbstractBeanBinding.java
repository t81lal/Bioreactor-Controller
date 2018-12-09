package ac.uk.ucl.bioreactor.core.binding;

public abstract class AbstractBeanBinding<T> implements Binding<T> {

	private final String name;
	private final Class<T> type;
	
	public AbstractBeanBinding(String name, Class<T> type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<T> getType() {
		return type;
	}
}
