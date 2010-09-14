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

package org.sonar.plugins.php.codesniffer.sensor;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractViolationsStaxParser;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RulesManager;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.php.codesniffer.configuration.PhpCodesnifferConfiguration;
import org.sonar.plugins.php.codesniffer.executor.PhpCodesnifferExecutor;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.executor.PhpPluginExecutionException;

/**
 * The Class PhpCodesnifferPluginSensor.
 */
public class PhpCodesnifferSensor implements Sensor {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpCodesnifferSensor.class);

  /** The rules manager. */
  private RulesManager rulesManager;

  /** The plugin configuration. */
  private PhpCodesnifferConfiguration config;

  /**
   * Default constructor used for tests only.
   */
  public PhpCodesnifferSensor() {
    super();
  }

  /**
   * Instantiates a new php codesniffer sensor.
   * 
   * @param rulesManager
   *          the rules manager
   */
  public PhpCodesnifferSensor(RulesManager rulesManager) {
    super();
    this.rulesManager = rulesManager;
  }

  /**
   * Launches the external tool (if configured so) and analyze result file.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   * 
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    try {
      // If configured so, execute the tool
      if ( !getConfiguration(project).isAnalyseOnly()) {
        PhpCodesnifferExecutor executor = new PhpCodesnifferExecutor(config);
        executor.execute();
      }
      AbstractViolationsStaxParser parser = getStaxParser(project, context);
      File report = getConfiguration(project).getReportFile();
      LOG.info("Analysing project with file:" + report.getAbsolutePath());
      parser.parse(report);

    } catch (XMLStreamException e) {
      LOG.error("Error occured while reading report file", e);
      throw new XmlParserException(e);
    } catch (PhpPluginExecutionException e) {
      LOG.error("Error occured while launching Php CodeSniffer", e);
    }
  }

  /**
   * Gets the violation stax result parser.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   * 
   * @return the violation stax result parser.
   */
  private AbstractViolationsStaxParser getStaxParser(Project project, SensorContext context) {
    return new PhpCheckStyleViolationsXmlParser(project, context, rulesManager);
  }

  /**
   * Gets the configuration.
   * 
   * @param project
   *          the project
   * 
   * @return the configuration
   */
  private PhpCodesnifferConfiguration getConfiguration(Project project) {
    if (config == null) {
      config = new PhpCodesnifferConfiguration(project);
    }
    return config;
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
    return getConfiguration(project).isShouldRun() && Php.INSTANCE.equals(project.getLanguage());
  }

  /**
   * To string.
   * 
   * @return the string
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}