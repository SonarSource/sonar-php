/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php;

import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.DurationStatistics;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.php.cache.Cache;
import org.sonarsource.analyzer.commons.ProgressReport;

abstract class Scanner {
  private static final Logger LOG = LoggerFactory.getLogger(Scanner.class);

  /**
   * Describes if an optimized analysis of unchanged files by skipping some rules is enabled.
   * By default, the property is not set (null), leaving SQ/SC to decide whether to enable this behavior.
   * Setting it to true or false, forces the behavior from the analyzer independently of the server.
   */
  public static final String SONAR_CAN_SKIP_UNCHANGED_FILES_KEY = "sonar.php.skipUnchanged";
  private static final String FAIL_FAST_PROPERTY_NAME = "sonar.internal.analysis.failFast";
  protected final SensorContext context;
  protected final DurationStatistics statistics;
  protected final Cache cache;
  protected boolean optimizedAnalysis;

  Scanner(SensorContext context, DurationStatistics statistics, Cache cache) {
    this.context = context;
    this.statistics = statistics;
    this.cache = cache;
    optimizedAnalysis = shouldOptimizeAnalysis();
  }

  void execute(List<InputFile> files) {
    ProgressReport progressReport = new ProgressReport("PHP analyzer progress", TimeUnit.SECONDS.toMillis(10));
    execute(progressReport, files);
  }

  void execute(ProgressReport progressReport, List<InputFile> files) {
    String name = this.name();
    LOG.info("Starting {}", name);
    List<String> filenames = files.stream().map(InputFile::toString).toList();
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

  protected static boolean inSonarLint(SensorContext context) {
    return context.runtime().getProduct() == SonarProduct.SONARLINT;
  }

  private boolean shouldOptimizeAnalysis() {
    return !(inSonarLint(context)) &&
      (context.canSkipUnchangedFiles() || context.config().getBoolean(SONAR_CAN_SKIP_UNCHANGED_FILES_KEY).orElse(false));
  }

  protected boolean fileCanBeSkipped(InputFile file) {
    return optimizedAnalysis && fileIsUnchanged(file);
  }

  private boolean fileIsUnchanged(InputFile inputFile) {
    if (inputFile.status() != null && !inputFile.status().equals(InputFile.Status.SAME)) {
      return false;
    }
    byte[] fileHash = cache.readFileContentHash(inputFile);
    // InputFile.Status is not reliable in some cases
    // We use the hash of the file's content to double-check the content is the same.
    try {
      byte[] bytes = FileHashingUtils.inputFileContentHash(inputFile);
      return MessageDigest.isEqual(fileHash, bytes);
    } catch (IllegalStateException ise) {
      LOG.debug("Failed to compute content hash for file {}", inputFile.key());
      return false;
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
