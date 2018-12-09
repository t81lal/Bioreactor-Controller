package ac.uk.ucl.bioreactor.core.programs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fazecast.jSerialComm.SerialPort;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.Program;
import ac.uk.ucl.bioreactor.core.subsystems.Subsystem;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.serial.SerialSubsystem;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class SerialSelectProgram implements Program {
	
	private static class BoundPort {
		SubsystemDescriptor desc;
		SerialPort port;
		
		public BoundPort(SubsystemDescriptor desc, SerialPort port) {
			this.desc = desc;
			this.port = port;
		}
	}
	
	private class LaunchConfiguration {
		final List<BoundPort> boundPorts = new ArrayList<>();
		
		BoundPort findBoundPort(SubsystemDescriptor descriptor) {
			for(BoundPort p : boundPorts) {
				if(p.desc == descriptor) {
					return p;
				}
			}
			BoundPort bp = new BoundPort(descriptor, null);
			boundPorts.add(bp);
			return bp;
		}
		
		void removeBoundport(BoundPort bp) {
			boundPorts.remove(bp);
		}
		
		List<SubsystemDescriptor> findMissingDescriptors(Context context) {
			List<SubsystemDescriptor> descs = new ArrayList<>(context.getReactor().getSupportedSubSystemDescriptors());
			for(Subsystem s : context.getReactor().getActiveSubSystems()) {
				descs.remove(s.getDescriptor());
			}
			for(BoundPort bp : boundPorts) {
				descs.remove(bp.desc);
			}
			return descs;
		}
		
		void launch(Context context) {
			List<SubsystemDescriptor> missingDescs = findMissingDescriptors(context);
			List<Subsystem> initialised = new ArrayList<>();
			if(missingDescs.isEmpty()) {
				try {
					for(BoundPort bp : boundPorts) {
						SerialSubsystem ss = (SerialSubsystem) bp.desc.createSubsystem(context);
						ss.setPort(bp.port);
						ss.init();
						initialised.add(ss);
					}
					for(Subsystem s : initialised) {
						context.getReactor().install(s);
					}
					Logging.logProgram(Level.INFO, "Installed %d subsystems", initialised.size());
				} catch(Throwable t) {
					Logging.fatalError(t);
				}
			} else {
				Logging.logProgram(Level.ERROR, "Cannot configure without configuring subsystem ports for: %s", missingDescs);
			}
		}

		void display(Context context) {
			List<Subsystem> activeSubsystems = context.getReactor().getActiveSubSystems();
			Logging.logProgram(Level.INFO, "%d active subsystems:", activeSubsystems.size());
			for(Subsystem s : activeSubsystems) {
				SerialSubsystem sss = (SerialSubsystem) s;
				Logging.logProgram(Level.INFO, " %s subsystem bound on port \"%s\"", s.getDescriptor().getName(), sss.getPort().getSystemPortName());
			}
			Logging.logProgram(Level.INFO, "%d (uncommitted) bound ports:", boundPorts.size());
			for(BoundPort d : boundPorts) {
				Logging.logProgram(Level.INFO, " %s subsystem on port \"%s\"", d.desc.getName(), d.port.getSystemPortName());
			}
		}
	}
	
	private static final String SELECT_PROGRAM_NAME = "select";
	
	private final OptionParser parser;
	private final OptionSpec<Void> infoOption;
	private final OptionSpec<Void> listPortsOption;
	private final OptionSpec<Void> listSystemsOption;
	private final OptionSpec<Void> launchOption;
	private final OptionSpec<Void> displayOption;
	private final NonOptionArgumentSpec<String> bindOption;
	
	private final Map<Context, LaunchConfiguration> configs;
	
	public SerialSelectProgram() {
		parser = new OptionParser(true);
		infoOption = parser.acceptsAll(Arrays.asList("info", "i"), "Description of workflow for serial selection procedure").forHelp();
		listPortsOption = parser.acceptsAll(Arrays.asList("list-ports", "ports", "lp"), "Listing of available communication ports");
		listSystemsOption = parser.acceptsAll(Arrays.asList("list-subsystems", "subsystems", "ls"), "Listing of supported subsystems");
		launchOption = parser.accepts("launch", "Launch active configuration");
		displayOption = parser.acceptsAll(Arrays.asList("print-state", "state", "config", "d"), "Show current configuration");
		bindOption = parser.nonOptions("Bind subsystem to a given port").describedAs("<subsystem> <port/NULL>");
		
		configs = new ConcurrentHashMap<>();
	}
	
	private LaunchConfiguration getLaunchConfigurationFor(Context context) {
		if(configs.containsKey(context)) {
			return configs.get(context);
		} else {
			LaunchConfiguration config = new LaunchConfiguration();
			configs.put(context, config);
			return config;
		}
	}

	@Override
	public OptionParser getOptionParser() {
		return parser;
	}

	@Override
	public String getName() {
		return SELECT_PROGRAM_NAME;
	}
	
	@Override
	public void execute(Context context, OptionSet optionSet) throws Exception {
		if(optionSet.has(infoOption)) {
			displayWorkflow();
		}
		if(optionSet.has(listPortsOption)) {
			listPorts();
		}
		if(optionSet.has(listSystemsOption)) {
			listSubsystems(context);
		}
		if(optionSet.has(displayOption)) {
			displayState(context);
		}
		
		List<String> args = bindOption.values(optionSet);
		if(args.size() == 2) {
			attemptBind(context, args.get(0), args.get(1));
		} else if(args.size() > 2) {
			Logging.log(Level.ERROR, "Too many args to %s program", SELECT_PROGRAM_NAME);
		}
		
		if(optionSet.has(launchOption)) {
			attemptLaunch(context);
		}
	}
	
	private void displayWorkflow() {
		Logging.logProgram(Level.INFO, "Bind subsystems with serial enabled ports using the default arguments and commit/launch the configuration using the launch option.");
	}
	
	private void displayState(Context context) {
		getLaunchConfigurationFor(context).display(context);
	}
	
	private void listPorts() {
		SerialPort[] ports = SerialPort.getCommPorts();
		Logging.logProgram(Level.INFO, "%d available ports:", ports.length);
		for(int i=0; i < ports.length; i++) {
			Logging.logProgram(Level.INFO, " %d. %s", i+1, ports[i].getSystemPortName());
		}
	}
	
	private void listSubsystems(Context context) {
		Logging.logProgram(Level.INFO, "Available subsystems:");
		List<SubsystemDescriptor> descs = context.getReactor().getSupportedSubSystemDescriptors();
		for(int i=0; i < descs.size(); i++) {
			Logging.logProgram(Level.INFO, " %d. %s", i+1, descs.get(i).getName());
		}
	}
	
	private void attemptLaunch(Context context) {
		getLaunchConfigurationFor(context).launch(context);
	}
	
	private void attemptBind(Context context, String systemName, String portName) {
		SubsystemDescriptor desc = findSubsystem(context, systemName);
		if(desc == null) {
			Logging.logProgram(Level.ERROR, "No subsystem for name \"%s\"", systemName);
			return;
		}
		
		SerialPort port = null;
		if(!portName.equals("NULL")) {
			port = findPort(portName);
			if(port == null) {
				Logging.logProgram(Level.ERROR, "No port for name \"%s\"", portName);
				return;
			}
		}
		
		LaunchConfiguration config = getLaunchConfigurationFor(context);
		BoundPort bp = config.findBoundPort(desc);
		
		if(port == null) {
			config.removeBoundport(bp);
			if(bp.port != null) {
				Logging.logProgram(Level.INFO, "unbound %s subsystem from port: \"%s\"", systemName, bp.port.getSystemPortName());
			} else {
				Logging.logProgram(Level.INFO, "%s subsystem was already unbound", systemName);
			}
		} else {
			bp.port = port;
			Logging.logProgram(Level.INFO, "Bound %s subsystem to port: \"%s\"", systemName, portName);
		}
	}
	
	private SubsystemDescriptor findSubsystem(Context context, String name) {
		for(SubsystemDescriptor desc : context.getReactor().getSupportedSubSystemDescriptors()) {
			if(desc.getName().equals(name)) {
				return desc;
			}
		}
		return null;
	}
	
	private SerialPort findPort(String systemPortName) {
		for(SerialPort sp : SerialPort.getCommPorts()) {
			if(sp.getSystemPortName().equals(systemPortName)) {
				return sp;
			}
		}
		return null;
	}
}
