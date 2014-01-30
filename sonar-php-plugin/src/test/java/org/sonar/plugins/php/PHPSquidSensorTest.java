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
package org.sonar.plugins.php;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PHPSquidSensorTest {

  private Project project;
  private PHPSquidSensor sensor;

  @Before
  public void setUp() {
    project = MockUtils.createMockProjectBis();
    sensor = new PHPSquidSensor();
  }

  @Test
  public void shouldExecuteOnProject() {
    Project javaProject = mock(Project.class);
    when(javaProject.getLanguageKey()).thenReturn("java");
    assertThat(sensor.shouldExecuteOnProject(javaProject), is(false));

    assertThat(sensor.shouldExecuteOnProject(project), is(true));
  }

  @Test
  public void analyse() {
    SensorContext context = mock(SensorContext.class);

    sensor.analyse(project, context);

    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.FILES), Mockito.eq(1.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.LINES), Mockito.eq(44.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.NCLOC), Mockito.eq(24.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.STATEMENTS), Mockito.eq(11.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.CLASSES), Mockito.eq(1.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMPLEXITY), Mockito.eq(7.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMMENT_LINES), Mockito.eq(7.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.FUNCTIONS), Mockito.eq(2.0));
  }
}
