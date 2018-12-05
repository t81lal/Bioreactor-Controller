package ac.uk.ucl.bioreactor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ac.uk.ucl.bioreactor.core.programs.SerialSelectProgram;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;

public class CommandProcessor {

	private final Context context;
	private final Map<String, Program> programs;
	private final List<ExecutedProgram> executedPrograms;
	private Program currentProgram;
	
	public CommandProcessor(Context context) {
		this.context = context;
		this.programs = new HashMap<>();
		addHelpProgram();
		//addProgram("set", new SetProgram());
		addProgram("bselect", new SerialSelectProgram());
		executedPrograms = new ArrayList<>();
		
		ExecutedProgram executedProgram = new ExecutedProgram();
		executedProgram.programName = "SYSTEM";
		executedProgram.returnState = true;
		executedProgram.active = true;
		
		executedPrograms.add(executedProgram);
		
		currentProgram = null;
	}
	
	public Program getCurrentProgram() {
		return currentProgram;
	}
	
	private void addHelpProgram() {
		addProgram("help", new Program() {
			@Override
			public boolean execute(Context context, List<String> _args) {
				Logging.logProgram("Help", "Loaded programs:\n");
				for(Entry<String, Program> e : programs.entrySet()) {
					Program p = e.getValue();
					if(p == this) {
						continue;
					}
					String usage = p.getUsage();
					if(usage == null) {
						usage = "[]";
					}
					Logging.logProgram("Help", "Usage: %s %s\n", e.getKey(), usage);
				}
				return true;
			}

			@Override
			public String getUsage() {
				return null;
			}
			
			public String getName() {
				return "help";
			}
		});
	}
	
	public void addProgram(String name, Program p) {
		if(name == null || p == null) {
			throw new NullPointerException();
		}
		for(char c : name.toCharArray()) {
			if(!Character.isAlphabetic(c)) {
				throw new UnsupportedOperationException(String.format("Program name \"%s\" must be alphabetic", name));
			}
		}
		synchronized (programs) {
			if(programs.containsKey(name)) {
				throw new UnsupportedOperationException(String.format("Program \"%s\" already exists.", name));
			} else {
				programs.put(name, p);
			}
		}
	}
	
	public void process(String input) {
		context.getExecutorService().execute(() -> {
			processImpl(input);
		});
	}
	
	private void processImpl(String input) {
		synchronized (programs) {
			String[] inputParts = input.split(" ", 2);
			if(inputParts.length == 0) {
				throw new IllegalStateException();
			}
			String programName = inputParts[0];
			
			if(!programs.containsKey(programName)) {
				Logging.log(Level.ERROR, "Unknown program: \"%s\"\n", programName);
				return;
			}

			List<String> args = new ArrayList<>();
			if(inputParts.length == 2) {
				String argsPart = inputParts[1];
				
				StringBuilder sb = new StringBuilder();
				boolean inString = false;
				char[] argChars = argsPart.toCharArray();
				for(int i=0; i < argChars.length; i++) {
					char c = argChars[i];
					if(c == '"') {
						inString = !inString;
						continue;
					}
					if(!inString && Character.isWhitespace(c)) {
						if(sb.length() > 0) {
							args.add(sb.toString());
						}
						sb.setLength(0);
					} else {
						sb.append(c);
					}
				}
				if(sb.length() > 0) {
					args.add(sb.toString());
				}
			}
			
			Program prog = programs.get(programName);
			currentProgram = prog;
			
			context.getExecutorService().execute(() -> {
				try {
					if (!prog.execute(context, args)) {
						Logging.log(Level.WARN, "Program \"%s\" returned false (rejected input)\n", programName);
					}
				} catch (Exception e) {
					Logging.log(Level.ERROR, "Error executing program: \"%s\":\n", programName);
					Logging.logThrowable(Level.ERROR, e);
				}
			});
		}
	}
	
	public static class ExecutedProgram {
		String programName;
		boolean returnState;
		boolean active;
		Exception exception;
	}
}
