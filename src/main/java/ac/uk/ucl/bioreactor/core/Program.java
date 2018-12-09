package ac.uk.ucl.bioreactor.core;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public interface Program {
	
	void execute(Context context, OptionSet optionSet) throws Exception;
	
	String getName();
	
	OptionParser getOptionParser();
}