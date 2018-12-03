package ac.uk.ucl.bioreactor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import ac.uk.ucl.bioreactor.core.Logging.Level;
import ac.uk.ucl.bioreactor.core.programs.SetProgram;

public class CommandProcessor {
	
	private final ExecutorService executorService;
	private final Map<String, Program> programs;
	private final Reactor reactor;
	private final ExecutingProgram executingProgram;
	
	public CommandProcessor(ExecutorService executorService, Reactor reactor) {
		this.executorService = executorService;
		this.programs = new HashMap<>();
		this.reactor = reactor;
		addHelpProgram();
		addProgram("set", new SetProgram());
		
		executingProgram = new ExecutingProgram();
		executingProgram.programName = "SYSTEM";
		executingProgram.returnState = true;
		executingProgram.active = true;
	}
	
	private void addHelpProgram() {
		addProgram("help", new Program() {
			@Override
			public boolean execute(Reactor reactor, List<String> _args) {
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
					Logging.logProgram("Help", "Usage: %s <%s>\n", e.getKey(), usage);
				}
				return true;
			}

			@Override
			public String getUsage() {
				return null;
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
		executorService.execute(() -> {
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
			
			executorService.execute(() -> {
				try {
					if (!prog.execute(reactor, args)) {
						Logging.log(Level.WARN, "Program \"%s\" returned false (rejected input)\n", programName);
					}
				} catch (Exception e) {
					Logging.log(Level.ERROR, "Error executing program: \"%s\":\n", programName);
					Logging.logThrowable(Level.ERROR, e);
				}
			});
		}
	}
	
	static class ExecutingProgram {
		String programName;
		boolean returnState;
		boolean active;
		Exception exception;
		ExecutingProgram next;
	}
}
