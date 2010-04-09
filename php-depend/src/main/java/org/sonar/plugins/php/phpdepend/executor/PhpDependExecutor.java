package org.sonar.plugins.php.phpdepend.executor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor;
import org.sonar.plugins.php.phpdepend.configuration.PhpDependConfiguration;

/**
 * The Class PhpDependExecutor.
 */
public class PhpDependExecutor extends PhpPluginAbstractExecutor {

	/** The configuration. */
	private PhpDependConfiguration config;

	/**
	 * Instantiates a new php depend executor.
	 * 
	 * @param configuration the configuration
	 */
	public PhpDependExecutor(PhpDependConfiguration configuration) {
		this.config = configuration;
	}

	@Override
	protected List<String> getCommandLine() {
		List<String> result = new ArrayList<String>();
		result.add(config.getOsDependentToolScriptName());
		result.add(config.getReportFileCommandOption());
		result.add(config.getSuffixesCommandOption());
		if (config.isStringPropertySet(PhpDependConfiguration.EXCLUDE_PACKAGE_KEY)) {
			result.add(PhpDependConfiguration.EXCLUDE_OPTION + config.getExcludePackeges());
		}
		if (config.isStringPropertySet(PhpDependConfiguration.IGNORE_KEY)) {
			result.add(PhpDependConfiguration.IGNORE_OPTION + config.getIgnoreDirs());
		}
		if (config.isBadDocumentation()) {
			result.add(PhpDependConfiguration.BAD_DOCUMENTATION_OPTION);
		}
		if (config.isWithoutAnnotation()) {
			result.add(PhpDependConfiguration.WITHOUT_ANNOTATION_OPTION);
		}
		if (config.isStringPropertySet(PhpDependConfiguration.ARGUMENT_LINE_KEY))
			result.add(config.getArgumentLine());
		for (File file : config.getSourceDirectories()) {
			result.add(file.getAbsolutePath());
		}
		return result;
	}

	@Override
	protected String getExecutedTool() {
		return PhpDependConfiguration.COMMAND_LINE;
	}
}
