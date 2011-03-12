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

/**
 *
 */
package org.sonar.plugins.php.cpd;

import static java.lang.Boolean.parseBoolean;
import static org.sonar.plugins.php.core.Php.PHP;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_SHOULD_RUN_PROPERTY_KEY;

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
 * PhpCpd sensor that rely on "phpcpd" tool to perform copy paste detection.
 * 
 * @author akram
 * 
 */
public class PhpCpdSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpCpdSensor.class);
  private static final String SONAR_PHP_CPD_SKIP_KEY = "sonar.php.cpd.skip";
  private static final Boolean SONAR_DEFAULT_PHP_CPD_SKIP = false;
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
    Configuration c = project.getConfiguration();
    Language language = project.getLanguage();
    Boolean phpcpdDefaultShouldRun = parseBoolean(PHPCPD_DEFAULT_SHOULD_RUN);
    return ((project.getPom() != null) && PHP.equals(language))
        && ( !c.getBoolean(SONAR_PHP_CPD_SKIP_KEY, SONAR_DEFAULT_PHP_CPD_SKIP) || c.getBoolean(PHPCPD_SHOULD_RUN_PROPERTY_KEY,
            phpcpdDefaultShouldRun));
  }

  /**
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
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
