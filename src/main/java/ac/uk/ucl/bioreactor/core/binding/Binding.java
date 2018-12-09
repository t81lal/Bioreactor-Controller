package ac.uk.ucl.bioreactor.core.binding;

public interface Binding<T> {

	String getName();
	
	Class<T> getType();
	
	void setValue(T newVal);
	
	T getValue();
	
	boolean supportsGet();
	
	boolean supportsSet();
}
