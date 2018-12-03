package ac.uk.ucl.bioreactor.core;

import java.util.List;

public interface Program {
	
	boolean execute(Reactor reactor, List<String> args) throws Exception;
	
	String getUsage();
}