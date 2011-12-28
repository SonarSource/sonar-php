/*
 * Sonar PHP Plugin
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

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.api.measures.CoreMetrics.COVERAGE_LINE_HITS_DATA;
import static org.sonar.api.measures.CoreMetrics.UNCOVERED_LINES;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.plugins.php.api.Php;

public class PhpUnitCoverageResultParserTest {

  /** The context. */
  private SensorContext context;

  /** The config. */
  private PhpUnitConfiguration config;

  /** The project. */
  private Project project;

  /**
   * Inits the.
   */
  private void init(String reportPath) {
    config = mock(PhpUnitConfiguration.class);
    project = mock(Project.class);
    context = mock(SensorContext.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:/projets/PHP/Monkey/sources/main")));

    File f1 = new File("C:/projets/PHP/Monkey/Sources/main/Monkey2.php");
    File f2 = new File("C:/projets/PHP/Monkey/Sources/main/Monkey.php");
    File f3 = new File("C:/projets/PHP/Monkey/Sources/main/Banana1.php");
    File f4 = new File("C:/projets/PHP/Monkey/Sources/test/Banana.php");
    File f5 = new File("C:/projets/PHP/Monkey/Sources/main/Money.inc");
    File f6 = new File("C:/projets/PHP/Monkey/sources/test/application/default/controllers/IndexControllerTest.php");

    List<File> sourceFiles = Arrays.asList(f1, f2, f3, f5);
    when(fs.mainFiles(Php.KEY)).thenReturn(InputFileUtils.create(new File("C:/projets/PHP/Money/Sources/main"), sourceFiles));
    List<File> testFiles = Arrays.asList(f4, f6);
    when(fs.testFiles(Php.KEY)).thenReturn(InputFileUtils.create(new File("C:/projets/PHP/Money/Sources/test"), testFiles));

    File reportFile = new File(getClass().getResource(reportPath).getFile());
    when(config.getReportFile()).thenReturn(reportFile);
    Configuration configuration = mock(Configuration.class);
    when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);

    PhpUnitCoverageResultParser parser = new PhpUnitCoverageResultParser(project, context);
    parser.parse(config.getReportFile());
  }

  /**
   * Should not throw an exception when report not found.
   */
  @Test
  public void shouldNotThrowAnExceptionWhenReportNotFound() {
    config = mock(PhpUnitConfiguration.class);
    project = mock(Project.class);
    context = mock(SensorContext.class);
    MavenProject mavenProject = mock(MavenProject.class);
    when(mavenProject.getPackaging()).thenReturn("maven-plugin");
    when(config.getReportFile()).thenReturn(new File("path/to/nowhere"));
    PhpUnitCoverageResultParser parser = new PhpUnitCoverageResultParser(project, context);
    parser.parse(null);
  }

  /**
   * Should parse even when there's a package node.
   */
  @Test
  public void shouldParseEvenWithPackageNode() {
    init("/org/sonar/plugins/php/phpunit/sensor/phpunit.coverage-with-package.xml");
    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), UNCOVERED_LINES, 2.0);
  }

  /**
   * Should generate coverage metrics.
   */
  @Test
  public void shouldGenerateCoverageMeasures() {
    init("/org/sonar/plugins/php/phpunit/sensor/phpunit.coverage.xml");
    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), UNCOVERED_LINES, 2.0);
  }

  /**
   * Should not generate coverage metrics for files that are not under project sources dirs.
   */
  @Test
  public void shouldNotGenerateCoverageMeasures() {
    init("/org/sonar/plugins/php/phpunit/sensor/phpunit.coverage.xml");
    org.sonar.api.resources.File file = new org.sonar.api.resources.File("IndexControllerTest.php");
    verify(context, never()).saveMeasure(eq(file), eq(CoreMetrics.LINES_TO_COVER), anyDouble());
    verify(context, never()).saveMeasure((Resource<?>) eq(null), eq(CoreMetrics.LINES_TO_COVER), anyDouble());
  }

  /**
   * Should generate line hits metrics.
   */
  @Test
  public void shouldGenerateLineHitsMeasures() {
    init("/org/sonar/plugins/php/phpunit/sensor/phpunit.coverage.xml");
    Measure monkeyCoverage = new Measure(COVERAGE_LINE_HITS_DATA, "34=1;35=1;38=1;40=0;45=1;46=1");
    verify(context, atLeastOnce()).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), monkeyCoverage);
  }

  // https://jira.codehaus.org/browse/SONARPLUGINS-1591
  @Test
  public void shouldNotFailIfNoStatementCount() {
    init("/org/sonar/plugins/php/phpunit/sensor/phpunit.coverage-with-no-statements-covered.xml");
    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), CoreMetrics.LINE_COVERAGE, 0.0d);
  }

}
