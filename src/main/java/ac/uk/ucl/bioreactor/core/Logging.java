package ac.uk.ucl.bioreactor.core;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logging {

	public static enum Level {
		USER, INFO, WARN, DEBUG, ERROR, FATAL;
		private final String prefix;
		Level() {
			prefix = "[" + getRawTag() + "]";
		}
		public String getTag() {
			return prefix;
		}
		
		public String getRawTag() {
			String name = name();
			return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
		}
	}
	
	private static void _log(String tag, String format, Object... args) {
		String userMessage = String.format(format, args);
		System.out.printf("%s: %s", tag, userMessage);
	}
	
	public static void logProgram(String programName, String format, Object... args) {
		log("[" + programName + "]", format, args);
	}
	
	public static void logProgram(String programName, Level level, String format, Object... args) {
		log("[" + programName + ":" + level.getRawTag() + "]", format, args);
	}
	
	public static void log(String customTag, String format, Object... args) {
		_log(customTag, format, args);
	}
	
	public static void log(Level level, String format, Object... args) {
		_log(level.getTag(), format, args);
	}
	
	public static void fatalError(String format, Object... args) {
		_log(Level.FATAL.getTag(), format, args);
		System.exit(1);
	}
	
	public static void fatalError(String msg, Throwable t) {
		_log(Level.FATAL.getTag(), "%s", msg);
		if(t != null) {
			fatalError(t);
		} else {
			System.exit(1);
		}
	}
	
	public static void fatalError(Throwable t) {
		logThrowable(Level.FATAL, t);
		System.exit(1);
	}
	
	public static void logThrowable(Level level, Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		
		String tag = level.getTag();
		for(String line : sw.toString().split(System.lineSeparator())) {
			_log(tag, "%s\n", line);
		}
	}
}
