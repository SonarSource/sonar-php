/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.php.phpunit;

import static java.lang.Boolean.parseBoolean;
import static org.sonar.plugins.php.core.Php.PHP;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_SHOULD_RUN_COVERAGE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_SHOULD_RUN_COVERAGE_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_SHOULD_RUN_PROPERTY_KEY;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

import com.thoughtworks.xstream.XStreamException;

/**
 * The Class PhpUnitSensor is used by the plugin to collect metrics concerning punit framework. This class doesn't launch the tests, it only
 * reads the results contains in the files found under the report directory set as a plugin property and which names begin with "punit" and
 * end with ".xml".
 */
public class PhpUnitSensor implements Sensor {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpUnitSensor.class);
  private PhpUnitExecutor executor;
  private PhpUnitResultParser parser;
  private PhpUnitCoverageResultParser coverageParser;

  /**
   * @param executor
   * @param parser
   */
  public PhpUnitSensor(PhpUnitExecutor executor, PhpUnitResultParser parser, PhpUnitCoverageResultParser coverageParser) {
    super();
    this.executor = executor;
    this.parser = parser;
    this.coverageParser = coverageParser;
  }

  /**
   * Analyse.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    try {
      PhpUnitConfiguration configuration = executor.getConfiguration();
      configuration.createWorkingDirectory();

      if ( !configuration.isAnalyseOnly()) {
        executor.execute();
      }
      parser.parse(configuration.getReportFile());
      if (configuration.shouldRunCoverage()) {
        coverageParser.parse(configuration.getCoverageReportFile());
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
   * @param project
   *          The project to be analyzed
   * @return boolean <code>true</code> if project's language is php a,d the project configuration says so, <code>false</code> in any other
   *         case.
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api .resources.Project)
   */
  public boolean shouldExecuteOnProject(Project project) {

    Configuration configuration = project.getConfiguration();
    Language language = project.getLanguage();
    boolean shouldExecute = PHP.equals(language);
    shouldExecute = shouldExecute
        && (configuration.getBoolean(PHPUNIT_SHOULD_RUN_PROPERTY_KEY, parseBoolean(PHPUNIT_DEFAULT_SHOULD_RUN)) || configuration
            .getBoolean(PHPUNIT_SHOULD_RUN_COVERAGE_PROPERTY_KEY, parseBoolean(PHPUNIT_DEFAULT_SHOULD_RUN_COVERAGE)));
    return shouldExecute;
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
