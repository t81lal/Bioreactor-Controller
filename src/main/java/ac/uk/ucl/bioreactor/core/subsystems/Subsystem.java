package ac.uk.ucl.bioreactor.core.subsystems;

public abstract class Subsystem {
	
	private final SubsystemDescriptor descriptor;
	
	public Subsystem(SubsystemDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	public abstract void init();
	
	public abstract void start();
	
	public abstract void stop();

	public SubsystemDescriptor getDescriptor() {
		return descriptor;
	}
}
