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
package org.sonar.plugins.php;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PHPSquidSensorTest {

  private Project project;
  private final ModuleFileSystem fileSystem = mock(ModuleFileSystem.class);
  private PHPSquidSensor sensor;

  @Before
  public void setUp() {
    project = mock(Project.class);
    sensor = spy(new PHPSquidSensor(mock(RulesProfile.class), fileSystem));
  }

  @Test
  public void shouldExecuteOnProject() {
    when(fileSystem.files(any(FileQuery.class))).thenReturn(ImmutableList.<java.io.File>of());
    assertThat(sensor.shouldExecuteOnProject(null), is(false));

    when(fileSystem.files(any(FileQuery.class))).thenReturn(ImmutableList.of(new java.io.File("file.php")));
    assertThat(sensor.shouldExecuteOnProject(null), is(true));
  }

  @Test
  public void analyse() {
    doReturn(new File("file")).when(sensor).getSonarResource(any(java.io.File.class));

    SensorContext context = mock(SensorContext.class);
    when(fileSystem.sourceCharset()).thenReturn(Charsets.UTF_8);
    when(fileSystem.files(any(FileQuery.class))).thenReturn(ImmutableList.of(new java.io.File("src/test/resources/PHPSquidSensor.php")));

    sensor.analyse(project, context);

    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.FILES), Mockito.eq(1.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.LINES), Mockito.eq(44.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.NCLOC), Mockito.eq(23.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.STATEMENTS), Mockito.eq(11.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.CLASSES), Mockito.eq(1.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMPLEXITY), Mockito.eq(7.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMMENT_LINES), Mockito.eq(7.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.FUNCTIONS), Mockito.eq(2.0));
  }
}
