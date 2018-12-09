package ac.uk.ucl.bioreactor.core.binding;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BindManager {

	private final Map<String, Binding<?>> bindings;
	
	public BindManager() {
		bindings = new ConcurrentHashMap<>();
	}
	
	public void addBinding(Binding<?> b) {
		if(b == null) {
			throw new NullPointerException();
		}
		
		String name = b.getName();
		if(name == null) {
			throw new NullPointerException();
		} else if(bindings.containsKey(name)) {
			throw new UnsupportedOperationException(String.format("Bind with name %s already exists", name));
		}
		
		bindings.put(name, b);
	}
	
	public void removeBinding(Binding<?> b) {
		if(b == null) {
			throw new NullPointerException();
		}
		
		String name = b.getName();
		if(name == null) {
			throw new NullPointerException();
		}
		bindings.remove(name);
	}
	
	public void removeBinding(String name) {
		if(name == null) {
			throw new NullPointerException();
		}
		bindings.remove(name);
	}
	
	public Binding<?> findBinding(String name) {
		if(name == null) {
			throw new NullPointerException();
		}
		if(bindings.containsKey(name)) {
			return bindings.get(name);
		} else {
			return null;
		}
	}
	
	public Collection<Binding<?>> getBindings() {
		return Collections.unmodifiableCollection(bindings.values());
	}
}
