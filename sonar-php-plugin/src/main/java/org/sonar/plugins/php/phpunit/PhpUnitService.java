/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.php.phpunit;

import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.plugins.php.PhpPlugin;

/**
 * Used by the plugin to collect coverage metrics from PHPUnit report.
 */
public class PhpUnitService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhpUnitService.class);

  private final Settings settings;

  private final PhpUnitOverallCoverageResultParser overallCoverageParser;
  private final PhpUnitItCoverageResultParser itCoverageParser;
  private final PhpUnitCoverageResultParser coverageParser;
  private final PhpUnitResultParser parser;

  private final FileSystem fileSystem;

  public PhpUnitService(FileSystem fileSystem, Settings settings, PhpUnitResultParser parser,
                       PhpUnitCoverageResultParser coverageParser,
                       PhpUnitItCoverageResultParser itCoverageParser,
                       PhpUnitOverallCoverageResultParser overallCoverageParser) {

    this.fileSystem = fileSystem;
    this.settings = settings;
    this.parser = parser;
    this.coverageParser = coverageParser;
    this.itCoverageParser = itCoverageParser;
    this.overallCoverageParser = overallCoverageParser;
  }

  public void execute(SensorContext context, Map<File, Integer> numberOfLinesOfCode) {
    parseReport(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, parser, "test", context, numberOfLinesOfCode);
    parseReport(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY, coverageParser, "unit test coverage", context, numberOfLinesOfCode);
    parseReport(PhpPlugin.PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY, itCoverageParser, "integration test coverage", context, numberOfLinesOfCode);
    parseReport(PhpPlugin.PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY, overallCoverageParser, "overall coverage", context, numberOfLinesOfCode);
  }

  private void parseReport(String reportPathKey, PhpUnitParser parser, String msg, SensorContext context, Map<File, Integer> numberOfLinesOfCode) {
    String reportPath = settings.getString(reportPathKey);

    if (reportPath != null) {
      File xmlFile = getIOFile(reportPath);

      if (xmlFile.exists()) {
        LOGGER.info("Analyzing PHPUnit " + msg + " report: " + reportPath + " with " + parser.toString());

        try {
          parser.parse(xmlFile, context, numberOfLinesOfCode);
        } catch (XStreamException e) {
          throw new IllegalStateException("Report file is invalid, plugin will stop.", e);
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

}
