package ac.uk.ucl.bioreactor.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ac.uk.ucl.bioreactor.core.binding.BindManager;
import ac.uk.ucl.bioreactor.core.subsystems.SubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.descriptors.StirringSubsystemDescriptor;
import ac.uk.ucl.bioreactor.core.subsystems.descriptors.TemperatureSubsystemDescriptor;
import ac.uk.ucl.bioreactor.ui.UIController;

public class Context {
	
	private static final Object contextLock = new Object();
	private static Context activeContext;
	
	public static Context getActiveContext() {
		synchronized (contextLock) {
			if(activeContext == null) {
				throw new IllegalStateException();
			}
			return activeContext;
		}
	}
	
	public static void setActiveContext(Context activeContext) {
		synchronized (contextLock) {
			Context.activeContext = activeContext;
		}
	}
	
	private final ExecutorService executorService;
	private UIController uiController;
	private Reactor reactor;
	private CommandProcessor commandProcessor;
	private BindManager bindManager;
	
	public Context() {
		executorService = Executors.newCachedThreadPool();
		bindManager = new BindManager();
		uiController = new UIController(this);
		commandProcessor = new CommandProcessor(this);
		
		List<SubsystemDescriptor> supportedDescriptors = new ArrayList<>();
//		supportedDescriptors.add(new PHSubsystemDescriptor());
		supportedDescriptors.add(new TemperatureSubsystemDescriptor());
		supportedDescriptors.add(new StirringSubsystemDescriptor());
		reactor = new BasicBioReactor(supportedDescriptors);
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public UIController getUiController() {
		return uiController;
	}

	public Reactor getReactor() {
		return reactor;
	}

	public CommandProcessor getCommandProcessor() {
		return commandProcessor;
	}
	
	public BindManager getBindManager() {
		return bindManager;
	}
}
