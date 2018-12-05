package ac.uk.ucl.bioreactor.core.subsystems.descriptors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ac.uk.ucl.bioreactor.core.binding.Binding;
import ac.uk.ucl.bioreactor.core.binding.BindingFactory;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.type.TemperatureSubsystem;
import ac.uk.ucl.bioreactor.util.Logging;

public class TemperatureSubsystemDescriptor implements SubsystemDescriptor {
	
	private static final List<Binding> BINDINGS;
	static {
		List<Binding> bindings = new ArrayList<>();
		try {
			bindings.add(BindingFactory.makeSetterBinding("temperature", float.class, TemperatureSubsystem.class, "setTargetTemperature"));
		} catch (Exception e) {
			Logging.fatalError(e);
		}
		BINDINGS = Collections.unmodifiableList(bindings);
	}
	
	@Override
	public String getName() {
		return "temperature";
	}

	@Override
	public List<Binding> getBindings() {
		return BINDINGS;
	}
}
