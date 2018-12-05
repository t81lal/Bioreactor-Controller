package ac.uk.ucl.bioreactor.core.subsystems.serial;

import com.fazecast.jSerialComm.SerialPort;

import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.type.TemperatureSubsystem;

public class SerialTemperatureSubsystem extends SerialSubsystem implements TemperatureSubsystem {

	public SerialTemperatureSubsystem(SubsystemDescriptor descriptor, SerialPort port) {
		super(descriptor, port);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTargetTemperature(float f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getCurrentTemperature() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getTargetTemperature() {
		// TODO Auto-generated method stub
		return 0;
	}
}
