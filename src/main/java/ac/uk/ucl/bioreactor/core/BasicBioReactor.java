package ac.uk.ucl.bioreactor.core;

import java.util.ArrayList;
import java.util.List;

import ac.uk.ucl.bioreactor.core.subsystems.Subsystem;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;

public class BasicBioReactor implements Reactor {

	private final List<SubsystemDescriptor> supportedDescriptors;
	private final List<Subsystem> activeSubSystems = new ArrayList<>();

	public BasicBioReactor(List<SubsystemDescriptor> supportedDescriptors) {
		this.supportedDescriptors = supportedDescriptors;
	}

	@Override
	public List<SubsystemDescriptor> getSupportedSubSystemDescriptors() {
		return supportedDescriptors;
	}

	@Override
	public List<Subsystem> getActiveSubSystems() {
		return new ArrayList<>(activeSubSystems);
	}

	@Override
	public void install(Subsystem subsystem) {
		for (Subsystem s : activeSubSystems) {
			if (s.getDescriptor().equals(subsystem.getDescriptor())) {
				throw new IllegalStateException(
						String.format("Subsystem %s already started", subsystem.getDescriptor().getName()));
			}
		}

		synchronized (activeSubSystems) {
			activeSubSystems.add(subsystem);
		}
		try {
			subsystem.start();
		} catch (Throwable t) {
			shutdownAll();
			Logging.fatalError(t);
		}
	}

	private void shutdownAll() {
		synchronized (activeSubSystems) {
			for (Subsystem s : activeSubSystems) {
				try {
					s.stop();
				} catch (Throwable t) {
					Logging.log(Level.WARN, "Error shutting down %s subsystem: %s", s.getDescriptor().getName(),
							t.getMessage());
				}
			}
		}
	}
}
