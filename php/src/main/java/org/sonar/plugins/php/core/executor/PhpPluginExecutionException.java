package org.sonar.plugins.php.core.executor;


/**
 * The Class PhpPluginExecutionException.
 */
public class PhpPluginExecutionException extends RuntimeException {

	/**
	 * Instantiates a new php plugin execution exception.
	 */
	public PhpPluginExecutionException() {
		super();
	}

	/**
	 * Instantiates a new php plugin execution exception.
	 * 
	 * @param message the message
	 */
	public PhpPluginExecutionException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new php plugin execution exception.
	 * 
	 * @param message the message
	 * @param cause the cause
	 */
	public PhpPluginExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new php plugin execution exception.
	 * 
	 * @param cause the cause
	 */
	public PhpPluginExecutionException(Throwable cause) {
		super(cause);
	}

}
