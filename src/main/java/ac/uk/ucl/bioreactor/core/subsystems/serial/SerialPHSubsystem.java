package ac.uk.ucl.bioreactor.core.subsystems.serial;

import com.fazecast.jSerialComm.SerialPort;

import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.type.PHSubsystem;

public class SerialPHSubsystem extends SerialSubsystem implements PHSubsystem {

	public SerialPHSubsystem(SubsystemDescriptor descriptor, SerialPort port) {
		super(descriptor, port);
	}

	@Override
	public void setTargetPH(float f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getTargetPH() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getCurrentPH() {
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
