package ac.uk.ucl.bioreactor.core;

import java.util.List;

import ac.uk.ucl.bioreactor.core.subsystems.Subsystem;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;

public interface Reactor {
	
	List<SubsystemDescriptor> getSupportedSubSystemDescriptors();
	
	List<Subsystem> getActiveSubSystems();
	
	void install(Subsystem subsystem);
}
