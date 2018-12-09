package ac.uk.ucl.bioreactor.core.binding;

public interface BoundPropertySetter<T> extends BoundProperty<T> {

	void setValue(T newVal);
}
