package ac.uk.ucl.bioreactor.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import ac.uk.ucl.bioreactor.core.CommandProcessor;
import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.Program;

public class Logging {

	private static FileOutputStream logFos;
	
	static {
		try {
			logFos = new FileOutputStream(new File(FileUtil.outputDir, "log" + String.valueOf(System.currentTimeMillis()) + ".txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("NO LOG FILE");
			logFos = null;
		}
	}
	
	private static boolean debuggingActive = false;
	
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
	
	private Logging() {
		throw new UnsupportedOperationException();
	}
	
	private static void _log(String tag, String format, Object... args) {
		String userMessage = String.format(format, args);
		String finalMsg = String.format("%s: %s\n", tag, userMessage);
		System.out.print(finalMsg);
		
		if(logFos != null) {
			Context.getActiveContext().getExecutorService().execute(() -> {
				synchronized (logFos) {
					try {
						logFos.write(finalMsg.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	public static void logProgram(String format, Object... args) {
		Context context = Context.getActiveContext();
		CommandProcessor commandProcessor = context.getCommandProcessor();
		Program program = commandProcessor.getCurrentProgram();
		if(program != null) {
			logProgram(program.getName(), format, args);
		} else {
			throw new IllegalStateException();
		}
	}
	
	public static void logProgram(Level level, String format, Object... args) {
		Context context = Context.getActiveContext();
		CommandProcessor commandProcessor = context.getCommandProcessor();
		Program program = commandProcessor.getCurrentProgram();
		if(program != null) {
			logProgram(program.getName(), level, format, args);
		} else {
			throw new IllegalStateException();
		}
	}
	
	public static void logProgram(String programName, String format, Object... args) {
		log("[" + programName + "]", format, args);
	}
	
	public static void logProgram(String programName, Level level, String format, Object... args) {
		if(acceptLevel(level)) {
			log("[" + programName + ":" + level.getRawTag() + "]", format, args);
		}
	}
	
	public static void log(String customTag, String format, Object... args) {
		_log(customTag, format, args);
	}
	
	public static void log(Level level, String format, Object... args) {
		if(acceptLevel(level)) {
			_log(level.getTag(), format, args);
		}
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
		if(!acceptLevel(level))
			return;
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		
		String tag = level.getTag();
		for(String line : sw.toString().split(System.lineSeparator())) {
			_log(tag, "%s", line);
		}
	}
	
	private static boolean acceptLevel(Level level) {
		return debuggingActive || level != Level.DEBUG;
	}
}
