package ac.uk.ucl.bioreactor.core.programs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ac.uk.ucl.bioreactor.core.Logging;
import ac.uk.ucl.bioreactor.core.Logging.Level;
import ac.uk.ucl.bioreactor.core.Program;
import ac.uk.ucl.bioreactor.core.Reactor;

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
				b = Binding.makeSetterBinding(alias, type, clazz, memberName);
			} else {
				b = Binding.makeFieldBinding(alias, clazz, memberName);
				if(clazz != null && !b.type.equals(type)) {
					Logging.fatalError("Bad field type? Expected=%s, actual=%s", type, b.type);
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
	public boolean execute(Reactor reactor, List<String> args) throws Exception {
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
			setVal(reactor, firstArg, args.get(1));
		}
		
		return true;
	}
	
	private boolean errorNArgs(int req, int got) {
		if(got != req) {
			Logging.logProgram(PROGRAM_NAME, "Not enough args, got %d, needed %d\n", got, req);
			return true;
		} else {
			return false;
		}
	}
	
	private void listBindings() {
		synchronized (bindings) {
			Logging.logProgram(PROGRAM_NAME, "Bound variables:\n");
			for(Entry<String, Binding> e : bindings.entrySet()) {
				Binding b = e.getValue();
				Logging.logProgram(PROGRAM_NAME + ":list", "%s - %s (%s)\n", b.alias, b.toString(), b.type);
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
	
	private static class FieldBinding extends Binding {
		private final Field field;
		public FieldBinding(String alias, Class<?> type, Field field) {
			super(alias, type);
			this.field = field;
		}

		@Override
		protected void setValueImpl(Object instance, Object v) throws Exception {
			field.setAccessible(true);
			field.set(instance, v);
			field.setAccessible(false);
		}
		
		@Override
		public String toString() {
			return field.getDeclaringClass().getSimpleName() + "." + field.getName();
		}
	}
	
	private static class MethodBinding extends Binding {
		private final Method method;
		public MethodBinding(String alias, Class<?> type, Method method) {
			super(alias, type);
			this.method = method;
		}

		@Override
		protected void setValueImpl(Object instance, Object v) throws Exception {
			method.setAccessible(true);
			method.invoke(instance, v);
			method.setAccessible(false);
		}
		
		@Override
		public String toString() {
			return method.getDeclaringClass().getSimpleName() + "." + method.getName();
		}
	}
	private static abstract class Binding {
		static Binding makeFieldBinding(String alias, Class<?> clazz, String fieldName) throws Exception {
			Field f = clazz.getDeclaredField(fieldName);
			return new FieldBinding(alias, f.getType(), f);
		}
		
		static Binding makeSetterBinding(String alias, Class<?> type, Class<?> clazz, String methodName) throws Exception {
			Method m = clazz.getDeclaredMethod(methodName, type);
			return new MethodBinding(alias, type, m);
		}
		
		private final String alias;
		private final Class<?> type;
		
		public Binding(String alias, Class<?> type) {
			this.alias = alias;
			this.type = type;
		}
		
		protected abstract void setValueImpl(Object instance, Object v) throws Exception;
		
		public void setValue(Object instance, String v) throws Exception {
			Object val;
			try {
				if(type == Float.class || type == float.class) {
					val = Float.parseFloat(v);
				} else if(type == Integer.class || type == int.class) {
					val = Integer.parseInt(v);
				} else if(type == String.class) {
					val = v;
				} else if(type == Double.class || type == double.class) {
					val = Double.parseDouble(v);
				} else if(type == Long.class || type == long.class) {
					val = Long.parseLong(v);
				} else if(type == Boolean.class || type == boolean.class) {
					val = Boolean.parseBoolean(v);
				} else if(type == Character.class || type == char.class) {
					if(v.length() != 1) {
						throw new SilentExitException(String.format("\"%s\" is not a character", v));
					}
					val = v.charAt(0);
				} else if(type == Byte.class || type == byte.class) {
					val = Byte.valueOf(v);
				} else {
					throw new SilentExitException(String.format("Can't handle field of type %s", type));
				}
			} catch(NumberFormatException e) {
				Logging.logProgram(PROGRAM_NAME, Level.ERROR, "\"%s\" is not of type %s\n", v, type);
				return;
			} catch(SilentExitException e) {
				Logging.logProgram(PROGRAM_NAME, Level.ERROR, e.getMessage() +"\n");
				return;
			}
			
			setValueImpl(instance, val);
			
			Logging.logProgram(PROGRAM_NAME, Level.INFO, "Success, set \"%s\" to \"%s\"\n", alias, val);
		}
	}
	
	private static class SilentExitException extends Exception {
		private static final long serialVersionUID = 1L;
		public SilentExitException(String message) {
			super(message);
		}
	}
}
