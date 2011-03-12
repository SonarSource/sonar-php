/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.core.Php.PHP;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.core.PhpFile;
import org.sonar.plugins.php.core.PhpPackage;
import org.sonar.plugins.php.core.PhpPlugin;

public class PhpUnitCoverageResultParserTest {

  /** The context. */
  private SensorContext context;

  /** The config. */
  private PhpUnitConfiguration config;

  /** The project. */
  private Project project;

  /** The metric. */
  private Metric metric;

  /**
   * Inits the.
   */
  private void init() {
    try {
      config = mock(PhpUnitConfiguration.class);
      project = mock(Project.class);
      context = mock(SensorContext.class);
      MavenProject mavenProject = mock(MavenProject.class);
      ProjectFileSystem fs = mock(ProjectFileSystem.class);

      when(project.getPom()).thenReturn(mavenProject);
      when(project.getFileSystem()).thenReturn(fs);
      when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:/projets/PHP/Monkey/sources/main")));
      when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:/projets/PHP/Monkey/sources/test")));

      File f1 = new File("C:/projets/PHP/Money/Sources/main/Monkey2.php");
      File f2 = new File("C:/projets/PHP/Monkey/sources/main/Monkey.php");
      File f3 = new File("C:/projets/PHP/Monkey/sources/main/Banana1.php");
      File f4 = new File("C:/projets/PHP/Monkey/sources/test/Banana.php");
      File f5 = new File("C:/projets/PHP/Monkey/sources/main/Money.inc");
      when(fs.getSourceFiles(PHP)).thenReturn(Arrays.asList(f1, f2, f3, f4, f5));

      when(mavenProject.getPackaging()).thenReturn("maven-plugin");
      File reportFile = new File(getClass().getResource("/org/sonar/plugins/php/phpunit/sensor/phpunit.coverage.xml").getFile());
      when(config.getReportFile()).thenReturn(reportFile);
      Configuration configuration = mock(Configuration.class);
      when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);

      PhpUnitCoverageResultParser parser = new PhpUnitCoverageResultParser(project, context);
      parser.parse(config.getReportFile());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
    when(project.getPom()).thenReturn(mavenProject);
    when(mavenProject.getPackaging()).thenReturn("maven-plugin");
    when(config.getReportFile()).thenReturn(new File("path/to/nowhere"));
    PhpUnitCoverageResultParser parser = new PhpUnitCoverageResultParser(project, context);
    parser.parse(null);
  }

  /**
   * Should save zero value when report not found.
   */
  @Test
  public void shouldSaveZeroValueWhenReportNotFound() {
    metric = CoreMetrics.COVERAGE;
    config = mock(PhpUnitConfiguration.class);
    project = mock(Project.class);
    context = mock(SensorContext.class);
    MavenProject mavenProject = mock(MavenProject.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(mavenProject.getPackaging()).thenReturn("maven-plugin");
    PhpUnitCoverageResultParser parser = new PhpUnitCoverageResultParser(project, context);
    parser.parse(null);
    verify(context).saveMeasure(metric, 0.0);
  }

  /**
   * Should generate coverage metrics.
   */
  @Test()
  public void shouldGenerateCoverageMeasures() {
    metric = CoreMetrics.LINES_TO_COVER;

    init();
    PhpFile phpFile = new PhpFile("Banana.php", true);
    verify(context).saveMeasure(phpFile, metric, 4.0);

    metric = CoreMetrics.UNCOVERED_LINES;
    phpFile = new PhpFile("Monkey.php");
    verify(context).saveMeasure(phpFile, metric, 2.0);

    // Sonar core engine will consolidate coverage on package level using coverage on files. So we have to ensure that saveMeasure on
    // PhpPackage is never called.
    PhpPackage phpPackage = new PhpPackage("Animals");
    verify(context, never()).saveMeasure(phpPackage, metric, 4.0);

  }

  /**
   * Should not generate coverage metrics for files that are not under project sources dirs.
   */
  @Test()
  public void shouldNotGenerateCoverageMeasures() {
    metric = CoreMetrics.COVERAGE;
    init();
    verify(context, never()).saveMeasure(new PhpFile("IndexControllerTest.php", true), metric, 1.0);
  }

  /**
   * Should generate line hits metrics.
   */
  @Test()
  public void shouldGenerateLineHitsMeasures() {
    metric = CoreMetrics.COVERAGE_LINE_HITS_DATA;
    init();
    verify(context, atLeastOnce()).saveMeasure(new PhpFile("Monkey.php", true), new Measure(metric, "34=1;35=1;38=1;40=0;45=1;46=1"));
    verify(context, atLeastOnce()).saveMeasure(new PhpFile("Banana.php"), new Measure(metric, "34=0;35=0;38=0;40=0;45=1;46=1"));
  }

  /**
   * Should generate project converage metric.
   */
  @Test()
  public void shouldGenerateProjectCoverageMeasures() {
    metric = CoreMetrics.LINES_TO_COVER;
    init();
    verify(context, times(1)).saveMeasure(metric, 100.0);
  }

}
