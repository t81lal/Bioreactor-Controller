package ac.uk.ucl.bioreactor.core.subsystems.serial;

import com.fazecast.jSerialComm.SerialPort;

import ac.uk.ucl.bioreactor.core.subsystems.Subsystem;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;

public abstract class SerialSubsystem extends Subsystem {
	private final SerialPort port;

	public SerialSubsystem(SubsystemDescriptor descriptor, SerialPort port) {
		super(descriptor);
		this.port = port;
	}
	
	public SerialPort getPort() {
		return port;
	}
}
