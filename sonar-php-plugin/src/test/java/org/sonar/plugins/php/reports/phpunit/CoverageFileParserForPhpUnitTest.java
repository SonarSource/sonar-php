/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.reports.phpunit.xml.FileNode;
import org.sonarsource.analyzer.commons.xml.ParseException;

import static org.assertj.core.api.Assertions.assertThat;

class CoverageFileParserForPhpUnitTest {

  private static final String NO_PROJECT_FILE = "phpunit.coverage-no-project.xml";
  private static final String INVALID_COVERAGE_FILE = "phpunit.coverage-invalid.xml";
  private static final String SRC_TEST_RESOURCES = "src/test/resources/";
  private static final String BASE_DIR = "/reports/phpunit/";

  private CoverageFileParserForPhpUnit parser;

  @BeforeEach
  public void setUp() {
    parser = new CoverageFileParserForPhpUnit();
  }

  @Test
  void shouldParseWhenThereIsNoProject() throws IOException {
    CountConsumer counter = new CountConsumer();
    parser.parse(reportFile(NO_PROJECT_FILE), counter);
    assertThat(counter.count).isZero();
  }

  @Test
  void shouldFailWhenXmlRootNodeIsNotCovered() {
    CountConsumer counter = new CountConsumer();
    File reportFile = reportFile(INVALID_COVERAGE_FILE);
    Assertions.assertThatExceptionOfType(ParseException.class)
      .isThrownBy(() -> parser.parse(reportFile, counter))
      .withMessage("javax.xml.stream.XMLStreamException: Report should start with <coverage>");
  }

  private static File reportFile(String file) {
    return new File(SRC_TEST_RESOURCES + BASE_DIR + file);
  }

  static class CountConsumer implements Consumer<FileNode> {

    protected int count = 0;

    @Override
    public void accept(FileNode fileNode) {
      count++;
    }

  }
}
