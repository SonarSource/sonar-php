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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.util.HashMap;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.test.TestUtils;

public class PhpUnitTestResultImporterTest {

  private PhpUnitTestResultImporter importer;

  private SensorContextTester setUpForSensorContextTester() {
    SensorContextTester context = SensorContextTester.create(new File("src/test/resources"));
    importer = new PhpUnitTestResultImporter();
    return context;
  }

  @Test()
  public void shouldGenerateTestsMeasures() {
    SensorContextTester context = setUpForSensorContextTester();
    File baseDir = TestUtils.getResource("/org/sonar/plugins/php/phpunit/sensor/src/");
    DefaultFileSystem fs = new DefaultFileSystem(baseDir);
    DefaultInputFile appTestFile = new DefaultInputFile("moduleKey", "src/AppTest.php").setType(InputFile.Type.TEST).setLanguage(Php.KEY);
    DefaultInputFile appSkippedTestFile = new DefaultInputFile("moduleKey", "src/AppSkipTest.php").setType(InputFile.Type.TEST).setLanguage(Php.KEY);

    fs.add(appTestFile);
    fs.add(appSkippedTestFile);
    context.setFileSystem(fs);

    String appTest = "moduleKey:" + "src/AppTest.php";
    String appSkipTest = "moduleKey:" + "src/AppSkipTest.php";

    importer = new PhpUnitTestResultImporter();
    importer.importReport(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_NAME), context, new HashMap<>());

    PhpTestUtils.assertMeasure(context, appTest, CoreMetrics.TESTS, 1);
    PhpTestUtils.assertMeasure(context, appTest, CoreMetrics.TEST_FAILURES, 0);
    PhpTestUtils.assertMeasure(context, appTest, CoreMetrics.TEST_ERRORS, 0);
    PhpTestUtils.assertMeasure(context, appTest, CoreMetrics.SKIPPED_TESTS, 0);
    PhpTestUtils.assertMeasure(context, appTest, CoreMetrics.TEST_EXECUTION_TIME, 0L);
    PhpTestUtils.assertMeasure(context, appTest, CoreMetrics.TEST_SUCCESS_DENSITY, 100.00);

    PhpTestUtils.assertMeasure(context, appSkipTest, CoreMetrics.TESTS, 0);
    PhpTestUtils.assertMeasure(context, appSkipTest, CoreMetrics.TEST_FAILURES, 0);
    PhpTestUtils.assertMeasure(context, appSkipTest, CoreMetrics.TEST_ERRORS, 0);
    PhpTestUtils.assertMeasure(context, appSkipTest, CoreMetrics.SKIPPED_TESTS, 1);
    PhpTestUtils.assertMeasure(context, appSkipTest, CoreMetrics.TEST_EXECUTION_TIME, 0L);
    PhpTestUtils.assertNoMeasure(context, appSkipTest, CoreMetrics.TEST_SUCCESS_DENSITY);
  }

}
