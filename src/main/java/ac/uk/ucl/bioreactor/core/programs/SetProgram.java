package ac.uk.ucl.bioreactor.core.programs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.Program;
import ac.uk.ucl.bioreactor.core.Reactor;
import ac.uk.ucl.bioreactor.core.binding.Binding;
import ac.uk.ucl.bioreactor.core.binding.BindingFactory;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;

public class SetProgram implements Program {
	
	private static final String PROGRAM_NAME = "Set";
	
	private final Map<String, Binding> bindings;
	
	public SetProgram() {
		bindings = new HashMap<>();
		try {
			loadReactorBindings();
		} catch(Exception e) {
			Logging.fatalError(e);
		}
	}
	
	public void registerFieldBinding(String alias, Class<?> clazz, String fieldName) throws Exception {
		registerBinding(alias, clazz, null, fieldName, false);
	}
	
	public void registerSetterBinding(String alias, Class<?> valType, Class<?> clazz, String methodName) throws Exception {
		registerBinding(alias, clazz, valType, methodName, true);
	}
	
	private void registerBinding(String alias, Class<?> clazz, Class<?> type, String memberName, boolean setter) throws Exception {
		synchronized (bindings) {
			if(bindings.containsKey(alias)) {
				Logging.fatalError("Binding alias \"%s\" already exists", alias);
				return;
			}
			
			Binding b;
			if(setter) {
				b = BindingFactory.makeSetterBinding(alias, type, clazz, memberName);
			} else {
				b = BindingFactory.makeFieldBinding(alias, clazz, memberName);
				if(clazz != null && !b.getType().equals(type)) {
					Logging.fatalError("Bad field type? Expected=%s, actual=%s", type, b.getType());
					return;
				}
			}
			bindings.put(alias, b);
		}
	}
	
	private void loadReactorBindings() throws Exception {
		registerSetterBinding("temperature", float.class, Reactor.class, "setTargetTemperature");
		registerSetterBinding("pH", float.class, Reactor.class, "setTargetPH");
		registerSetterBinding("stirRate", int.class, Reactor.class, "setTargetStirringRPM");
	}
	
	@Override
	public boolean execute(Context context, List<String> args) throws Exception {
		if(args.size() < 1) {
			errorNArgs(1, 0);
			return false;
		}
		String firstArg = args.get(0);
		
		if(firstArg.equals("--list")) {
			listBindings();
		} else if(firstArg.equals("--bind")) {
			if(errorNArgs(3, args.size())) {
				return false;
			}
			//tryBind(args.get(1), args.get(2));
			return false;
		} else {
			if(errorNArgs(2, args.size())) {
				return false;
			}
			setVal(context.getReactor(), firstArg, args.get(1));
		}
		
		return true;
	}
	
	private void listBindings() {
		synchronized (bindings) {
			Logging.logProgram(PROGRAM_NAME, "Bound variables:\n");
			for(Entry<String, Binding> e : bindings.entrySet()) {
				Binding b = e.getValue();
				Logging.logProgram(PROGRAM_NAME + ":list", "%s - %s (%s)\n", b.getAlias(), b.toString(), b.getType());
			}
		}
	}
	
	/*private void tryBind(String var, String classField, boolean method) {
		synchronized (bindings) {
			Binding b = bindings.get(var);
			if(b == null) {
				if(method) {
					
				}
			} else {
				Logging.logProgram(PROGRAM_NAME, Level.ERROR, "Binding for alias \"%s\" already exists", var);
			}
		}
	}*/
	
	private void setVal(Reactor reactor, String var, String val) throws Exception {
		synchronized (bindings) {
			Binding b = bindings.get(var);
			if(b != null) {
				b.setValue(reactor, val);
			} else {
				Logging.logProgram(PROGRAM_NAME, Level.ERROR, "No binding for alias \"%s\"\n", var);
			}
		}
	}

	@Override
	public String getUsage() {
		return "(<var> <value> | --list | --bind <var> <class.field>)";
	}

	@Override
	public String getName() {
		return PROGRAM_NAME;
	}
}
