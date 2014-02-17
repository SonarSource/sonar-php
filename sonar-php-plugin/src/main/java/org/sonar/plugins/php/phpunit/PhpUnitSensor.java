/*
 * SonarQube PHP Plugin
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

import com.thoughtworks.xstream.XStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.plugins.php.api.Php;

import java.io.File;

/**
 * The Class PhpUnitSensor is used by the plugin to collect coverage metrics from PHPUnit report.
 */
public class PhpUnitSensor implements Sensor {


  private static final Logger LOGGER = LoggerFactory.getLogger(PhpUnitSensor.class);
  private final Settings settings;

  private final PhpUnitCoverageResultParser coverageParser;
  private final PhpUnitResultParser parser;
  private final ProjectFileSystem fileSystem;

  public PhpUnitSensor(ProjectFileSystem fileSystem, Settings settings, PhpUnitResultParser parser, PhpUnitCoverageResultParser coverageParser) {
    super();
    this.fileSystem = fileSystem;
    this.settings = settings;
    this.parser = parser;
    this.coverageParser = coverageParser;
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    parseReport(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, false, "tests");
    parseReport(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY, true, "coverage");
  }

  private void parseReport(String reportPathKey, boolean isCoverage, String msg) {
    String reportPath = settings.getString(reportPathKey);

    if (reportPath != null) {
      File xmlFile = fileSystem.resolvePath(reportPath);

      if (xmlFile.exists()) {
        LOGGER.info("Analyzing PHPUnit " + msg + " report: " + reportPath);

        try {
          if (isCoverage) {
            coverageParser.parse(xmlFile);
          } else {
            parser.parse(xmlFile);
          }

        } catch (XStreamException e) {
          throw new SonarException("Report file is invalid, plugin will stop.", e);
        }
      } else {
        LOGGER.info("PHPUnit xml " + msg + "report not found: " + reportPath);
      }
    } else {
      LOGGER.info("No PHPUnit report provided (see '" + reportPathKey + "' property)");
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    return Php.KEY.equals(project.getLanguageKey());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHPUnit Sensor";
  }
}
