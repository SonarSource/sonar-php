/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpdepend.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.executor.PhpPluginExecutionException;
import org.sonar.plugins.php.phpdepend.configuration.PhpDependConfiguration;
import org.sonar.plugins.php.phpdepend.executor.PhpDependExecutor;

/**
 * This class is in charge of knowing wether or not it has to be launched depending on a given project. In case it has to be launched, the
 * sensor, choose between execute phpDepend and analyze its result or only analyze its result
 */
public class PhpDependSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpDependSensor.class);

  private PhpDependConfiguration config;

  /**
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    try {
      if ( !getConfig(project).isAnalyseOnly()) {
        PhpDependExecutor executor = new PhpDependExecutor(config);
        executor.execute();
      }
      PhpDependResultsParser parser = new PhpDependResultsParser(project, context);
      parser.parse(config.getReportFile());
    } catch (PhpPluginExecutionException e) {
      LOG.error("Error occured while launching PhpDepend", e);
    }
  }

  /**
   * Determines whether or not this sensor will be executed on the given project
   * 
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api .resources.Project)
   * @param project
   *          The project to be analyzed
   * @return boolean <code>true</code> if project's language is php a,d the project configuration says so, <code>false</code> in any other
   *         case.
   */
  public boolean shouldExecuteOnProject(Project project) {
    return getConfig(project).isShouldRun() && Php.INSTANCE.equals(project.getLanguage());
  }

  /**
   * Returns a PhpDependConfiguration plugin configuration. If the instance variable isn't initialize, the method instantiates it before
   * returning it.
   * 
   * @param project
   *          the analyzed project
   * @return
   */
  private PhpDependConfiguration getConfig(Project project) {
    if (config == null) {
      config = new PhpDependConfiguration(project);
    }
    return config;
  }
}
