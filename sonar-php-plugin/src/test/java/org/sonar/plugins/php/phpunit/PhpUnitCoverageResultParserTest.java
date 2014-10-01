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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.test.TestUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.api.measures.CoreMetrics.COVERAGE_LINE_HITS_DATA;
import static org.sonar.api.measures.CoreMetrics.LINES_TO_COVER;
import static org.sonar.api.measures.CoreMetrics.UNCOVERED_LINES;

public class PhpUnitCoverageResultParserTest {

  private static final org.sonar.api.resources.File monkeyResource = org.sonar.api.resources.File.create("Monkey.php");

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private PhpUnitCoverageResultParser parser;
  private SensorContext context;
  private Project project;
  private ModuleFileSystem moduleFileSystem;

  @Before
  public void setUp() throws Exception {
    context = mock(SensorContext.class);
    project = mock(Project.class);
    moduleFileSystem = mock(ModuleFileSystem.class);
    mockProjectFileSystem(project, moduleFileSystem);

    parser = new PhpUnitCoverageResultParser(project, context, moduleFileSystem);
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
    verify(context).saveMeasure(monkeyResource, LINES_TO_COVER, 4.0);
    verify(context).saveMeasure(monkeyResource, UNCOVERED_LINES, 2.0);
  }

  /**
   * Should generate coverage metrics.
   */
  @Test
  public void shouldGenerateCoverageMeasures() {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_COVERAGE_REPORT));

    verify(context, atLeastOnce()).saveMeasure(monkeyResource, new Measure(COVERAGE_LINE_HITS_DATA, "34=1;35=1;38=1;40=0;45=1;46=1"));
    verify(context).saveMeasure(monkeyResource, UNCOVERED_LINES, 2.0);
    verifyNoMeasureForFileOutOfSourcesDirs();
  }

  /**
   * Should not generate coverage metrics for files that are not under project sources dirs.
   */
  public void verifyNoMeasureForFileOutOfSourcesDirs() {
    org.sonar.api.resources.File file = new org.sonar.api.resources.File("IndexControllerTest.php");

    verify(context, never()).saveMeasure(eq(file), eq(CoreMetrics.LINES_TO_COVER), anyDouble());
    verify(context, never()).saveMeasure((Resource) eq(null), eq(CoreMetrics.LINES_TO_COVER), anyDouble());
  }

  // https://jira.codehaus.org/browse/SONARPLUGINS-1591
  @Test
  public void shouldNotFailIfNoStatementCount() {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR + "phpunit.coverage-with-no-statements-covered.xml"));
    verify(context, atLeastOnce()).saveMeasure(monkeyResource, CoreMetrics.LINE_COVERAGE, 0.0d);
  }

  // https://jira.codehaus.org/browse/SONARPLUGINS-1675
  @Test
  public void shouldNotFailIfNoLineForFileNode() {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR + "phpunit.coverage-with-filenode-without-line.xml"));
    verify(context, atLeastOnce()).saveMeasure(monkeyResource, CoreMetrics.LINE_COVERAGE, 0.0d);
  }

  @Test
  public void should_save_measure_for_missing_file_in_report() throws Exception {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR + "phpunit.coverage-empty.xml"));
    verify(context).saveMeasure(monkeyResource, CoreMetrics.LINE_COVERAGE, 0.0d);
  }

  private static void mockProjectFileSystem(Project project, ModuleFileSystem moduleFileSystem) {
    ProjectFileSystem fs = mock(ProjectFileSystem.class);

    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBasedir()).thenReturn(new File("C:/projets/PHP/Monkey/sources/main"));

    File f1 = new File("C:/projets/PHP/Monkey/sources/main/Monkey2.php");
    File f2 = new File("C:/projets/PHP/Monkey/sources/main/Monkey.php");
    File f3 = new File("C:/projets/PHP/Monkey/sources/main/Banana1.php");
    File f4 = new File("C:/projets/PHP/Monkey/sources/test/Banana.php");
    File f5 = new File("C:/projets/PHP/Monkey/sources/main/Money.inc");
    File f6 = new File("C:/projets/PHP/Monkey/sources/test/application/default/controllers/IndexControllerTest.php");

    List<File> sourceFiles = Arrays.asList(f1, f2, f3, f5);
    when(fs.mainFiles(Php.KEY)).thenReturn(InputFileUtils.create(new File("C:/projets/PHP/Money/Sources/main"), sourceFiles));
    when(moduleFileSystem.files(any(FileQuery.class))).thenReturn(sourceFiles);

    List<File> testFiles = Arrays.asList(f4, f6);
    when(fs.testFiles(Php.KEY)).thenReturn(InputFileUtils.create(new File("C:/projets/PHP/Money/Sources/test"), testFiles));

  }

}
