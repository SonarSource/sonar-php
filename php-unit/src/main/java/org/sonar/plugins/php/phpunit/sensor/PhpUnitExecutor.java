package org.sonar.plugins.php.phpunit.sensor;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor;
import org.sonar.plugins.php.phpunit.configuration.PhpUnitConfiguration;

/**
 * The Class PhpUnitExecutor.
 */
public class PhpUnitExecutor extends PhpPluginAbstractExecutor {

	/** The config. */
	private final PhpUnitConfiguration config;

	/** The project. */
	private final Project project;

	/**
	 * Instantiates a new php unit executor.
	 * 
	 * @param config the config
	 * @param project the project
	 */
	public PhpUnitExecutor(PhpUnitConfiguration config, Project project) {
		super();
		this.config = config;
		this.project = project;
	}

	/**
	 * @see org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor#getCommandLine()
	 */
	@Override
	protected List<String> getCommandLine() {
		List<String> result = new ArrayList<String>();
		result.add(config.getOsDependentToolScriptName());
		if (config.isStringPropertySet(PhpUnitConfiguration.FILTER_PROPERTY_KEY)) {
			result.add(PhpUnitConfiguration.FILTER_OPTION + config.getFilter());
		} else if (config.isStringPropertySet(PhpUnitConfiguration.BOOTSTRAP_PROPERTY_KEY)) {
			result.add(PhpUnitConfiguration.BOOTSTRAP_OPTION + config.getBootstrap());
		} else if (config.isStringPropertySet(PhpUnitConfiguration.CONFIGURATION_PROPERTY_KEY)) {
			result.add(PhpUnitConfiguration.CONFIGURATION_OPTION + config.getConfiguration());
		} else if (config.isStringPropertySet(PhpUnitConfiguration.LOADER_PROPERTY_KEY)) {
			result.add(PhpUnitConfiguration.LOADER_OPTION + config.getLoader());
		} else if (config.isStringPropertySet(PhpUnitConfiguration.GROUP_PROPERTY_KEY)) {
			result.add(PhpUnitConfiguration.GROUP_OPTION + config.getGroup());
		} else if (config.isStringPropertySet(PhpUnitConfiguration.ARGUMENT_LINE_KEY)) {
			result.add(config.getArgumentLine());
		}
		result.add("--log-junit=" + config.getReportFile());
		if (config.shouldRunCoverage()) {
			result.add("--coverage-clover=" + config.getCoverageReportFile());
		}
		result.add(project.getName());
		result.add(config.getMainTestClass());
		return result;
	}

	/**
	 * @see org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor#getExecutedTool()
	 */
	@Override
	protected String getExecutedTool() {
		return "PHP UNIT";
	}

}
