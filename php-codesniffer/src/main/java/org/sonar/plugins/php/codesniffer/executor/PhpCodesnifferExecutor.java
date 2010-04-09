package org.sonar.plugins.php.codesniffer.executor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.codesniffer.configuration.PhpCodesnifferConfiguration;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor;

/**
 * The Class PhpCheckstyleExecutor.
 */
public class PhpCodesnifferExecutor extends PhpPluginAbstractExecutor {

	/** The PhpCodesnifferConfiguration. */
	private PhpCodesnifferConfiguration config;

	/**
	 * Instantiates a new php codesniffer executor.
	 * 
	 * @param configuration the configuration
	 */
	public PhpCodesnifferExecutor(PhpCodesnifferConfiguration configuration) {
		this.config = configuration;
	}

	/**
	 * @see org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor#getCommandLine()
	 */
	@Override
	protected List<String> getCommandLine() {
		List<String> result = new ArrayList<String>();
		result.add(config.getOsDependentToolScriptName());
		result.add(PhpCodesnifferConfiguration.REPORT_FILE_OPTION + config.getReportFile());
		result.add(PhpCodesnifferConfiguration.REPORT_OPTION);
		if (config.isStringPropertySet(PhpCodesnifferConfiguration.LEVEL_ARGUMENT_KEY)) {
			result.add(PhpCodesnifferConfiguration.LEVEL_OPTION + config.getLevel());
		} else {
			result.add(PhpCodesnifferConfiguration.LEVEL_OPTION + PhpCodesnifferConfiguration.DEFAULT_LEVEL_ARGUMENT);
		}
		if (config.isStringPropertySet(PhpCodesnifferConfiguration.STANDARD_ARGUMENT_KEY)) {
			result.add(PhpCodesnifferConfiguration.STANDARD_OPTION + config.getStandard());
		} else {
			result.add(PhpCodesnifferConfiguration.STANDARD_OPTION + PhpCodesnifferConfiguration.DEFAULT_STANDARD_ARGUMENT);
		}
		if (config.isStringPropertySet(PhpCodesnifferConfiguration.IGNORE_ARGUMENT_KEY)) {
			result.add(PhpCodesnifferConfiguration.IGNORE_OPTION + config.getIgnoreList());
		}
		if (config.isStringPropertySet(PhpCodesnifferConfiguration.ARGUMENT_LINE_KEY)) {
			result.add(PhpCodesnifferConfiguration.IGNORE_OPTION + config.getArgumentLine());
		}
		result.add(PhpCodesnifferConfiguration.EXTENSIONS_OPTION + StringUtils.join(Php.SUFFIXES, ","));
		for (File file : config.getSourceDirectories()) {
			result.add(file.getAbsolutePath());
		}
		return result;
	}

	/**
	 * @see org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor#getExecutedTool()
	 */
	@Override
	protected String getExecutedTool() {
		return "PHPCodeSniffer";
	}
}
