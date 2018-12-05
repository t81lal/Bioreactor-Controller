package ac.uk.ucl.bioreactor.core.programs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fazecast.jSerialComm.SerialPort;

import ac.uk.ucl.bioreactor.core.Context;
import ac.uk.ucl.bioreactor.core.Program;
import ac.uk.ucl.bioreactor.core.subsystems.Subsystem;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.descriptors.PHSubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.descriptors.StirringSubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.descriptors.TemperatureSubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.serial.SerialPHSubsystem;
import ac.uk.ucl.bioreactor.core.subsystems.serial.SerialStirringSubsystem;
import ac.uk.ucl.bioreactor.core.subsystems.serial.SerialTemperatureSubsystem;
import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;

public class SerialSelectProgram implements Program {
	
	private static final String PROGRAM_NAME = "bselect";
	
	private final Map<Context, List<BoundPort>> boundPorts = new ConcurrentHashMap<>();
	
	private List<BoundPort> getBoundPortsFor(Context context) {
		if(boundPorts.containsKey(context)) {
			return boundPorts.get(context);
		} else {
			List<BoundPort> list = new ArrayList<>();
			boundPorts.put(context, list);
			return list;
		}
	}
	
	private BoundPort getBoundSystemPort(Context context, SubsystemDescriptor desc) {
		List<BoundPort> bound = getBoundPortsFor(context);
		for(BoundPort p : bound) {
			if(p.desc == desc) {
				return p;
			}
		}
		return null;
	}
	
	@Override
	public boolean execute(Context context, List<String> args) throws Exception {
		if(args.size() < 1) {
			errorNArgs(1, 0);
			return false;
		}
		
		String firstArg = args.get(0);
		if(firstArg.equals("--help")) {
			if(errorNArgs(1, args.size())) {
				return false;
			}
			printHelp();
			return true;
		} else if(firstArg.equals("--list-ports")) {
			if(errorNArgs(1, args.size())) {
				return false;
			}
			listPorts();
			return true;
		} else if(firstArg.equals("--list-subsystems")) {
			if(errorNArgs(1, args.size())) {
				return false;
			}
			listSubsystems(context);
			return true;
		} else if(firstArg.equals("--set")) {
			if(errorNArgs(3, args.size())) {
				return false;
			}
			setSubsystemPort(context, args.get(1), args.get(2));
			return true;
		} else if(firstArg.equals("--configure")) {
			if(errorNArgs(1, args.size())) {
				return false;
			}
			configure(context);
			return true;
		} else if(firstArg.equals("--print-state")) {
			if(errorNArgs(1, args.size())) {
				return false;
			}
			return true;
		} else {
			Logging.logProgram(Level.ERROR, "Unknown comnmand: \"%s\"\n", firstArg);
			return false;
		}
	}
	
	private void printHelp() {
		Logging.logProgram(Level.INFO, "Set a port for each subsystem and finalise using the --configure command\n");
	}
	
	private void listPorts() {
		SerialPort[] ports = SerialPort.getCommPorts();
		Logging.logProgram(Level.INFO, "%d available ports:\n", ports.length);
		for(int i=0; i < ports.length; i++) {
			Logging.logProgram(Level.INFO, " %d. %s\n", i+1, ports[i]);
		}
	}
	
	private void listSubsystems(Context context) {
		Logging.logProgram(Level.INFO, "Available subsystems:\n");
		List<SubsystemDescriptor> descs = context.getReactor().getSupportedSubSystemDescriptors();
		for(int i=0; i < descs.size(); i++) {
			Logging.logProgram(Level.INFO, " %d. %s\n", i+1, descs.get(i).getName());
		}
	}
	
	private void setSubsystemPort(Context context, String subsystemName, String portName) {
		SubsystemDescriptor targetSys = null;
		for(SubsystemDescriptor ss : context.getReactor().getSupportedSubSystemDescriptors()) {
			if(ss.getName().equals(subsystemName)) {
				if(targetSys != null) {
					Logging.logProgram(Level.ERROR, "Multiple subsystems for name \"%s\"\n", subsystemName);
					return;
				} else {
					targetSys = ss;
				}
			}
		}
		
		if(targetSys == null) {
			Logging.logProgram(Level.ERROR, "No subsystem for name \"%s\"\n", subsystemName);
			return;
		}
		
//		SerialPort port = null;
//		for(SerialPort sp : SerialPort.getCommPorts()) {
//			if(sp.getSystemPortName().equals(portName)) {
//				port = sp;
//			}
//		}
		
		// TODO: remove
		SerialPort port = SerialPort.getCommPort("COM1");
		
		if(port == null) {
			Logging.logProgram(Level.ERROR, "No port for name \"%s\"\n", portName);
			return;
		}
		
		BoundPort bp = getBoundSystemPort(context, targetSys);
		if(bp != null) {
			getBoundPortsFor(context).remove(bp);
		}
		getBoundPortsFor(context).add(new BoundPort(targetSys, port));
	}
	
	private List<SubsystemDescriptor> getMissingDescriptors(Context context, List<BoundPort> bps) {
		List<SubsystemDescriptor> descs = new ArrayList<>(context.getReactor().getSupportedSubSystemDescriptors());
		for(Subsystem s : context.getReactor().getActiveSubSystems()) {
			descs.remove(s.getDescriptor());
		}
		for(BoundPort bp : bps) {
			descs.remove(bp.desc);
		}
		return descs;
	}
	
	private void configure(Context context) {
		List<BoundPort> bps = getBoundPortsFor(context);
		List<SubsystemDescriptor> missingDescs = getMissingDescriptors(context, bps);
		if(missingDescs.isEmpty()) {
			List<Subsystem> initialised = new ArrayList<>();
			try {
				for(BoundPort bp : bps) {
					Subsystem ss = createSubSystem(bp.desc, bp.port);
					ss.init();
					initialised.add(ss);
				}
				
				for(Subsystem s : initialised) {
					context.getReactor().install(s);
				}
				Logging.logProgram(Level.INFO, "Installed %d subsystems\n", initialised.size());
			} catch(Throwable t) {
				Logging.fatalError(t);
			}
		} else {
			Logging.logProgram(Level.ERROR, "Cannot configure without configuring subsystem ports for: %s\n", missingDescs);
		}
	}
	
	private Subsystem createSubSystem(SubsystemDescriptor desc, SerialPort port) {
		if(desc instanceof PHSubsystemDescriptor) {
			return new SerialPHSubsystem(desc, port);
		} else if(desc instanceof TemperatureSubsystemDescriptor) {
			return new SerialTemperatureSubsystem(desc, port);
		} else if(desc instanceof StirringSubsystemDescriptor) {
			return new SerialStirringSubsystem(desc, port);
		} else {
			throw new UnsupportedOperationException("Unknown type: " + desc.getClass());
		}
	}
	
	private void shutdownAll(List<Subsystem> systems) {
		for(Subsystem s : systems) {
			try {
				s.stop();
			} catch(Throwable t) {
				Logging.logProgram(Level.WARN, "Error shutting down %s subsystem: %s\n", t.getMessage());
			}
		}
	}

	@Override
	public String getUsage() {
		return "(--list-ports | --list-subsystems | --help | --set <system> <port> | --configure)";
	}

	@Override
	public String getName() {
		return PROGRAM_NAME;
	}
	
	private static class BoundPort {
		public final SubsystemDescriptor desc;
		public final SerialPort port;
		
		public BoundPort(SubsystemDescriptor desc, SerialPort port) {
			this.desc = desc;
			this.port = port;
		}
	}
}
