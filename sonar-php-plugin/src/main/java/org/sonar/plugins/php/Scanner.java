/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.analyzer.commons.ProgressReport;

abstract class Scanner {

  private static final Logger LOG = Loggers.get(Scanner.class);
  private static final String FAIL_FAST_PROPERTY_NAME = "sonar.internal.analysis.failFast";
  protected final SensorContext context;
  protected final DurationStatistics statistics;

  Scanner(SensorContext context, DurationStatistics statistics) {
    this.context = context;
    this.statistics = statistics;
  }

  void execute(List<InputFile> files) {
    ProgressReport progressReport = new ProgressReport("PHP analyzer progress", TimeUnit.SECONDS.toMillis(10));
    execute(progressReport, files);
  }

  void execute(ProgressReport progressReport, List<InputFile> files) {
    LOG.info("Starting " + this.name());
    List<String> filenames = files.stream().map(InputFile::toString).collect(Collectors.toList());
    progressReport.start(filenames);

    boolean success = false;
    try {
      for (InputFile file : files) {
        if (context.isCancelled()) {
          throw new CancellationException();
        }
        processFile(file);
        progressReport.nextFile();
      }
      onEnd();
      success = true;
    } finally {
      stopProgressReport(progressReport, success);
    }
  }

  private void processFile(InputFile file) {
    try {
      this.scanFile(file);
    } catch (Exception e) {
      this.logException(e, file);
      if (context.config().getBoolean(FAIL_FAST_PROPERTY_NAME).orElse(false)) {
        throw new IllegalStateException("Exception when analyzing " + file, e);
      }
    }
  }

  private static void stopProgressReport(ProgressReport progressReport, boolean success) {
    if (success) {
      progressReport.stop();
    } else {
      progressReport.cancel();
    }
  }

  abstract String name();

  abstract void scanFile(InputFile file);

  abstract void logException(Exception e, InputFile file);

  void onEnd() {
  }
}
