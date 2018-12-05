package ac.uk.ucl.bioreactor.core.subsystems.serial;

import com.fazecast.jSerialComm.SerialPort;

import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.type.StirringSubsystem;

public class SerialStirringSubsystem extends SerialSubsystem implements StirringSubsystem {

	public SerialStirringSubsystem(SubsystemDescriptor descriptor, SerialPort port) {
		super(descriptor, port);
	}

	@Override
	public void setTargetStirringRate(float f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getTargetStirringRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getCurrentStirringRate() {
		// TODO Auto-generated method stub
		return 0;
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

}
