/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.plugins.php.reports.phpunit.xml.FileNode;
import org.sonarsource.analyzer.commons.xml.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CoverageFileParserForPhpUnitTest {
  final static String NO_PROJECT_FILE = "phpunit.coverage-no-project.xml";

  final static String INVALID_COVERAGE_FILE="phpunit.coverage-invalid.xml";

  private static final String SRC_TEST_RESOURCES = "src/test/resources/";

  private static final String BASE_DIR = "/reports/phpunit/";

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  @Test
  public void should_parse_when_there_is_no_project() {
    CoverageFileParserForPhpUnit parser = new CoverageFileParserForPhpUnit();
    try {
      CountConsumer counter = new CountConsumer();
      parser.consumeAllFileNodes(getReportFile(NO_PROJECT_FILE), counter);
      assertThat(counter.getCount()).isZero();
    } catch(IOException ex) {
      fail("Not suppose to throw an Exception.");
    }
  }

  @Test(expected = ParseException.class)
  public void should_fail_when_xml_root_node_is_not_coverage() {
    CoverageFileParserForPhpUnit parser = new CoverageFileParserForPhpUnit();
    try {
      CountConsumer counter = new CountConsumer();
      parser.consumeAllFileNodes(getReportFile(INVALID_COVERAGE_FILE), counter);
      fail("should not parse this file");
    } catch(IOException ex) {
      fail("Not suppose to throw an Exception.");
    }
  }



  private static File getReportFile(String file) {
    return new File(SRC_TEST_RESOURCES + BASE_DIR + file);
  }

  static class CountConsumer implements Consumer<FileNode> {
    int count = 0;
    @Override
    public void accept(FileNode fileNode) {
        count++;
    }

    public int getCount() {
      return count;
    }
  }
}
