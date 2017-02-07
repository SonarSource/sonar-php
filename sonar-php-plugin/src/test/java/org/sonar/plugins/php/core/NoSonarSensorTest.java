/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.php.core;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.squidbridge.text.Source;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

public class NoSonarSensorTest {

  private DefaultFileSystem fs;
  private NoSonarFilter noSonarFilter;
  private NoSonarSensor sensor;

  private SensorContextTester setUpForSensorContextTester() {
    return SensorContextTester.create(new File("src/test/resources"));
  }

  private SensorContext setUpForMockedSensorContext() {
    return Mockito.mock(SensorContext.class);
  }

  @Before
  public void setUp() throws Exception {
    fs = PhpTestUtils.getDefaultFileSystem();
    noSonarFilter = new NoSonarFilter();
    sensor = spy(new NoSonarSensor(fs, noSonarFilter));
  }

  @Test
  public void testAnalyse() {
    SensorContextTester context = setUpForSensorContextTester();

    String moduleKey = "moduleKey";
    String fileName = "Mail.php";
    DefaultInputFile mainFile = new DefaultInputFile(moduleKey, fileName).setLanguage(Php.KEY).setType(InputFile.Type.MAIN);
    fs.add(mainFile);

    sensor.execute(context);
  }

  @Test
  public void testAnalyseEmptySourceFiles() {
    SensorContext context = setUpForMockedSensorContext();

    DefaultInputFile file = new DefaultInputFile("moduleKey", "nonexistent.php").setType(InputFile.Type.MAIN).setLanguage(Php.KEY);
    fs.add(file);

    NoSonarSensor localSensor = new NoSonarSensor(fs, noSonarFilter);
    localSensor.execute(context);
  }

  @Test
  public void testAnalyseSourceCode() {
    File file = new File(this.getClass().getResource("/Mail.php").getPath());
    Source source = NoSonarSensor.analyseSourceCode(file, UTF_8);
    assertEquals(1, source.getNoSonarTagLines().size());
    assertEquals(17, (int) source.getNoSonarTagLines().iterator().next());
  }

  @Test
  public void testAnalyseSourceCodeWithRegions() {
    File file = new File(this.getClass().getResource("/Math2.php").getPath());
    Source source = NoSonarSensor.analyseSourceCode(file, UTF_8);
    assertEquals(1, source.getNoSonarTagLines().size());
    assertEquals(126, (int) source.getNoSonarTagLines().iterator().next());
  }

  @Test
  public void testAnalyseSourceCodeWithNoNoSonar() {
    File file = new File(this.getClass().getResource("/Math3.php").getPath());
    Source source = NoSonarSensor.analyseSourceCode(file, UTF_8);
    assertEquals(0, source.getNoSonarTagLines().size());
  }

  @Test
  public void testAnalyseSourceCodeWithNonexistentFile() throws Exception {
    NoSonarSensor.analyseSourceCode(new File("xxx"), UTF_8);
  }

}
