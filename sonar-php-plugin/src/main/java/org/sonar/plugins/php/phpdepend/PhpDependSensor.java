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

import static org.sonar.plugins.php.api.Php.PHP;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

/**
 * This class is in charge of knowing wether or not it has to be launched depending on a given project. In case it has to be launched, the
 * sensor, choose between execute phpDepend and analyze its result or only analyze its result
 */
public class PhpDependSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpDependSensor.class);

  private PhpDependConfiguration configuration;
  private PhpDependExecutor executor;
  private PhpDependResultsParser parser;

  /**
   * @param executor
   * @param parser
   */
  public PhpDependSensor(PhpDependConfiguration config, PhpDependExecutor executor, PhpDependResultsParser parser) {
    super();
    this.configuration = config;
    this.executor = executor;
    this.parser = parser;
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
      File reportFile = configuration.getReportFile();
      parser.parse(reportFile);
    } catch (PhpPluginExecutionException e) {
      LOG.error("Error occured while launching PhpDepend", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    if (!PHP.equals(project.getLanguage())) {
      return false;
    }

    return !configuration.isSkip();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHP Depend Sensor";
  }
}
