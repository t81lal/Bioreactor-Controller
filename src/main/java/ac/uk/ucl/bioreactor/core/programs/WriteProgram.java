package ac.uk.ucl.bioreactor.core.programs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.Program;
import ac.uk.ucl.bioreactor.core.binding.BindManager;
import ac.uk.ucl.bioreactor.core.binding.Binding;
import ac.uk.ucl.bioreactor.core.binding.CastHelper;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class WriteProgram implements Program {

	private final OptionParser parser;
	private final NonOptionArgumentSpec<String> argsOpt;
	
	public WriteProgram() {
		parser = new OptionParser();
		argsOpt = parser.nonOptions("<bind name> <value>").ofType(String.class);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void execute(Context context, OptionSet optionSet) throws Exception {
		BindManager bindManager = context.getBindManager();
		if(bindManager == null) {
			Logging.logProgram(Level.ERROR, "No bind manager available.");
			return;
		}

		Map<String, Exception> errors = new HashMap<>();
		List<String> args = argsOpt.values(optionSet);
		if((args.size() % 2) == 0) {
			for(int i=0; i < args.size(); i+=2) {
				String varName = args.get(i);
				String valueStr = args.get(i+1);
				
				Binding b = bindManager.findBinding(varName);
				if(b != null) {
					try {
						b.setValue(CastHelper.parseValue(b.getType(), valueStr));
						Logging.logProgram(Level.INFO, " Set %s to \"%s\" (%s)", b.getName(), valueStr, b.getType().getSimpleName());
					} catch(Exception e) {
						errors.put(varName, e);
					}
				} else {
					Logging.logProgram(Level.ERROR, " \"%s\" is not a valid bound property name.", varName);
				}
			}
		} else {
			Logging.logProgram(Level.ERROR, "Unequal number of args, need key value pairs (got %d)", args.size());
		}
		
		if(errors.size() > 0) {
			for(Entry<String, Exception> e : errors.entrySet()) {
				Logging.logProgram(Level.ERROR, " Error setting value of property \"%s\": %s", e.getKey(), e.getValue().toString());
			}
		}
	}

	@Override
	public String getName() {
		return "write";
	}

	@Override
	public OptionParser getOptionParser() {
		return parser;
	}
}
