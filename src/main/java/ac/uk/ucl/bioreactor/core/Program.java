package ac.uk.ucl.bioreactor.core;

import java.util.List;

import ac.uk.ucl.bioreactor.util.Logging;
import ac.uk.ucl.bioreactor.util.Logging.Level;

public interface Program {
	
	boolean execute(Context context, List<String> args) throws Exception;
	
	String getUsage();
	
	String getName();

	default boolean errorNArgs(int req, int got) {
		if(got != req) {
			Logging.logProgram(getName(), Level.ERROR, "Not enough args, got %d, needed %d\n", got, req);
			return true;
		} else {
			return false;
		}
	}
}