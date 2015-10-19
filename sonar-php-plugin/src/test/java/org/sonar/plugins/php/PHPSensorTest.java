/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.php.api.Php;
import org.sonar.test.TestUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PHPSensorTest {


  private final DefaultFileSystem fileSystem = new DefaultFileSystem();
  private PHPSensor sensor;

  @Before
  public void setUp() {
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);

    CheckFactory checkFactory = new CheckFactory(mock(ActiveRules.class));
    sensor = new PHPSensor(mock(ResourcePerspectives.class), fileSystem, fileLinesContextFactory, checkFactory, new NoSonarFilter());
  }

  @Test
  public void shouldExecuteOnProject() {
    DefaultFileSystem localFS = new DefaultFileSystem();
    PHPSensor localSensor = new PHPSensor(mock(ResourcePerspectives.class), localFS, null, new CheckFactory(mock(ActiveRules.class)), new NoSonarFilter());

    // empty file system
    assertThat(localSensor.shouldExecuteOnProject(null), is(false));

    localFS.add(new DefaultInputFile("file.php").setType(InputFile.Type.MAIN).setLanguage(Php.KEY));
    assertThat(localSensor.shouldExecuteOnProject(null), is(true));
  }

  @Test
  public void analyse() {
    SensorContext context = mock(SensorContext.class);
    fileSystem.add(new DefaultInputFile("PHPSquidSensor.php")
      .setAbsolutePath(TestUtils.getResource("PHPSquidSensor.php").getAbsolutePath())
      .setType(InputFile.Type.MAIN)
      .setLanguage(Php.KEY));

    Resource resource = mock(Resource.class);

    when(resource.getEffectiveKey()).thenReturn("someKey");
    when(context.getResource(any(InputFile.class))).thenReturn(resource);
    sensor.analyse(new Project(""), context);

    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.FILES), Mockito.eq(1.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.LINES), Mockito.eq(55.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.NCLOC), Mockito.eq(32.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.STATEMENTS), Mockito.eq(16.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.CLASSES), Mockito.eq(1.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMPLEXITY_IN_CLASSES), Mockito.eq(7.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMPLEXITY_IN_FUNCTIONS), Mockito.eq(10.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMMENT_LINES), Mockito.eq(7.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.FUNCTIONS), Mockito.eq(3.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMPLEXITY), Mockito.eq(12.0));

  }

}
