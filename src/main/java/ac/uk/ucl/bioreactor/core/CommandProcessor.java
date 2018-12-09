package ac.uk.ucl.bioreactor.core;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ac.uk.ucl.bioreactor.core.programs.ListBindingsProgram;
import ac.uk.ucl.bioreactor.core.programs.ReadProgram;
import ac.uk.ucl.bioreactor.core.programs.SerialSelectProgram;
import ac.uk.ucl.bioreactor.core.programs.WriteProgram;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;
import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CommandProcessor {

	private final Context context;
	private final Map<String, Program> programs;
	private Program currentProgram;
	
	public CommandProcessor(Context context) {
		this.context = context;
		this.programs = new HashMap<>();
		addHelpProgram();
		addProgram(new SerialSelectProgram());
		addProgram(new ListBindingsProgram());
		addProgram(new ReadProgram());
		addProgram(new WriteProgram());
		
		currentProgram = null;
	}
	
	public Program getCurrentProgram() {
		return currentProgram;
	}
	
	private void addHelpProgram() {
		addProgram(new Program() {
			OptionParser parser = new OptionParser();
			BuiltinHelpFormatter formatter = new BuiltinHelpFormatter(71, 3);
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void execute(Context context, OptionSet optionSet) throws Exception {
				Logging.logProgram("Help", "Loaded programs:");
				for(Entry<String, Program> e : programs.entrySet()) {
					Program p = e.getValue();
					if(p == this) {
						continue;
					}
					Logging.log(Level.INFO, "===================== %s =====================", p.getName());
					StringWriter sw = new StringWriter();
					OptionParser parser = p.getOptionParser();
					sw.write(formatter.format((Map)parser.recognizedOptions()));
					sw.flush();
					
					for(String line : sw.toString().split(System.lineSeparator())) {
						Logging.log(Level.INFO, line);
					}
				}
			}
			@Override
			public OptionParser getOptionParser() {
				return parser;
			}
			
			@Override
			public String getName() {
				return "help";
			}
		});
	}
	
	public void addProgram(Program p) {
		if(p == null) {
			throw new NullPointerException();
		}
		String name = p.getName();
		for(char c : name.toCharArray()) {
			if(!Character.isAlphabetic(c)) {
				throw new UnsupportedOperationException(String.format("Program name \"%s\" must be alphabetic", name));
			}
		}
		synchronized (programs) {
			if(programs.containsKey(name)) {
				throw new UnsupportedOperationException(String.format("Program \"%s\" already exists.", name));
			} else {
				Logging.log(Level.DEBUG, "Installing program: \"%s\" (type:%s)", name, p.getClass().getName());
				programs.put(name, p);
			}
		}
	}
	
	public void process(String input) {
		context.getExecutorService().execute(() -> {
			Logging.log(Level.DEBUG, "Processing input: \"%s\"", input);
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
				Logging.log(Level.ERROR, "Unknown program: \"%s\"", programName);
				return;
			}

			Program prog = programs.get(programName);
			
			OptionParser optionParser = prog.getOptionParser();
			OptionSet optionSet;
			
			if(inputParts.length == 2) {
				optionSet = optionParser.parse(inputParts[1].split(" "));
			} else {
				optionSet = optionParser.parse();
			}
			
			currentProgram = prog;
			
			context.getExecutorService().execute(() -> {
				try {
					prog.execute(context, optionSet);
				} catch (Exception e) {
					Logging.log(Level.ERROR, "Error executing program: \"%s\":", programName);
					Logging.logThrowable(Level.ERROR, e);
				}
			});
		}
	}
}
