package ac.uk.ucl.bioreactor.core.subsystems.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.type.TemperatureSubsystem;

public class SerialTemperatureSubsystem extends SerialSubsystem implements TemperatureSubsystem, SerialPortDataListener {

	public SerialTemperatureSubsystem(Context context, SubsystemDescriptor descriptor, SerialPort port) {
		super(context, descriptor, port, 'T', () -> context.getUiController().getTempGraph(), 20, "Temp");
	}
		
	
	@Override
	public void setTargetTemperature(float f) {
		setTargetValue(f);
	}

	@Override
	public float getCurrentTemperature() {
		return getCurrentValue();
	}

	@Override
	public float getTargetTemperature() {
		return getCurrentTarget();
	}

	@Override
	protected void onCustomPacketEvent(char header, String msg) throws Exception {
	}
}
