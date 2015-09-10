/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
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

  private final PhpUnitOverallCoverageResultParser overallCoverageParser;
  private final PhpUnitItCoverageResultParser itCoverageParser;
  private final PhpUnitCoverageResultParser coverageParser;
  private final PhpUnitResultParser parser;
  private final FileSystem fileSystem;
  private final FilePredicates filePredicates;

  public PhpUnitSensor(FileSystem fileSystem, Settings settings, PhpUnitResultParser parser,
                       PhpUnitCoverageResultParser coverageParser,
                       PhpUnitItCoverageResultParser itCoverageParser,
                       PhpUnitOverallCoverageResultParser overallCoverageParser) {

    this.fileSystem = fileSystem;
    this.filePredicates = fileSystem.predicates();
    this.settings = settings;
    this.parser = parser;
    this.coverageParser = coverageParser;
    this.itCoverageParser = itCoverageParser;
    this.overallCoverageParser = overallCoverageParser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    parseReport(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, parser, "test");
    parseReport(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY, coverageParser, "unit test coverage");
    parseReport(PhpPlugin.PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY, itCoverageParser, "integration test coverage");
    parseReport(PhpPlugin.PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY, overallCoverageParser, "overall coverage");
  }


  private void parseReport(String reportPathKey, PhpUnitParser parser, String msg) {
    String reportPath = settings.getString(reportPathKey);

    if (reportPath != null) {
      File xmlFile = getIOFile(reportPath);

      if (xmlFile.exists()) {
        LOGGER.info("Analyzing PHPUnit " + msg + " report: " + reportPath + " with " + parser.toString());

        try {
          parser.parse(xmlFile);
        } catch (XStreamException e) {
          throw new SonarException("Report file is invalid, plugin will stop.", e);
        }
      } else {
        LOGGER.info("PHPUnit xml " + msg + " report not found: " + reportPath);
      }
    } else {
      LOGGER.info("No PHPUnit " + msg + " report provided (see '" + reportPathKey + "' property)");
    }
  }

  /**
   * Returns a java.io.File for the given path.
   * If path is not absolute, returns a File with module base directory as parent path.
   */
  private File getIOFile(String path) {
    File file = new File(path);
    if (!file.isAbsolute()) {
      file = new File(fileSystem.baseDir(), path);
    }

    return file;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.hasFiles(filePredicates.and(filePredicates.hasLanguage(Php.KEY), filePredicates.hasType(InputFile.Type.MAIN)));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHPUnit Sensor";
  }
}
