package ac.uk.ucl.bioreactor.util;

public class SilentExitException extends Exception {
	private static final long serialVersionUID = 1L;

	public SilentExitException(String message) {
		super(message);
	}
}