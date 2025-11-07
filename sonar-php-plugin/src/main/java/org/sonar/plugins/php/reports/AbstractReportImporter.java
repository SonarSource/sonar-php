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
package org.sonar.plugins.php.reports;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

public abstract class AbstractReportImporter implements ReportImporter {

  protected final Set<String> unresolvedInputFiles = new LinkedHashSet<>();
  protected final AnalysisWarningsWrapper analysisWarningsWrapper;
  protected ExternalReportFileHandler fileHandler;
  private ExclusionPattern[] exclusionPatterns = {};

  protected AbstractReportImporter(AnalysisWarningsWrapper analysisWarningsWrapper) {
    this.analysisWarningsWrapper = analysisWarningsWrapper;
  }

  @Override
  public void execute(SensorContext context) {
    exclusionPatterns = ExclusionPattern.create(context.config().getStringArray("sonar.exclusion"));
    fileHandler = ExternalReportFileHandler.create(context);
    List<File> reportFiles = getReportFiles(context);

    reportFiles.forEach((File report) -> {
      unresolvedInputFiles.clear();
      importExternalReport(report, context);
      logUnresolvedInputFiles(report);
    });
  }

  protected void importExternalReport(File reportPath, SensorContext context) {
    try {
      importReport(reportPath, context);
    } catch (Exception e) {
      logFileCantBeRead(e, reportPath);
    }
  }

  public void logFileCantBeRead(Exception e, File reportPath) {
    String msg = getFileReadErrorMessage(e, reportPath);
    logger().warn(msg);
    analysisWarningsWrapper.addWarning(msg);
  }

  protected void addUnresolvedInputFile(String filePath) {
    if (!isExcluded(filePath)) {
      unresolvedInputFiles.add(filePath);
    }
  }

  private boolean isExcluded(String filePath) {
    if (exclusionPatterns.length == 0) {
      return false;
    }

    var path = Path.of(filePath);
    for (ExclusionPattern exclusionPattern : exclusionPatterns) {
      if (exclusionPattern.match(path)) {
        return true;
      }
    }
    return false;
  }

  private void logUnresolvedInputFiles(File reportPath) {
    if (unresolvedInputFiles.isEmpty()) {
      return;
    }
    String fileList = unresolvedInputFiles.stream().sorted().limit(MAX_LOGGED_FILE_NAMES).collect(Collectors.joining(";"));
    if (unresolvedInputFiles.size() > MAX_LOGGED_FILE_NAMES) {
      fileList += ";...";
    }
    var msg = String.format(getUnresolvedInputFileMessageFormat(),
      unresolvedInputFiles.size(), reportName(), reportPath.getName(), fileList);
    logger().warn(msg);
    analysisWarningsWrapper.addWarning(msg);
  }

  /**
   * Inspired by org.sonar.api.batch.fs.internal.PathPattern
   */
  private static final class ExclusionPattern {

    private final WildcardPattern pattern;

    private ExclusionPattern(String pattern) {
      this.pattern = WildcardPattern.create(pattern);
    }

    static ExclusionPattern[] create(String[] s) {
      var result = new ExclusionPattern[s.length];
      for (var i = 0; i < s.length; i++) {
        result[i] = new ExclusionPattern(s[i]);
      }
      return result;
    }

    boolean match(Path relativePath) {
      var path = PathUtils.sanitize(relativePath.toString());
      return path != null && pattern.match(path);
    }
  }
}
