/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.test.TestUtils;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.api.measures.CoreMetrics.COVERAGE_LINE_HITS_DATA;
import static org.sonar.api.measures.CoreMetrics.LINES_TO_COVER;
import static org.sonar.api.measures.CoreMetrics.LINE_COVERAGE;
import static org.sonar.api.measures.CoreMetrics.NCLOC;
import static org.sonar.api.measures.CoreMetrics.UNCOVERED_LINES;

public class PhpUnitCoverageResultParserTest {

  private static final String BASE_DIR = "/org/sonar/plugins/php/phpunit/sensor/src/";
  private static final String MONKEY_FILE_NAME = "Monkey.php";

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private PhpUnitCoverageResultParser parser;
  private SensorContext context;
  private DefaultFileSystem fileSystem;

  @Before
  public void setUp() throws Exception {
    context = mock(SensorContext.class);
    when(context.getResource(any(Resource.class))).thenReturn(org.sonar.api.resources.File.create(MONKEY_FILE_NAME));

    fileSystem = new DefaultFileSystem();
    addFiles(fileSystem);

    parser = new PhpUnitCoverageResultParser(context, fileSystem);
  }

  @Test
  public void shouldThrowAnExceptionWhenReportNotFound() {
    thrown.expect(SonarException.class);
    thrown.expectMessage("Can't read phpUnit report:");

    parser.parse(new File("notfound.txt"));
  }

  /**
   * Should parse even when there's a package node.
   */
  @Test
  public void shouldParseEvenWithPackageNode() {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR + "phpunit.coverage-with-package.xml"));

    verify(context).saveMeasure(any(Resource.class), eq(LINES_TO_COVER), eq(4.0));
    verify(context).saveMeasure(any(Resource.class), eq(UNCOVERED_LINES), eq(2.0));
  }

  /**
   * Should generate coverage metrics.
   */
  @Test
  public void shouldGenerateCoverageMeasures() {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_COVERAGE_REPORT));

    verify(context, atLeastOnce()).saveMeasure(any(Resource.class), eq(new Measure(COVERAGE_LINE_HITS_DATA, "34=1;35=1;38=1;40=0;45=1;46=1")));
    verify(context).saveMeasure(any(Resource.class), eq(UNCOVERED_LINES), eq(2.0));
  }

  /**
   * SONARPLUGINS-1591
   */
  @Test
  public void shouldNotFailIfNoStatementCount() {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR + "phpunit.coverage-with-no-statements-covered.xml"));
    verify(context, atLeastOnce()).saveMeasure(any(Resource.class), eq(CoreMetrics.LINE_COVERAGE), eq(0.0d));
  }

  /**
   * SONARPLUGINS-1675
   */
  @Test
  public void shouldNotFailIfNoLineForFileNode() {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR + "phpunit.coverage-with-filenode-without-line.xml"));
    verify(context, atLeastOnce()).saveMeasure(any(Resource.class), eq(CoreMetrics.LINE_COVERAGE), eq(0.0d));
  }

  @Test
  public void should_save_measure_for_missing_file_in_report() throws Exception {
    when(context.getResource(any(Resource.class))).thenReturn(org.sonar.api.resources.File.create("Banana.php"));
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR + "phpunit.coverage-empty.xml"));

    verify(context, atLeastOnce()).saveMeasure(any(Resource.class), eq(CoreMetrics.LINE_COVERAGE), eq(0.0d));
  }

  private static void addFiles(DefaultFileSystem fs) {
    File baseDir = TestUtils.getResource(BASE_DIR);

    InputFile monkeyFile = new DefaultInputFile("Monkey.php").setAbsolutePath((new File(baseDir, "Monkey.php").getAbsolutePath())).setType(InputFile.Type.MAIN).setLanguage(Php.KEY);
    InputFile bananaFile = new DefaultInputFile("Banana.php").setAbsolutePath((new File(baseDir, "Banana.php").getAbsolutePath())).setType(InputFile.Type.MAIN).setLanguage(Php.KEY);

    fs.setBaseDir(baseDir);

    fs.add(monkeyFile);
    fs.add(bananaFile);
  }

}
