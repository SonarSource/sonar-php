/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php;

import java.io.File;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.php.cache.Cache;
import org.sonarsource.analyzer.commons.ProgressReport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ScannerTest {

  private final SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());
  private ProgressReport progressReport = mock(ProgressReport.class);

  @Test
  void progressReportShouldBeStopped() {
    TestScanner scanner = new TestScanner(context);
    scanner.execute(progressReport, Collections.singletonList(PhpTestUtils.inputFile("empty.php")));
    verify(progressReport).stop();
  }

  @Test
  void cancelledAnalysis() {
    Scanner scanner = new TestScanner(context);
    context.setCancelled(true);
    Throwable throwable = catchThrowable(() -> scanner.execute(progressReport, Collections.singletonList(PhpTestUtils.inputFile("empty.php"))));
    assertThat(throwable).hasMessage("Analysis cancelled");
    verify(progressReport).cancel();
    verify(progressReport, never()).stop();
  }

  private static class TestScanner extends Scanner {

    TestScanner(SensorContext context) {
      super(context, new DurationStatistics(context.config()), mock(Cache.class));
    }

    @Override
    String name() {
      return "Test scanner";
    }

    @Override
    void scanFile(InputFile file) {
    }

    @Override
    void logException(Exception e, InputFile file) {
    }
  }
}
