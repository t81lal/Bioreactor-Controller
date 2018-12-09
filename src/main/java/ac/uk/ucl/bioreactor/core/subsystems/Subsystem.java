package ac.uk.ucl.bioreactor.core.subsystems;

import ac.uk.ucl.bioreactor.core.Context;

public abstract class Subsystem {
	
	protected final Context context;
	protected final SubsystemDescriptor descriptor;
	
	public Subsystem(Context context, SubsystemDescriptor descriptor) {
		this.context = context;
		this.descriptor = descriptor;
	}
	
	public abstract void init();
	
	public abstract void start();
	
	public abstract void stop();

	public SubsystemDescriptor getDescriptor() {
		return descriptor;
	}
}
