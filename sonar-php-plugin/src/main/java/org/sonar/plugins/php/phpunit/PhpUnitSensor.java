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
package org.sonar.plugins.php.phpunit;

import static org.sonar.plugins.php.api.Php.PHP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
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

  private PhpUnitConfiguration configuration;
  private PhpUnitExecutor executor;
  private PhpUnitResultParser parser;
  private PhpUnitCoverageResultParser coverageParser;

  /**
   * @param executor
   * @param parser
   */
  public PhpUnitSensor(PhpUnitConfiguration conf, PhpUnitExecutor executor, PhpUnitResultParser parser,
      PhpUnitCoverageResultParser coverageParser) {
    super();
    this.configuration = conf;
    this.executor = executor;
    this.parser = parser;
    this.coverageParser = coverageParser;
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    try {
      configuration.createWorkingDirectory();

      if (!configuration.isAnalyseOnly()) {
        executor.execute();
      }
      parser.parse(configuration.getReportFile());
      if (!configuration.shouldSkipCoverage()) {
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
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    if (!PHP.equals(project.getLanguage()) || !configuration.isDynamicAnalysisEnabled()) {
      return false;
    }

    return !configuration.isSkip() || !configuration.shouldSkipCoverage();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "phpunit Sensor";
  }
}
