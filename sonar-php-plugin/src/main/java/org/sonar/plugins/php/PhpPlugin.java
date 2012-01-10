/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.php;

import static org.sonar.plugins.php.PhpPlugin.FILE_SUFFIXES_DEFVALUE;
import static org.sonar.plugins.php.PhpPlugin.FILE_SUFFIXES_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_ANALYZE_ONLY_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_DEFVALUE;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SEVERITY_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SKIP_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_STANDARD_ARGUMENT_DEFVALUE;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_TIMEOUT_KEY;
import static org.sonar.plugins.php.core.AbstractPhpConfiguration.DEFAULT_TIMEOUT;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_ANALYZE_ONLY_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_BAD_DOCUMENTATION_DEFVALUE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_BAD_DOCUMENTATION_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_EXCLUDE_PACKAGE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_FILE_NAME_DEFVALUE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_FILE_NAME_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_SKIP_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_TIMEOUT_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_DEFVALUE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_ONLY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_TEST_DIRECTORY_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_FILE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_SKIP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_FILTER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_GROUP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_IGNORE_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_LOADER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_SKIP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_TIMEOUT_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_ANALYZE_ONLY_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_LEVEL_ARGUMENT_DEFVALUE;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_LEVEL_ARGUMENT_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_DEFVALUE;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_SKIP_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_TIMEOUT_KEY;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferPriorityMapper;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferProfileExporter;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferProfileImporter;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferSensor;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferViolationsXmlParser;
import org.sonar.plugins.php.core.NoSonarAndCommentedOutLocSensor;
import org.sonar.plugins.php.core.PhpSourceCodeColorizer;
import org.sonar.plugins.php.core.PhpSourceImporter;
import org.sonar.plugins.php.core.profiles.AllPhpCSProfile;
import org.sonar.plugins.php.core.profiles.AllPhpmdProfile;
import org.sonar.plugins.php.core.profiles.PearProfile;
import org.sonar.plugins.php.core.profiles.SonarWayProfile;
import org.sonar.plugins.php.core.profiles.ZendProfile;
import org.sonar.plugins.php.duplications.PhpCPDMapping;
import org.sonar.plugins.php.phpdepend.PhpDependConfiguration;
import org.sonar.plugins.php.phpdepend.PhpDependExecutor;
import org.sonar.plugins.php.phpdepend.PhpDependResultsParser;
import org.sonar.plugins.php.phpdepend.PhpDependSensor;
import org.sonar.plugins.php.phpunit.PhpUnitConfiguration;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageDecorator;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitExecutor;
import org.sonar.plugins.php.phpunit.PhpUnitResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitSensor;
import org.sonar.plugins.php.pmd.PhpmdConfiguration;
import org.sonar.plugins.php.pmd.PhpmdExecutor;
import org.sonar.plugins.php.pmd.PhpmdProfileExporter;
import org.sonar.plugins.php.pmd.PhpmdProfileImporter;
import org.sonar.plugins.php.pmd.PhpmdRuleRepository;
import org.sonar.plugins.php.pmd.PhpmdSensor;
import org.sonar.plugins.php.pmd.PmdRulePriorityMapper;

/**
 * This class is the sonar entry point of this plugin. It declares all the extension that can be launched with this plugin.
 */
@Properties({
  // ------------------ Global Php Configuration ------------------
  @Property(key = FILE_SUFFIXES_KEY, defaultValue = FILE_SUFFIXES_DEFVALUE, name = "File suffixes", project = true, global = true,
    description = "Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty."),

  // ------------------ Phpmd configuration ------------------
  @Property(key = PHPMD_SKIP_KEY, defaultValue = "false", name = "Disable PHPMD", project = true, global = true,
    description = "Disabling PHPMD is not a good idea because almost all metrics rely on it.", category = PhpPlugin.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_ANALYZE_ONLY_KEY, defaultValue = "false", name = "Only analyze existing Phpmd report files", project = true,
    global = true, description = "By default, the plugin will launch PHPMD and parse the generated result file."
      + "If this option is set to true, the plugin will only reuse an existing report file.", category = PhpPlugin.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_REPORT_FILE_RELATIVE_PATH_KEY, defaultValue = PHPMD_REPORT_FILE_RELATIVE_PATH_DEFVALUE,
    name = "Report file path", project = true, global = true, description = "Relative path of the report file to analyse.",
    category = PhpPlugin.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_REPORT_FILE_NAME_KEY, defaultValue = PHPMD_REPORT_FILE_NAME_DEFVALUE, name = "Report file name", project = true,
    global = true, description = "Name of the report file to analyse.", category = PhpPlugin.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_LEVEL_ARGUMENT_KEY, defaultValue = PHPMD_LEVEL_ARGUMENT_DEFVALUE, name = "Mimimum priority", project = true,
    global = true, description = "The lowest level events won't be included in report file. "
      + "Values goes from 1(Strong) to 5(Weak) (only integers)", category = PhpPlugin.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_ARGUMENT_LINE_KEY, defaultValue = "", name = "Additional arguments", project = true, global = true,
    description = "Additionnal parameters that can be passed to PHPMD tool.", category = PhpPlugin.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_TIMEOUT_KEY, defaultValue = "" + DEFAULT_TIMEOUT, name = "Timeout", project = true, global = true,
    description = "Maximum number of minutes that the execution of the tool should take.", category = PhpPlugin.CATEGORY_PHP_PHPMD),

  // ------------------ PhpCodeSniffer configuration ------------------
  @Property(key = PHPCS_SKIP_KEY, defaultValue = "false", name = "Disable PHP CodeSniffer", project = true, global = true,
    description = "If true, PhpCodeSniffer engine will not run and its violations will not be present in Sonar dashboard.",
    category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),
  @Property(key = PHPCS_ANALYZE_ONLY_KEY, defaultValue = "false", name = "Only analyze existing PHP CodeSniffer report files",
    project = true, global = true,
    description = "By default, the plugin will launch PHP CodeSniffer and parse the generated result file."
      + "If this option is set to true, the plugin will only reuse an existing report file.",
    category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),
  @Property(key = PHPCS_REPORT_FILE_RELATIVE_PATH_KEY, defaultValue = PHPCS_REPORT_FILE_RELATIVE_PATH_DEFVALUE,
    name = "Report file path", project = true, global = true, description = "Relative path of the report file to analyse.",
    category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),
  @Property(key = PHPCS_REPORT_FILE_NAME_KEY, defaultValue = PHPCS_REPORT_FILE_NAME_DEFVALUE, name = "Report file name", project = true,
    global = true, description = "Name of the report file to analyse.", category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),
  @Property(key = PhpCodeSnifferConfiguration.PHPCS_STANDARD_ARGUMENT_KEY, defaultValue = PHPCS_STANDARD_ARGUMENT_DEFVALUE,
    name = "Ruleset (or standard) to run PHP_CodeSniffer with", project = true, global = true,
    description = "The ruleset file (or the standard name) used to run PHP_CodeSniffer against. "
      + "If no one is specified all standards will be launched", category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),
  @Property(key = PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY, defaultValue = "", name = "Severity modifier", project = true, global = true,
    description = "Allows to specify a seveity modifier, like '--error-severity=' or '--warning-severity=', "
      + "used in conjunction with property '" + PHPCS_SEVERITY_KEY + "'.", category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),
  @Property(key = PHPCS_SEVERITY_KEY, defaultValue = "", name = "Severity level value", project = true, global = true,
    description = "Specifies what the minimum severity level must be to report a violation in the report.",
    category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),
  @Property(key = PHPCS_ARGUMENT_LINE_KEY, defaultValue = "", name = "Additional arguments", project = true, global = true,
    description = "Additionnal parameters that can be passed to PHP CodeSniffer tool.", category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),
  @Property(key = PHPCS_TIMEOUT_KEY, defaultValue = "" + DEFAULT_TIMEOUT, name = "Timeout", project = true, global = true,
    description = "Maximum number of minutes that the execution of the tool should take.", category = PhpPlugin.CATEGORY_PHP_CODE_SNIFFER),

  // ------------------ PhPdepend configuration ------------------
  @Property(key = PDEPEND_SKIP_KEY, defaultValue = "false", name = "Disable PHP Depend", project = true, global = true,
    description = "If true, PHP Depend engine will not run and its violations will not be present in Sonar dashboard.",
    category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_ANALYZE_ONLY_KEY, defaultValue = "false", name = "Only analyze existing PHP Depend report files",
    project = true, global = true, description = "By default, the plugin will launch PHP Depend and parse the generated result file."
      + "If this option is set to true, the plugin will only reuse an existing report file.",
    category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY, defaultValue = PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE,
    name = "Report file path", project = true, global = true, description = "Relative path of the report file to analyse.",
    category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_REPORT_FILE_NAME_KEY, defaultValue = PDEPEND_REPORT_FILE_NAME_DEFVALUE, name = "Report file name",
    project = true, global = true, description = "Name of the report file to analyse.", category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_WITHOUT_ANNOTATION_KEY, defaultValue = PDEPEND_WITHOUT_ANNOTATION_DEFVALUE, name = "Without annotation",
    project = true, global = true, description = "If set to true, tells PHP Depend to not parse doc comment annotations.",
    category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_BAD_DOCUMENTATION_KEY, defaultValue = PDEPEND_BAD_DOCUMENTATION_DEFVALUE, name = "Check bad documentation",
    project = true, global = true, description = "If set to true, tells PHP Depend to check "
      + "that annotations are used for documentation.", category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_EXCLUDE_PACKAGE_KEY, defaultValue = "", name = "Package to exclude", project = true, global = true,
    description = "Comma separated string of packages that will be excluded during the parsing process.",
    category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_ARGUMENT_LINE_KEY, defaultValue = "", name = "Additional arguments", project = true, global = true,
    description = "Additionnal parameters that can be passed to PHP Depend tool.", category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_TIMEOUT_KEY, defaultValue = "" + DEFAULT_TIMEOUT, name = "Timeout", project = true, global = true,
    description = "Maximum number of minutes that the execution of the tool should take.", category = PhpPlugin.CATEGORY_PHP_PHP_DEPEND),

  // ------------------ Phpunit Configuration ------------------
  @Property(key = PHPUNIT_SKIP_KEY, defaultValue = "false", name = "Disable PHPUnit", project = true, global = true,
    description = "If true, PHPUnit tests will not run and unit tests counts will not be present in Sonar dashboard.",
    category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_COVERAGE_SKIP_KEY, defaultValue = "false", name = "Disable PHPUnit coverage", project = true, global = true,
    description = "If true, code coverage measures will not be computed.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_ANALYZE_ONLY_KEY, defaultValue = "false", name = "Only analyze existing PHPUnit report files", project = true,
    global = true, description = "By default, the plugin will launch PHP Unit and parse the generated result file."
      + "If this option is set to true, the plugin will only reuse an existing report file.",
    category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY, defaultValue = PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE,
    name = "Report file path", project = true, global = true, description = "Relative path of the report file to analyse.",
    category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_REPORT_FILE_NAME_KEY, defaultValue = PHPUNIT_REPORT_FILE_NAME_DEFVALUE, name = "Report file name",
    project = true, global = true, description = "Name of the report file to analyse.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_COVERAGE_REPORT_FILE_KEY, defaultValue = PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE,
    name = "Coverage report file name", project = true, global = true, description = "Name of the coverage report file to analyse.",
    category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_MAIN_TEST_FILE_KEY, defaultValue = PHPUNIT_MAIN_TEST_FILE_DEFVALUE,
    name = "File containing the main method calling all the tests", project = true, global = true,
    description = "The project main test file including the relative path, ie : \"/source/tests/AllTests.php\". "
      + "If not present, phpunit will look for phpunit.xml file in test directory.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY, defaultValue = PHPUNIT_ANALYZE_TEST_DIRECTORY_DEFVALUE,
    name = "Should analyse the whole test directory", project = true, global = true,
    description = "If set to false, only tests listed in the main test file will be run.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_FILTER_KEY, defaultValue = "", name = "Test filter", project = true, global = true,
    description = "Filter which tests to run.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_BOOTSTRAP_KEY, defaultValue = "", name = "Bootstrap file", project = true, global = true,
    description = "A 'bootstrap' PHP file that is run before the tests.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_CONFIGURATION_KEY, defaultValue = "", name = "Configuration file", project = true, global = true,
    description = "Read configuration from XML file.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_IGNORE_CONFIGURATION_KEY, defaultValue = "false", name = "Ignore default configuration", project = true,
    global = true, description = "Ignore default configuration file (phpunit.xml).", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_LOADER_KEY, defaultValue = "", name = "PHPUnit loader", project = true, global = true,
    description = "Specifies which TestSuiteLoader implementation to use.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_GROUP_KEY, defaultValue = "", name = "Groups to run", project = true, global = true,
    description = "Only runs tests from the specified group(s).", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_ARGUMENT_LINE_KEY, defaultValue = "", name = "Additional arguments", project = true, global = true,
    description = "Additionnal parameters that can be passed to PHPUnit tool.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_TIMEOUT_KEY, defaultValue = "" + DEFAULT_TIMEOUT, name = "Timeout", project = true, global = true,
    description = "Maximum number of minutes that the execution of the tool should take.", category = PhpPlugin.CATEGORY_PHP_PHP_UNIT)})
public class PhpPlugin extends SonarPlugin {

  protected static final String CATEGORY_PHP_PHP_UNIT = "PHP Unit";
  protected static final String CATEGORY_PHP_PHP_DEPEND = "PHP Depend";
  protected static final String CATEGORY_PHP_CODE_SNIFFER = "PHP CodeSniffer";
  protected static final String CATEGORY_PHP_PHPMD = "PHP PMD";

  /** All the valid php files suffixes. */
  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";
  public static final String FILE_SUFFIXES_DEFVALUE = "php,php3,php4,php5,phtml,inc";

  /**
   * Gets the extensions.
   * 
   * @return the extensions
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();

    extensions.add(Php.class);

    // Core extensions
    extensions.add(PhpSourceImporter.class);
    extensions.add(PhpSourceCodeColorizer.class);
    extensions.add(NoSonarAndCommentedOutLocSensor.class);

    // Profiles
    extensions.add(SonarWayProfile.class);
    extensions.add(AllPhpmdProfile.class);
    extensions.add(AllPhpCSProfile.class);
    extensions.add(PearProfile.class);
    extensions.add(ZendProfile.class);

    // Duplications
    extensions.add(PhpCPDMapping.class);

    // PhpDepend
    extensions.add(PhpDependExecutor.class);
    extensions.add(PhpDependResultsParser.class);
    extensions.add(PhpDependConfiguration.class);
    extensions.add(PhpDependSensor.class);

    // Phpmd
    extensions.add(PhpmdSensor.class);
    extensions.add(PhpmdRuleRepository.class);
    extensions.add(PhpmdConfiguration.class);
    extensions.add(PhpmdExecutor.class);
    extensions.add(PmdRulePriorityMapper.class);
    extensions.add(PhpmdProfileImporter.class);
    extensions.add(PhpmdProfileExporter.class);

    // Code sniffer
    extensions.add(PhpCodeSnifferRuleRepository.class);
    extensions.add(PhpCodeSnifferExecutor.class);
    extensions.add(PhpCodeSnifferViolationsXmlParser.class);
    extensions.add(PhpCodeSnifferSensor.class);
    extensions.add(PhpCodeSnifferConfiguration.class);
    extensions.add(PhpCodeSnifferPriorityMapper.class);
    extensions.add(PhpCodeSnifferProfileExporter.class);
    extensions.add(PhpCodeSnifferProfileImporter.class);

    // PhpUnit
    extensions.add(PhpUnitConfiguration.class);
    extensions.add(PhpUnitSensor.class);
    extensions.add(PhpUnitExecutor.class);
    extensions.add(PhpUnitResultParser.class);
    extensions.add(PhpUnitCoverageResultParser.class);
    extensions.add(PhpUnitCoverageDecorator.class);

    return extensions;
  }
}
