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

/**
 * 
 */
package org.sonar.plugins.php.cpd.sensor;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration;
import org.sonar.plugins.php.cpd.configuration.PhpCpdConfiguration;
import org.sonar.plugins.php.cpd.executor.PhpCpdExecutor;

/**
 * PhpCpd sensor that rely on "phpcpd" tool to perform copy paste detection.
 * 
 * @author akram
 * 
 */
public class PhpCpdSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpCpdSensor.class);
  /** The config. */
  private PhpCpdConfiguration config;

  /**
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api.resources.Project)
   */
  public boolean shouldExecuteOnProject(Project project) {
    return getConfig(project).isShouldRun() && Php.INSTANCE.equals(project.getLanguage());
  }

  /**
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    PhpCpdExecutor executor = new PhpCpdExecutor(config);
    executor.execute();
    File reportFile = getConfig(project).getReportFile();
    LOG.debug("Starting analysis of copy/paste with report file" + reportFile);
    PhpCpdResultParser parser = new PhpCpdResultParser(project, context);
    parser.parse(reportFile);
  }

  /**
   * Gets the config.
   * 
   * @param project
   *          the project
   * @return the config
   */
  private PhpPluginAbstractConfiguration getConfig(Project project) {
    if (config == null) {
      config = new PhpCpdConfiguration(project);
    }
    LOG.debug("Configuration: " + config);
    return config;
  }

}
