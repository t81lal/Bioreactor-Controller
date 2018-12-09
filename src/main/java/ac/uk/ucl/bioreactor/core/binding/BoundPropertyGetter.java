package ac.uk.ucl.bioreactor.core.binding;

public interface BoundPropertyGetter<T> extends BoundProperty<T> {

	T getValue();
}
