package ac.uk.ucl.bioreactor.core.binding;

import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.SilentExitException;
import ac.uk.ucl.bioreactor.util.Logging.Level;

public abstract class Binding {

	private final String alias;
	private final Class<?> type;

	public Binding(String alias, Class<?> type) {
		this.alias = alias;
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public Class<?> getType() {
		return type;
	}

	protected abstract void setValueImpl(Object instance, Object v) throws Exception;

	public void setValue(Object instance, String v) throws Exception {
		Object val;
		try {
			if (type == Float.class || type == float.class) {
				val = Float.parseFloat(v);
			} else if (type == Integer.class || type == int.class) {
				val = Integer.parseInt(v);
			} else if (type == String.class) {
				val = v;
			} else if (type == Double.class || type == double.class) {
				val = Double.parseDouble(v);
			} else if (type == Long.class || type == long.class) {
				val = Long.parseLong(v);
			} else if (type == Boolean.class || type == boolean.class) {
				val = Boolean.parseBoolean(v);
			} else if (type == Character.class || type == char.class) {
				if (v.length() != 1) {
					throw new SilentExitException(String.format("\"%s\" is not a character", v));
				}
				val = v.charAt(0);
			} else if (type == Byte.class || type == byte.class) {
				val = Byte.valueOf(v);
			} else {
				throw new SilentExitException(String.format("Can't handle field of type %s", type));
			}
		} catch (NumberFormatException e) {
			Logging.logProgram("\"%s\" is not of type %s\n", v, type);
			return;
		} catch (SilentExitException e) {
			Logging.logProgram(Level.ERROR, e.getMessage() + "\n");
			return;
		}

		setValueImpl(instance, val);

		Logging.logProgram(Level.INFO, "Success, set \"%s\" to \"%s\"\n", alias, val);
	}
}