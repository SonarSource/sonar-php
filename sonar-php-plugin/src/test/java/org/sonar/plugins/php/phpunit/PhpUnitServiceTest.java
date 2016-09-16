/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.plugins.php.phpunit;

import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.test.TestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PhpUnitServiceTest {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  private DefaultFileSystem fs;

  @Mock
  private PhpUnitResultParser parser;

  @Mock
  private PhpUnitCoverageResultParser coverageParser;

  @Mock
  private PhpUnitItCoverageResultParser itCoverageParser;

  @Mock
  private PhpUnitOverallCoverageResultParser overallCoverageParser;

  @Mock
  private SensorContext context;
  
  @Mock
  private Map<File, Integer> numberOfLinesOfCode;

  private Settings settings;
  private PhpUnitService sensor;
  private static final File TEST_REPORT_FILE = TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_NAME);
  private static final File COVERAGE_REPORT_FILE = TestUtils.getResource(PhpTestUtils.PHPUNIT_COVERAGE_REPORT);

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);

    settings = newSettings();
    fs = new DefaultFileSystem(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_DIR));
    sensor = createService(fs, settings);
  }

  @Test
  public void testToString() {
    assertThat(sensor.toString()).isEqualTo("PHPUnit Sensor");
  }

  @Test
  public void shouldParseReport() {
    sensor = createService(fs, settings);
    sensor.execute(context, numberOfLinesOfCode);

    verify(parser, times(1)).parse(TEST_REPORT_FILE, context, numberOfLinesOfCode);
    verify(coverageParser, times(1)).parse(COVERAGE_REPORT_FILE, context, numberOfLinesOfCode);
    verify(itCoverageParser, times(1)).parse(COVERAGE_REPORT_FILE, context, numberOfLinesOfCode);
    verify(overallCoverageParser, times(1)).parse(COVERAGE_REPORT_FILE, context, numberOfLinesOfCode);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void noReport() {
    sensor = createService(fs, new Settings());
    sensor.execute(context, numberOfLinesOfCode);

    verify(parser, never()).parse(any(File.class), any(SensorContext.class), anyMap());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void badReport() {
    sensor = createService(fs, settings("/fake/path.xml"));
    sensor.execute(context, numberOfLinesOfCode);

    verify(parser, never()).parse(any(File.class), any(SensorContext.class), anyMap());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void xstream_exception() throws Exception {
    Mockito.doThrow(new XStreamException("")).when(parser).parse((File) any(), any(SensorContext.class), anyMap());
    sensor = createService(fs, settings("phpunit.xml"));
    expected.expect(IllegalStateException.class);
    sensor.execute(context, numberOfLinesOfCode);
  }

  @Test
  public void should_parse_relative_path_report() {
    sensor = createService(fs, settings("phpunit.xml"));
    sensor.execute(context, numberOfLinesOfCode);

    verify(parser, times(1)).parse(TEST_REPORT_FILE, context, numberOfLinesOfCode);
  }

  private PhpUnitService createService(FileSystem fs, Settings settings) {
    return new PhpUnitService(fs, settings, parser, coverageParser, itCoverageParser, overallCoverageParser);
  }

  private Settings settings(String path) {
    Properties props = new Properties();
    props.put(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, path);
    Settings settings = new Settings();
    settings.addProperties(props);
    return settings;
  }

  private static Settings newSettings() {
    Settings settings = new Settings();
    Properties props = new Properties();

    props.put(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, PhpUnitServiceTest.TEST_REPORT_FILE.getAbsolutePath());
    props.put(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY, PhpUnitServiceTest.COVERAGE_REPORT_FILE.getAbsolutePath());
    props.put(PhpPlugin.PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY, PhpUnitServiceTest.COVERAGE_REPORT_FILE.getAbsolutePath());
    props.put(PhpPlugin.PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY, PhpUnitServiceTest.COVERAGE_REPORT_FILE.getAbsolutePath());

    settings.addProperties(props);

    return settings;
  }

}
