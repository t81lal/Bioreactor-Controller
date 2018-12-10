package ac.uk.ucl.bioreactor.core.subsystems.serial.descriptors;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.subsystems.Subsystem;
import ac.uk.ucl.bioreactor.core.subsystems.descriptors.StirringSubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.descriptors.TemperatureSubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.serial.SerialTemperatureSubsystem;

public class SerialStirringSubsystemDescriptor extends StirringSubsystemDescriptor {

	@Override
	public Subsystem createSubsystem(Context context) {
		return new SerialTemperatureSubsystem(context, this);
	}
}
