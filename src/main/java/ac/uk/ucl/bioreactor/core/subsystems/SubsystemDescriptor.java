package ac.uk.ucl.bioreactor.core.subsystems;

import ac.uk.ucl.bioreactor.core.Context;

public interface SubsystemDescriptor {

	String getName();
	
	Subsystem createSubsystem(Context context);
}
