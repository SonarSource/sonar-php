package org.sonar.plugins.php.codesniffer.configuration;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration;

/**
 * The Class PhpCheckstyleConfiguration.
 */
public class PhpCodesnifferConfiguration extends PhpPluginAbstractConfiguration {

	/** The Constant DEFAULT_REPORT_FILE_NAME. */
	public static final String DEFAULT_REPORT_FILE_NAME = "codesniffer.xml";

	/** The Constant DEFAULT_REPORT_FILE_PATH. */
	public static final String DEFAULT_REPORT_FILE_PATH = "/logs";

	/** The Constant REPORT_FILE_NAME_PROPERTY_KEY. */
	public static final String REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpCodesniffer.reportFileName";

	/** The Constant REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY. */
	public static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpCodesniffer.reportFileRelativePath";

	/** The Constant SHOULD_ANALYSE_ONLY_KEY. */
	public static final String ANALYZE_ONLY_KEY = "sonar.phpCodesniffer.analyseOnly";

	/** The Constant SHOULD_RUN_KEY. */
	public static final String SHOULD_RUN_KEY = "sonar.phpCodesniffer.shouldRun";

	/** The Constant DEFAULT_SHOULD_ANALYSE_ONLY. */
	public static final String DEFAULT_ANALYZE_ONLY = "false";

	/** The Constant DEFAULT_SHOULD_RUN. */
	public static final String DEFAULT_SHOULD_RUN = "true";

	/** The Constant LEVEL_ARGUMENT_KEY. */
	public static final String LEVEL_ARGUMENT_KEY = "sonar.phpCodesniffer.levelArgument";

	/** The Constant DEFAULT_LEVEL_ARGUMENT. */
	public static final String DEFAULT_LEVEL_ARGUMENT = "warning";

	/** The Constant LEVEL_OPTION. */
	public static final String LEVEL_OPTION = "--level=";

	/** The Constant LEVEL_ARGUMENT_KEY. */
	public static final String STANDARD_ARGUMENT_KEY = "sonar.phpCodesniffer.standardArgument";

	/** The Constant DEFAULT_STANDARD_ARGUMENT. */
	public static final String DEFAULT_STANDARD_ARGUMENT = "GN";

	/** The Constant STANDARD_OPTION. */
	public static final String STANDARD_OPTION = "--standard=";

	/** The Constant STANDARD_OPTION. */
	public static final String REPORT_FILE_OPTION = "--report-file=";

	/** The Constant ARGUMENT_LINE_KEY. */
	public static final String ARGUMENT_LINE_KEY = "sonar.phpCodesniffer.argumentLine";

	/** The Constant DEFAULT_ARGUMENT_LINE. */
	public static final String DEFAULT_ARGUMENT_LINE = " ";

	/** The Constant DEFAULT_IGNORE_ARGUMENT. */
	public static final String DEFAULT_IGNORE_ARGUMENT = " ";

	/** The Constant IGNORE_ARGUMENT_KEY. */
	public static final String IGNORE_ARGUMENT_KEY = "sonar.phpCodesniffer.ignoreArgument";

	/** The Constant REPORT_OPTION. */
	public static final String REPORT_OPTION = "--report=checkstyle";

	/** The Constant EXTENSIONS_OPTION. */
	public static final String EXTENSIONS_OPTION = "--extensions=";

	/** The Constant IGNORE_OPTION. */
	public static final String IGNORE_OPTION = "--ignore=";

	/** The Constant COMMAND_LINE. */
	private static final String COMMAND_LINE = "sqlics";

	// Only for unit tests
	/**
	 * Instantiates a new php checkstyle configuration.
	 */
	protected PhpCodesnifferConfiguration() {
		super();
	}

	/**
	 * Instantiates a new php checkstyle configuration.
	 * 
	 * @param pom the pom
	 */
	public PhpCodesnifferConfiguration(Project pom) {
		super();
		init(pom);
	}

	/**
	 * Gets the default report file name.
	 * 
	 * @return the default report file name
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getDefaultReportFileName()
	 */
	@Override
	protected String getDefaultReportFileName() {
		return DEFAULT_REPORT_FILE_NAME;
	}

	/**
	 * Gets the default report file path.
	 * 
	 * @return the default report file path
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getDefaultReportFilePath()
	 */
	@Override
	protected String getDefaultReportFilePath() {
		return DEFAULT_REPORT_FILE_PATH;
	}

	/**
	 * Gets the report file name key.
	 * 
	 * @return the report file name key
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getReportFileNameKey()
	 */
	@Override
	protected String getReportFileNameKey() {
		return REPORT_FILE_NAME_PROPERTY_KEY;
	}

	/**
	 * Gets the report file relative path key.
	 * 
	 * @return the report file relative path key
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getReportFileRelativePathKey()
	 */
	@Override
	protected String getReportFileRelativePathKey() {
		return REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
	}

	/**
	 * Gets the should analyse only key.
	 * 
	 * @return the should analyse only key
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getShouldAnalyzeOnlyKey()
	 */
	@Override
	protected String getShouldAnalyzeOnlyKey() {
		return ANALYZE_ONLY_KEY;
	}

	/**
	 * Gets the should run key.
	 * 
	 * @return the should run key
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getShouldRunKey()
	 */
	@Override
	protected String getShouldRunKey() {
		return SHOULD_RUN_KEY;
	}

	/**
	 * Should analyze only default.
	 * 
	 * @return true, if should analyze only default
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#shouldAnalyseOnlyDefault()
	 */
	@Override
	protected boolean shouldAnalyzeOnlyDefault() {
		return Boolean.parseBoolean(DEFAULT_ANALYZE_ONLY);
	}

	/**
	 * Should run default.
	 * 
	 * @return true, if should run default
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#shouldRunDefault()
	 */
	@Override
	protected boolean shouldRunDefault() {
		return Boolean.parseBoolean(DEFAULT_SHOULD_RUN);
	}

	/**
	 * Gets the argument line key.
	 * 
	 * @return the argument line key
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getArgumentLineKey()
	 */
	@Override
	protected String getArgumentLineKey() {
		return ARGUMENT_LINE_KEY;
	}

	/**
	 * Gets the default argument line value.
	 * 
	 * @return the default argument line
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getDefaultArgumentLine()
	 */
	@Override
	protected String getDefaultArgumentLine() {
		return DEFAULT_ARGUMENT_LINE;
	}

	/**
	 * Gets the external tool command line
	 * @return the external tool command line
	 * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getCommandLine()
	 */
	@Override
	protected String getCommandLine() {
		return COMMAND_LINE;
	}

	/**
	 * Gets the level argument value.
	 * 
	 * @return the level
	 */
	public String getLevel() {
		return project.getConfiguration().getString(LEVEL_ARGUMENT_KEY, DEFAULT_LEVEL_ARGUMENT);
	}

	/**
	 * Gets the standard argument value.
	 * 
	 * @return the standard
	 */
	public String getStandard() {
		return project.getConfiguration().getString(STANDARD_ARGUMENT_KEY, DEFAULT_STANDARD_ARGUMENT);
	}

	/**
	 * Gets the ignore list argument value.
	 * 
	 * @return the ignore list
	 */
	public String getIgnoreList() {
		String[] values = project.getConfiguration().getStringArray(IGNORE_ARGUMENT_KEY);
		if (values != null && values.length > 0) {
			return StringUtils.join(values, ',');
		}
		return null;
	}
}
