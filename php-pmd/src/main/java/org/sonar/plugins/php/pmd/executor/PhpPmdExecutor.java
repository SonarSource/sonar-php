package org.sonar.plugins.php.pmd.executor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor;
import org.sonar.plugins.php.pmd.configuration.PhpPmdConfiguration;

/**
 * The Class PhpCheckstyleExecutor.
 */
public class PhpPmdExecutor extends PhpPluginAbstractExecutor {

	/** The config. */
	private PhpPmdConfiguration config;

	/**
	 * Instantiates a new php checkstyle executor.
	 * 
	 * @param configuration the configuration
	 */
	public PhpPmdExecutor(PhpPmdConfiguration configuration) {
		this.config = configuration;
	}

	/**
	 * Gets a complete command line with configured arguments
	 * 
	 * @return the command line
	 * 
	 * @see org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor#getCommandLine()
	 */
	@Override
	protected List<String> getCommandLine() {
		List<String> result = new ArrayList<String>();
		result.add(config.getOsDependentToolScriptName());
		for (File file : config.getSourceDirectories()) {
			result.add(file.getAbsolutePath());
		}
		result.add(PhpPmdConfiguration.REPORT_FORMAT);
		result.add(config.getRulesets());
		result.add(PhpPmdConfiguration.REPORT_FILE_OPTION);
		result.add(config.getReportFile().getAbsolutePath());
		result.add(PhpPmdConfiguration.LEVEL_OPTION);
		result.add(config.getLevel());
		if (config.isStringPropertySet(PhpPmdConfiguration.IGNORE_ARGUMENT_KEY)) {
			result.add(PhpPmdConfiguration.IGNORE_OPTION);
			result.add(config.getIgnoreList());
		}
		result.add(PhpPmdConfiguration.EXTENSIONS_OPTION);
		result.add(StringUtils.join(Php.SUFFIXES, ","));
		if (config.isStringPropertySet(PhpPmdConfiguration.ARGUMENT_LINE_KEY)) {
			result.add(config.getArgumentLine());
		}
		return result;
	}

	/**
	 * Gets the executed tool.
	 * 
	 * @return the executed tool
	 * 
	 * @see org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor#getExecutedTool()
	 */
	@Override
	protected String getExecutedTool() {
		return "PHPMD";
	}
}
