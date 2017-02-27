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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.php.phpunit.xml.TestSuites;

public class PhpUnitTestResultImporter implements PhpUnitImporter {

  final JUnitLogParserForPhpUnit parser = new JUnitLogParserForPhpUnit();

  @Override
  public void importReport(File reportFile, SensorContext context, Map<String, Integer> numberOfLinesOfCode) {
    Preconditions.checkNotNull(reportFile);
    TestSuites testSuites = parser.parse(reportFile);
    List<PhpUnitTestFileReport> fileReports = testSuites.arrangeSuitesIntoTestFileReports();
    for (PhpUnitTestFileReport fileReport : Lists.reverse(fileReports)) {
      fileReport.saveTestMeasures(context);
    }
  }

}
