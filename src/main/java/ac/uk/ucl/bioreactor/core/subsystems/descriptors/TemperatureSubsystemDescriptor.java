package ac.uk.ucl.bioreactor.core.subsystems.descriptors;

import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;

public abstract class TemperatureSubsystemDescriptor implements SubsystemDescriptor {
	@Override
	public String getName() {
		return "temperature";
	}
}
