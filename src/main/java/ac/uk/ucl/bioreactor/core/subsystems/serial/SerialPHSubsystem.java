package ac.uk.ucl.bioreactor.core.subsystems.serial;

import com.fazecast.jSerialComm.SerialPort;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.type.PHSubsystem;

public class SerialPHSubsystem extends SerialSubsystem implements PHSubsystem {

	public SerialPHSubsystem(Context context, SubsystemDescriptor descriptor, SerialPort port) {
		super(context, descriptor, port, 'P', () -> context.getUiController().getPHGraph(), 7, "PH");
	}

	@Override
	public void setTargetPH(float f) {
		setTargetValue(f);
	}

	@Override
	public float getTargetPH() {
		return getCurrentTarget();
	}

	@Override
	public float getCurrentPH() {
		return getCurrentValue();
	}

	@Override
	protected void onCustomPacketEvent(char header, String msg) throws Exception {
	}
}
