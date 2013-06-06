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
package org.sonar.plugins.php.phpdepend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.api.PhpConstants;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

import java.io.File;

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
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_PATH_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_TYPE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_TYPE_DEFVALUE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_SKIP_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_TIMEOUT_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_DEFVALUE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_KEY;

/**
 * This class is in charge of knowing whether or not it has to be launched depending on a given project. In case it has to be launched, the
 * sensor, choose between execute phpDepend and analyze its result or only analyze its result
 */
@Properties({
  @Property(key = PDEPEND_SKIP_KEY, defaultValue = "false", name = "Disable PHP Depend", project = true, global = true,
    description = "If set to true, PHP Depend will not run. Skipping PHP Depend is not a good idea because most of the metrics will not be computed.",
    category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND,
    type = PropertyType.BOOLEAN),
  @Property(key = PDEPEND_ANALYZE_ONLY_KEY, defaultValue = "false", name = "Only analyze existing PHP Depend report files",
    project = true, global = true,
    description = "If set to false, PHP Depend will be executed. If set to true, PHP Depend will not be executed and the report provided through the \""
      + PDEPEND_REPORT_PATH_KEY + "\" property will be used.",
    category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND,
    type = PropertyType.BOOLEAN),
  @Property(key = PDEPEND_REPORT_PATH_KEY,
    name = "Report file path", project = true, global = true, description = "Relative path to the report to analyze. Example: path/to/pdepend.xml.",
    category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY, defaultValue = PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE,
    name = "Report file path (Deprecated)", project = true, global = true, description = "Replaced by the \"" + PDEPEND_REPORT_PATH_KEY + "\" property.",
    category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_REPORT_FILE_NAME_KEY, defaultValue = PDEPEND_REPORT_FILE_NAME_DEFVALUE, name = "Report file name (Deprecated)",
    project = true, global = true, description = "Replaced by the \"" + PDEPEND_REPORT_PATH_KEY + "\" property.",
    category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_WITHOUT_ANNOTATION_KEY, defaultValue = PDEPEND_WITHOUT_ANNOTATION_DEFVALUE, name = "Without annotation",
    project = true, global = true, description = "If set to true, tells PHP Depend to not parse doc comment annotations.",
    category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND,
    type = PropertyType.BOOLEAN),
  @Property(key = PDEPEND_BAD_DOCUMENTATION_KEY, defaultValue = PDEPEND_BAD_DOCUMENTATION_DEFVALUE, name = "Check bad documentation",
    project = true, global = true, description = "If set to true, tells PHP Depend to check "
      + "that annotations are used for documentation.", category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND,
    type = PropertyType.BOOLEAN),
  @Property(key = PDEPEND_EXCLUDE_PACKAGE_KEY, defaultValue = "", name = "Package to exclude", project = true, global = true,
    description = "Comma separated string of packages that will be excluded during the parsing process.",
    category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_ARGUMENT_LINE_KEY, defaultValue = "", name = "Additional arguments", project = true, global = true,
    description = "Additional parameters that can be passed to PHP Depend.", category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_TIMEOUT_KEY, defaultValue = "" + DEFAULT_TIMEOUT, name = "Timeout", project = true, global = true,
    description = "Execution of PHP Depend will be stopped after this amount of time (in minutes).", category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND),
  @Property(key = PDEPEND_REPORT_TYPE, defaultValue = PDEPEND_REPORT_TYPE_DEFVALUE, name = "XML report type", project = true, global = true,
    description = "Type of report PHP Depend will generate and Sonar analyse afterwards. Valid values: summary-xml, phpunit-xml (deprecated)",
    category = PhpDependSensor.CATEGORY_PHP_PHP_DEPEND,
    type = PropertyType.SINGLE_SELECT_LIST,
    options = {PhpDependConfiguration.PDEPEND_REPORT_TYPE_SUMMARY, PhpDependConfiguration.PDEPEND_REPORT_TYPE_PHPUNIT})
})
public class PhpDependSensor implements Sensor {

  protected static final String CATEGORY_PHP_PHP_DEPEND = "PHP Depend";

  private static final Logger LOG = LoggerFactory.getLogger(PhpDependSensor.class);

  private PhpDependConfiguration configuration;
  private PhpDependExecutor executor;
  private PhpDependParserSelector parserSelector;

  /**
   * @param config
   * @param executor
   * @param parserSelector
   */
  public PhpDependSensor(PhpDependConfiguration config, PhpDependExecutor executor, PhpDependParserSelector parserSelector) {
    super();
    this.configuration = config;
    this.executor = executor;
    this.parserSelector = parserSelector;
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    PhpDependResultsParser parser = parserSelector.select();
    try {
      configuration.createWorkingDirectory();
      if (!configuration.isAnalyseOnly()) {
        executor.execute();
      }
      File reportFile = configuration.getReportFile();
      parser.parse(reportFile);
    } catch (PhpPluginExecutionException e) {
      LOG.error("Error occurred while launching PhpDepend", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    return PhpConstants.LANGUAGE_KEY.equals(project.getLanguageKey())
      && !configuration.isSkip()
      && !project.getFileSystem().mainFiles(PhpConstants.LANGUAGE_KEY).isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHP Depend Sensor";
  }
}
