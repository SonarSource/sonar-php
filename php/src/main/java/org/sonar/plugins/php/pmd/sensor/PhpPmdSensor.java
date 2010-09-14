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

package org.sonar.plugins.php.pmd.sensor;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractViolationsStaxParser;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RulesManager;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.pmd.configuration.PhpPmdConfiguration;
import org.sonar.plugins.php.pmd.executor.PhpPmdExecutor;

/**
 * The plugin entry point.
 */
public class PhpPmdSensor implements Sensor {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpPmdSensor.class);

  /** The profile. */
  private RulesProfile profile;

  /** The rules manager. */
  private RulesManager rulesManager;

  /** The plugin configuration. */
  private PhpPmdConfiguration config;

  /**
   * Constructor used for tests.
   */
  public PhpPmdSensor() {
    super();
  }

  /**
   * Instantiates a new php pmd sensor.
   * 
   * @param profile
   *          the profile
   * @param rulesManager
   *          the rules manager
   */
  public PhpPmdSensor(RulesProfile profile, RulesManager rulesManager) {
    super();
    this.profile = profile;
    this.rulesManager = rulesManager;
  }

  /**
   * If configured so runs the PHPMD tool and analyze the results.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    try {
      getConfiguration(project);
      // If configured so, execute the tool
      if ( !config.isAnalyseOnly()) {
        PhpPmdExecutor executor = new PhpPmdExecutor(config);
        executor.execute();
      }
      // Gets report file
      File report = config.getReportFile();
      if (report == null || !report.exists() || !report.isFile()) {
        LOG.error("Report file can't be found" + (report != null ? " : " + report.getAbsolutePath() : ""));
        LOG.error("Plugin will stop.");
      }
      // If reports can't be found plugin stop without errors
      if (report != null) {
        AbstractViolationsStaxParser parser = getStaxParser(project, context);
        parser.parse(report);
      }
    } catch (XMLStreamException e) {
      LOG.error("PMD report is invalid data will not be imported", e);
      throw new SonarException(e);
    }
  }

  /**
   * Gets the violation parser.
   * 
   * @param project
   *          the analyzed project
   * @param context
   *          the execution context
   * @return the violation parser.
   */
  private AbstractViolationsStaxParser getStaxParser(Project project, SensorContext context) {
    return new PhpPmdViolationsXmlParser(project, context, rulesManager, profile);
  }

  /**
   * Should execute on project.
   * 
   * @param project
   *          the project
   * @return true, if should execute on project
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api .resources.Project)
   */
  public boolean shouldExecuteOnProject(Project project) {
    return getConfiguration(project).isShouldRun() && Php.INSTANCE.equals(project.getLanguage());
  }

  /***
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /**
   * Gets the configuration. If config field is null initialize it, other way only returns it.
   * 
   * @param project
   *          the project
   * @return the configuration
   */
  private PhpPmdConfiguration getConfiguration(Project project) {
    if (config == null) {
      config = new PhpPmdConfiguration(project);
    }
    return config;
  }
}