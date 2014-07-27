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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.util.HashMap;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
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
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(Resource.class))).thenReturn(fileLinesContext);

    ProjectFileSystem pfs = mock(ProjectFileSystem.class);
    when(pfs.getSourceDirs()).thenReturn(ImmutableList.of(
      new java.io.File("src/test/resources/")
    ));

    project = mock(Project.class);
    when(project.getFileSystem()).thenReturn(pfs);

    sensor = spy(new PHPSquidSensor(mock(RulesProfile.class), mock(ResourcePerspectives.class), fileSystem, fileLinesContextFactory, mock(Settings.class)));
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
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.LINES), Mockito.eq(51.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.NCLOC), Mockito.eq(29.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.STATEMENTS), Mockito.eq(14.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.CLASSES), Mockito.eq(1.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMPLEXITY), Mockito.eq(10.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMPLEXITY_IN_CLASSES), Mockito.eq(7.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.COMMENT_LINES), Mockito.eq(7.0));
    verify(context).saveMeasure(Mockito.any(File.class), Mockito.eq(CoreMetrics.FUNCTIONS), Mockito.eq(3.0));
  }

  @Test
  public void dependency() {
    doReturn(new File("file")).when(sensor).getSonarResource(any(java.io.File.class));

    SensorContext context = mock(SensorContext.class);
    when(context.getResource(Mockito.any(Resource.class))).thenAnswer(new Answer<Resource>() {
      @Override
      public Resource answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        return (Resource) args[0];
      }
    });
    when(fileSystem.sourceCharset()).thenReturn(Charsets.UTF_8);
    when(fileSystem.files(any(FileQuery.class))).thenReturn(ImmutableList.of(
      new java.io.File("src/test/resources/dependencies/Vendor/Package/UnitTest.php"),
      new java.io.File("src/test/resources/dependencies/Vendor/Package/PackageInterface.php"),
      new java.io.File("src/test/resources/dependencies/Vendor/Errors/RuntimeError.php"),
      new java.io.File("src/test/resources/dependencies/Vendor/Errors/IAlias.php"),
      new java.io.File("src/test/resources/dependencies/Vendor/Common/UnitTest.php"),
      new java.io.File("src/test/resources/dependencies/RuntimeError.php"),
      new java.io.File("src/test/resources/dependencies/IRoot.php")
    ));

    sensor.analyse(project, context);

    HashMap<String, String> expectedMap = new HashMap<String, String>();
    expectedMap.put(null, "[" +
      "{\"i\":null,\"n\":\"dependencies/Vendor/Package\",\"q\":\"DIR\",\"v\":[{},{},{},{}]}," +
      "{\"i\":null,\"n\":\"dependencies/Vendor/Errors\",\"q\":\"DIR\",\"v\":[{\"i\":null,\"w\":2},{},{},{}]}," +
      "{\"i\":null,\"n\":\"dependencies\",\"q\":\"DIR\",\"v\":[{\"i\":null,\"w\":1},{\"i\":null,\"w\":2},{},{}]}," +
      "{\"i\":null,\"n\":\"dependencies/Vendor/Common\",\"q\":\"DIR\",\"v\":[{\"i\":null,\"w\":1},{},{},{}]}" +
      "]");
    expectedMap.put("dependencies", "[" +
      "{\"i\":null,\"n\":\"IRoot.php\",\"q\":\"FIL\",\"v\":[{},{}]}," +
      "{\"i\":null,\"n\":\"RuntimeError.php\",\"q\":\"FIL\",\"v\":[{},{}]}]");
    expectedMap.put("dependencies/Vendor/Common", "[" +
      "{\"i\":null,\"n\":\"UnitTest.php\",\"q\":\"FIL\",\"v\":[{}]}]");
    expectedMap.put("dependencies/Vendor/Package", "[" +
      "{\"i\":null,\"n\":\"UnitTest.php\",\"q\":\"FIL\",\"v\":[{},{}]}," +
      "{\"i\":null,\"n\":\"PackageInterface.php\",\"q\":\"FIL\",\"v\":[{\"i\":null,\"w\":1},{}]}]");
    expectedMap.put("dependencies/Vendor/Errors", "[" +
      "{\"i\":null,\"n\":\"IAlias.php\",\"q\":\"FIL\",\"v\":[{},{}]}," +
      "{\"i\":null,\"n\":\"RuntimeError.php\",\"q\":\"FIL\",\"v\":[{},{}]}]");

    ArgumentCaptor<Resource> resourceArgumentCaptor = ArgumentCaptor.forClass(Resource.class);
    ArgumentCaptor<Measure> measureArgumentCaptor = ArgumentCaptor.forClass(Measure.class);
    verify(context, Mockito.atLeastOnce()).saveMeasure(resourceArgumentCaptor.capture(), measureArgumentCaptor.capture());

    Iterator<Resource> resourceIterator = resourceArgumentCaptor.getAllValues().iterator();
    Iterator<Measure> measureIterator = measureArgumentCaptor.getAllValues().iterator();
    while (resourceIterator.hasNext() && measureIterator.hasNext()) {
      Resource resource = resourceIterator.next();
      Measure measure = measureIterator.next();
      if (measure.getMetric().equals(CoreMetrics.DEPENDENCY_MATRIX)) {
        String key = resource.getKey();
        String message = key == null ? "project" : "directory: " + key;
        assertEquals(message, expectedMap.get(key), measure.getData());
      }
    }
  }

}
