/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.plugins.php.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;
import org.sonarsource.analyzer.commons.FileProvider;

public final class ExternalReportWildcardProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ExternalReportWildcardProvider.class);
  private static final int MINIMUM_MAJOR_SUPPORTED_VERSION = 7;
  private static final int MINIMUM_MINOR_SUPPORTED_VERSION = 2;

  private ExternalReportWildcardProvider() {
    // util class
  }

  public static List<File> getReportFiles(SensorContext context, String externalReportsProperty) {
    String[] reportPaths = context.config().getStringArray(externalReportsProperty);
    if (reportPaths.length == 0) {
      return Collections.emptyList();
    }

    var minumumVersion = Version.create(MINIMUM_MAJOR_SUPPORTED_VERSION, MINIMUM_MINOR_SUPPORTED_VERSION);
    boolean externalIssuesSupported = context.runtime().getApiVersion().isGreaterThanOrEqual(minumumVersion);
    if (!externalIssuesSupported) {
      LOG.warn("Import of external issues aborted! Import requires SonarQube 7.2 or greater.");
      return Collections.emptyList();
    }

    List<File> result = new ArrayList<>();
    for (String reportPath : reportPaths) {
      try {
        var reports = getIOFiles(context.fileSystem().baseDir(), reportPath);
        result.addAll(reports);
      } catch (IllegalStateException e) {
        LOG.debug("Exception when searching for report files to import.", e);
      }
    }

    return result;
  }

  private static boolean isWildcard(String path) {
    return path.contains("*") || path.contains("?");
  }

  private static List<File> getIOFiles(File baseDir, String reportPath) {
    if (!isWildcard(reportPath)) {
      return List.of(getSpecificFile(baseDir, reportPath));
    } else {
      var fileProvider = new FileProvider(baseDir, reportPath);
      return fileProvider.getMatchingFiles();
    }
  }

  private static File getSpecificFile(File baseDir, String path) {
    var file = new File(path);
    if (!file.isAbsolute()) {
      file = new File(baseDir, path);
    }

    return file;
  }
}
