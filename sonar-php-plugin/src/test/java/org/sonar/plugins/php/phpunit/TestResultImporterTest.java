/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;

public class TestResultImporterTest {

  private TestResultImporter importer;

  private SensorContextTester setUpForSensorContextTester() {
    SensorContextTester context = SensorContextTester.create(new File("src/test/resources"));
    importer = new TestResultImporter();
    return context;
  }

  @Test()
  public void shouldGenerateTestsMeasures() {
    SensorContextTester context = setUpForSensorContextTester();
    File baseDir = new File("src/test/resources/org/sonar/plugins/php/phpunit/sensor/src/");
    DefaultFileSystem fs = new DefaultFileSystem(baseDir.getAbsoluteFile());
    DefaultInputFile appTestFile = TestInputFileBuilder.create("moduleKey", "src/AppTest.php").setType(InputFile.Type.TEST).setLanguage(Php.KEY).build();
    DefaultInputFile appSkippedTestFile = TestInputFileBuilder.create("moduleKey", "src/AppSkipTest.php").setType(InputFile.Type.TEST).setLanguage(Php.KEY).build();

    fs.add(appTestFile);
    fs.add(appSkippedTestFile);
    context.setFileSystem(fs);

    String appTestFileKey = appTestFile.key();
    String appSkipTestFileKey = appSkippedTestFile.key();

    importer = new TestResultImporter();
    importer.importReport(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_NAME), context);

    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.TESTS, 1);
    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.TEST_FAILURES, 0);
    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.TEST_ERRORS, 0);
    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.SKIPPED_TESTS, 0);
    PhpTestUtils.assertMeasure(context, appTestFileKey, CoreMetrics.TEST_EXECUTION_TIME, 0L);

    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.TESTS, 0);
    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.TEST_FAILURES, 0);
    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.TEST_ERRORS, 0);
    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.SKIPPED_TESTS, 1);
    PhpTestUtils.assertMeasure(context, appSkipTestFileKey, CoreMetrics.TEST_EXECUTION_TIME, 0L);
  }

}
