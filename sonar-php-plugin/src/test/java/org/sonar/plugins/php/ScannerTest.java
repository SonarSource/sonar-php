/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonarsource.analyzer.commons.ProgressReport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ScannerTest {

  private final SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());
  private ProgressReport progressReport = mock(ProgressReport.class);

  @org.junit.Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void progress_report_should_be_stopped() {
    TestScanner scanner = new TestScanner(context);
    scanner.execute(progressReport, Collections.singletonList(PhpTestUtils.inputFile("empty.php")));
    verify(progressReport).stop();
  }

  @Test
  public void cancelled_analysis() {
    Scanner scanner = new TestScanner(context);
    context.setCancelled(true);
    thrown.expectMessage("Analysis cancelled");
    try {
      scanner.execute(progressReport, Collections.singletonList(PhpTestUtils.inputFile("empty.php")));
    } finally {
      verify(progressReport).cancel();
      verify(progressReport, never()).stop();
    }
  }
  private static class TestScanner extends Scanner {

   TestScanner(SensorContext context) {
      super(context);
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
