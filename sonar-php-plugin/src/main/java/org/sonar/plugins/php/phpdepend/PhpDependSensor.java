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

package org.sonar.plugins.php.phpdepend;

import static java.lang.Boolean.parseBoolean;
import static org.sonar.plugins.php.core.Php.PHP;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_SHOULD_RUN_PROPERTY_KEY;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

/**
 * This class is in charge of knowing wether or not it has to be launched depending on a given project. In case it has to be launched, the
 * sensor, choose between execute phpDepend and analyze its result or only analyze its result
 */
public class PhpDependSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpDependSensor.class);

  private PhpDependExecutor executor;
  private PhpDependResultsParser parser;

  /**
   * @param executor
   * @param parser
   */
  public PhpDependSensor(PhpDependExecutor executor, PhpDependResultsParser parser) {
    super();
    this.executor = executor;
    this.parser = parser;
  }

  /**
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    try {
      PhpDependConfiguration configuration = executor.getConfiguration();
      configuration.createWorkingDirectory();
      if ( !configuration.isAnalyseOnly()) {
        executor.execute();
      }
      File reportFile = configuration.getReportFile();
      parser.parse(reportFile);
    } catch (PhpPluginExecutionException e) {
      LOG.error("Error occured while launching PhpDepend", e);
    }
  }

  /**
   * Returns <code>true</code> if the given project language is PHP and the project configuration is set to allow plugin to run.
   * 
   * @param project
   *          the project
   * 
   * @return true, if should execute on project
   * 
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api .resources.Project)
   */
  public boolean shouldExecuteOnProject(Project project) {
    Configuration configuration = project.getConfiguration();
    Language language = project.getLanguage();
    return (project.getPom() != null) && PHP.equals(language)
        && configuration.getBoolean(PDEPEND_SHOULD_RUN_PROPERTY_KEY, parseBoolean(PDEPEND_DEFAULT_SHOULD_RUN));
  }

  /**
   * The name of the sensor.
   */
  @Override
  public String toString() {
    return "PHP Depend Sensor";
  }
}
