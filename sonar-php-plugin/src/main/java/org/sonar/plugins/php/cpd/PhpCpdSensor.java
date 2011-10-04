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
/**
 *
 */
package org.sonar.plugins.php.cpd;

import static org.sonar.plugins.php.api.Php.PHP;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

/**
 * PhpCpd sensor that rely on "phpcpd" tool to perform copy paste detection.
 * 
 * @author akram
 * 
 */
public class PhpCpdSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpCpdSensor.class);

  /** The configuration. */
  private PhpCpdConfiguration configuration;

  /** The executor. */
  private PhpCpdExecutor executor;

  /** The parser */
  private PhpCpdResultParser parser;

  /**
   * @param configuration
   */
  public PhpCpdSensor(PhpCpdConfiguration config, PhpCpdExecutor executor, PhpCpdResultParser parser) {
    super();
    this.configuration = config;
    this.executor = executor;
    this.parser = parser;
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    if ( !PHP.equals(project.getLanguage())) {
      return false;
    }

    return !configuration.isSkip();
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    try {
      if ( !configuration.isAnalyseOnly()) {
        executor.execute();
      }

      File reportFile = configuration.getReportFile();
      LOG.debug("Starting analysis of copy/paste with report file" + reportFile);
      parser.parse(reportFile);
    } catch (PhpPluginExecutionException e) {
      LOG.error("Error occured while launching phpcpd", e);
    }
  }
}
