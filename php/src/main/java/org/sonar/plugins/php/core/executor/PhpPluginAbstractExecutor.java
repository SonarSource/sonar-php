package org.sonar.plugins.php.core.executor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract php plugin executor. This class handles common executor needs such as running the process,
 * reading its common and error output streams and logging. 
 * In nominal case implementing executor should just construct the desire command line.
 */
public abstract class PhpPluginAbstractExecutor {

	/**
	 * The Class AsyncPipe.
	 */
	class AsyncPipe extends Thread {

		/** The input stream. */
		private InputStream istrm;

		/** The output stream. */
		private OutputStream ostrm;

		/**
		 * Instantiates a new async pipe.
		 * 
		 * @param input an InputStream
		 * @param output an OutputStream
		 */
		public AsyncPipe(InputStream input, OutputStream output) {
			istrm = input;
			ostrm = output;
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			try {
				final byte[] buffer = new byte[1024];
				// Reads the process input stream and writes it to the output stream
				for (int length = 0; (length = istrm.read(buffer)) != -1;) {
					synchronized (ostrm) {
						ostrm.write(buffer, 0, length);
					}
				}
			} catch (Exception e) {
				LOG.error("Can't execute the Async Pipe", e);
			}
		}
	}

	/** The logger*/
	private static final Logger LOG = LoggerFactory.getLogger(PhpPluginAbstractExecutor.class);

	/**
	 * Executes the external tool.
	 */
	public void execute() {
		try {
			// Gets the tool command line
			List<String> commandLine = getCommandLine();
			ProcessBuilder builder = new ProcessBuilder(commandLine);
			LOG.info("Execute" + getExecutedTool() + " with command '{}'", prettyPrint(commandLine));
			// Starts the process
			Process p = builder.start();
			// And handles it's normal and error stream in separated threads.
			new AsyncPipe(p.getInputStream(), System.out).start();
			new AsyncPipe(p.getErrorStream(), System.err).start();
			LOG.info(getExecutedTool() + " ended with returned code '{}'.", p.waitFor());
		} catch (Exception e) {
			LOG.error("Can't execute the external tool", e);
			throw new PhpPluginExecutionException(e);
		}
	}

	/**
	 * Returns a String where each list member is separated with a space
	 * @param commandLine the external tool command line argument
	 * @return String where each list member is separated with a space
	 */
	private String prettyPrint(List<String> commandLine) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> iter = commandLine.iterator(); iter.hasNext();) {
			String part = iter.next();
			sb.append(part);
			if (iter.hasNext()) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	/**
	 * Gets the command line.
	 * 
	 * @return the command line
	 */
	protected abstract List<String> getCommandLine();

	/**
	 * Gets the executed tool.
	 * 
	 * @return the executed tool
	 */
	protected abstract String getExecutedTool();
}
