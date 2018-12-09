package ac.uk.ucl.bioreactor.core.subsystems.serial.descriptors;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.subsystems.Subsystem;
import ac.uk.ucl.bioreactor.core.subsystems.descriptors.PHSubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.serial.SerialPHSubsystem;

public class SerialPHSubsystemDescriptor extends PHSubsystemDescriptor {

	@Override
	public Subsystem createSubsystem(Context context) {
		return new SerialPHSubsystem(context, this);
	}
}
