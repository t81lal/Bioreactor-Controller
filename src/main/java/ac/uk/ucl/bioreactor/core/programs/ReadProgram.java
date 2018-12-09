package ac.uk.ucl.bioreactor.core.programs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.Program;
import ac.uk.ucl.bioreactor.core.binding.BindManager;
import ac.uk.ucl.bioreactor.core.binding.Binding;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class ReadProgram implements Program {
	
	private final OptionParser parser;
	private final NonOptionArgumentSpec<String> varOption;
	
	public ReadProgram() {
		parser = new OptionParser(true);
		varOption = parser.nonOptions("Bind names").ofType(String.class);
	}
	
	@Override
	public void execute(Context context, OptionSet optionSet) throws Exception {
		BindManager bindManager = context.getBindManager();
		if(bindManager == null) {
			Logging.logProgram(Level.ERROR, "No bind manager available.");
			return;
		}
		
		Map<String, Exception> errors = new HashMap<>();
		List<String> vars = varOption.values(optionSet);
		for(String v : vars) {
			Binding<?> b = bindManager.findBinding(v);
			if(b != null) {
				try {
					Logging.logProgram(Level.INFO, " %s = \"%s\" (%s)", b.getName(), String.valueOf(b.getValue()), b.getType().getSimpleName());
				} catch(Exception e) {
					errors.put(v, e);
				}
			} else {
				Logging.logProgram(Level.ERROR, " \"%s\" is not a valid bound property name.", v);
			}
		}
		
		if(errors.size() > 0) {
			for(Entry<String, Exception> e : errors.entrySet()) {
				Logging.logProgram(Level.ERROR, " Error getting value of property \"%s\": %s", e.getKey(), e.getValue().getMessage());
			}
		}
	}

	@Override
	public String getName() {
		return "read";
	}

	@Override
	public OptionParser getOptionParser() {
		return parser;
	}
}
