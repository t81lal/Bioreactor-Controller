package ac.uk.ucl.bioreactor.core.programs;

import java.util.Collection;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.Program;
import ac.uk.ucl.bioreactor.core.binding.BindManager;
import ac.uk.ucl.bioreactor.core.binding.Binding;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class ListBindingsProgram implements Program {
	
	private final OptionParser parser = new OptionParser();
	
	@Override
	public void execute(Context context, OptionSet optionSet) throws Exception {
		BindManager bindManager = context.getBindManager();
		Collection<Binding<?>> bs = bindManager.getBindings();
		Logging.logProgram(Level.INFO, "%d registered bindings:", bs.size());
		for(Binding<?> b : bs) {
			String s = "";
			if(!b.supportsGet() || !b.supportsSet()) {
				if(b.supportsGet()) {
					s = " (read only)";
				} else {
					s = " (write only)";
				}
			}
			Logging.logProgram(Level.INFO, " %s :: (%s)%s", b.getName(), b.getType().getSimpleName(), s);
		}
	}

	@Override
	public String getName() {
		return "listbinds";
	}

	@Override
	public OptionParser getOptionParser() {
		return parser;
	}

}
