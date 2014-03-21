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
package org.sonar.plugins.php.core;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.php.api.Php;
import org.sonar.squidbridge.measures.Metric;
import org.sonar.squidbridge.text.Source;
import org.sonar.test.TestUtils;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NoSonarAndCommentedOutLocSensorTest {

  private ModuleFileSystem fs;
  private Project project;
  private NoSonarFilter noSonarFilter;
  private NoSonarAndCommentedOutLocSensor sensor;
  private SensorContext context;

  @Before
  public void setUp() throws Exception {
    fs = mock(ModuleFileSystem.class);
    project = mock(Project.class);

    noSonarFilter = new NoSonarFilter();
    context = mock(SensorContext.class);
    sensor = spy(new NoSonarAndCommentedOutLocSensor(fs, noSonarFilter));
  }

  @Test
  public void testAnalyse() {
    File phpFile = TestUtils.getResource("/Mail.php");
    when(fs.sourceDirs()).thenReturn(ImmutableList.of(phpFile.getParentFile()));
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(TestUtils.getResource("/Mail.php")));

    org.sonar.api.resources.File sonarFile = new org.sonar.api.resources.File("Mail.php");
    doReturn(sonarFile).when(sensor).getSonarResource(any(Project.class), any(File.class));

    sensor.analyse(project, context);
    // Mail.php contains 9 commented oud code lines.
    verify(context).saveMeasure(sonarFile, CoreMetrics.COMMENTED_OUT_CODE_LINES, 9d);
  }

  @Test
  public void testShouldNotRunOnJavaProject() {
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.<java.io.File>of());
    assertThat(sensor.shouldExecuteOnProject(null)).isFalse();

    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(new java.io.File("file.php")));
    assertThat(sensor.shouldExecuteOnProject(null)).isTrue();
  }

  @Test
  public void testAnalyseEmptySourceFiles() {
    File fakeFile = new File("fake.php");
    ModuleFileSystem localFs = mock(ModuleFileSystem.class);
    when(fs.sourceDirs()).thenReturn(ImmutableList.of(new File("fake/directory/")));
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(fakeFile, new File("fake")));

    NoSonarAndCommentedOutLocSensor localSensor = new NoSonarAndCommentedOutLocSensor(localFs, noSonarFilter);
    localSensor.analyse(project, context);
    verify(context, never()).saveMeasure(any(Resource.class), any(org.sonar.api.measures.Metric.class), any(Double.class));

  }

  @Test
  public void testAnalyseSourceCode() {
    File file = new File(this.getClass().getResource("/Mail.php").getPath());
    Source source = NoSonarAndCommentedOutLocSensor.analyseSourceCode(file);
    assertEquals(1, source.getNoSonarTagLines().size());
    assertEquals(17, (int) source.getNoSonarTagLines().iterator().next());

    assertEquals(9, source.getMeasure(Metric.COMMENTED_OUT_CODE_LINES));
  }

  @Test
  public void testAnalyseSourceCodeWithRegions() {
    File file = new File(this.getClass().getResource("/Math2.php").getPath());
    Source source = NoSonarAndCommentedOutLocSensor.analyseSourceCode(file);
    assertEquals(1, source.getNoSonarTagLines().size());
    assertEquals(126, (int) source.getNoSonarTagLines().iterator().next());

    assertEquals(3, source.getMeasure(Metric.COMMENTED_OUT_CODE_LINES));
  }

  @Test
  public void testAnalyseSourceCodeWithNoNoSonar() {
    File file = new File(this.getClass().getResource("/Math3.php").getPath());
    Source source = NoSonarAndCommentedOutLocSensor.analyseSourceCode(file);
    assertEquals(0, source.getNoSonarTagLines().size());
    assertEquals(5, source.getMeasure(Metric.COMMENTED_OUT_CODE_LINES));
  }

  // TEST for SONARPLUGINS-662
  @Test
  public void testAnalyseSourceCodeWithMultiLineString() {
    File file = new File(this.getClass().getResource("/Math4.php").getPath());
    Source source = NoSonarAndCommentedOutLocSensor.analyseSourceCode(file);
    assertEquals(1, source.getNoSonarTagLines().size());
    assertEquals(91, (int) source.getNoSonarTagLines().iterator().next());

    assertEquals(5, source.getMeasure(Metric.COMMENTED_OUT_CODE_LINES));
  }
}
