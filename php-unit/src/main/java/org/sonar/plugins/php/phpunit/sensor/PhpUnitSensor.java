package org.sonar.plugins.php.phpunit.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.executor.PhpPluginExecutionException;
import org.sonar.plugins.php.phpunit.configuration.PhpUnitConfiguration;

import com.thoughtworks.xstream.XStreamException;

/**
 * The Class PhpUnitSensor is used by the plugin to collect metrics concerning punit framework. This class doesn't launch the tests,
 * it only reads the results contains in the files found under the report directory set as a plugin property and which names begin
 * with "punit" and end with ".xml".
 */
public class PhpUnitSensor implements Sensor {

	/** The config. */
	private PhpUnitConfiguration config;

	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(PhpUnitSensor.class);

	/**
	 * Analyse.
	 * 
	 * @param project the project
	 * @param context the context
	 * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
	 */
	public void analyse(Project project, SensorContext context) {
		try {
			if (!getConfig(project).isAnalyseOnly()) {
				PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
				executor.execute();
			}
			new PhpUnitResultParser(project, context).parse(config.getReportFile());
			if (config.shouldRunCoverage()) {
				new PhpUnitCoverageResultParser(project, context).parse(config.getCoverageReportFile());
			}
		} catch (XStreamException e) {
			LOG.error("Report file is invalid, plugin will stop.", e);
			throw new SonarException(e);
		} catch (PhpPluginExecutionException e) {
			LOG.error("Error occured while launching PhpUnit", e);
			throw new SonarException(e);
		}
	}

	/**
	 * Determines whether or not this sensor will be executed on the given project.
	 * 
	 * @param project The project to be analyzed
	 * @return boolean <code>true</code> if project's language is php a,d the project configuration says so, <code>false</code> in any
	 *         other case.
	 * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api .resources.Project)
	 */
	public boolean shouldExecuteOnProject(Project project) {
		return getConfig(project).isShouldRun() && Php.INSTANCE.equals(project.getLanguage());
	}

	/**
	 * Gets the config.
	 * 
	 * @param project the project
	 * @return the config
	 */
	private PhpUnitConfiguration getConfig(Project project) {
		if (config == null) {
			config = new PhpUnitConfiguration(project);
		}
		return config;
	}

	/**
	 * To string.
	 * 
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
