package ac.uk.ucl.bioreactor.core.subsystems.descriptors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ac.uk.ucl.bioreactor.core.binding.Binding;
import ac.uk.ucl.bioreactor.core.binding.BindingFactory;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.type.PHSubsystem;
import ac.uk.ucl.bioreactor.util.Logging;

public class PHSubsystemDescriptor implements SubsystemDescriptor {
	
	private static final List<Binding> BINDINGS;
	static {
		List<Binding> bindings = new ArrayList<>();
		try {
			bindings.add(BindingFactory.makeSetterBinding("pH", float.class, PHSubsystem.class, "setTargetPH"));
		} catch (Exception e) {
			Logging.fatalError(e);
		}
		BINDINGS = Collections.unmodifiableList(bindings);
	}
	
	@Override
	public String getName() {
		return "pH";
	}

	@Override
	public List<Binding> getBindings() {
		return BINDINGS;
	}
}
