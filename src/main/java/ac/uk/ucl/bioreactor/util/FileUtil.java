package ac.uk.ucl.bioreactor.util;

import java.io.File;

public class FileUtil {

	public static final File outputDir = new File("out/");
	
	static {
		if(!outputDir.exists()) {
			outputDir.mkdir();
		}
	}
}
