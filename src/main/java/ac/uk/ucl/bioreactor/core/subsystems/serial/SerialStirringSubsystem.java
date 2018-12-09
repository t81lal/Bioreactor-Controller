package ac.uk.ucl.bioreactor.core.subsystems.serial;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.type.StirringSubsystem;

public class SerialStirringSubsystem extends SerialSubsystem implements StirringSubsystem {

	public SerialStirringSubsystem(Context context, SubsystemDescriptor descriptor) {
		super(context, descriptor, 'S', () -> context.getUiController().getStirGraph(), 500, "StirRate");
	}

	@Override
	public void setTargetStirringRate(float f) {
		setTargetValue(f);
	}

	@Override
	public float getTargetStirringRate() {
		return getCurrentTarget();
	}

	@Override
	public float getCurrentStirringRate() {
		return getCurrentValue();
	}

	@Override
	protected void onCustomPacketEvent(char header, String msg) throws Exception {
		
	}
}
