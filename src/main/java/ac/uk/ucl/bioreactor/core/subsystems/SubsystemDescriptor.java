package ac.uk.ucl.bioreactor.core.subsystems;

import java.util.List;

import ac.uk.ucl.bioreactor.core.binding.Binding;

public interface SubsystemDescriptor {

	String getName();
	
	List<Binding> getBindings();
}
