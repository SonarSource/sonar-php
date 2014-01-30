/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.php;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.AstScanner;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.php.PHPAstScanner;
import org.sonar.php.PHPConfiguration;
import org.sonar.php.api.PHPMetric;
import org.sonar.plugins.php.api.Php;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.indexer.QueryByType;

import java.io.File;
import java.util.Collection;

public class PHPSquidSensor implements Sensor {

  private Project project;
  private SensorContext context;
  private AstScanner<Grammar> scanner;

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return Php.KEY.equals(project.getLanguageKey());
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    this.project = project;
    this.context = context;
    this.scanner = PHPAstScanner.create(createConfiguration());

    scanner.scanFiles(getProjectMainFiles());
    saveMeasures(scanner.getIndex().search(new QueryByType(SourceFile.class)));
  }

  private void saveMeasures(Collection<SourceCode> squidSourceFiles) {
    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;
      org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File.fromIOFile(new java.io.File(squidFile.getKey()), project);

      saveFileMeasures(sonarFile, squidFile);
    }
  }

  private void saveFileMeasures(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    context.saveMeasure(sonarFile, CoreMetrics.FILES, squidFile.getDouble(PHPMetric.FILES));
    context.saveMeasure(sonarFile, CoreMetrics.LINES, squidFile.getDouble(PHPMetric.LINES));
    context.saveMeasure(sonarFile, CoreMetrics.NCLOC, squidFile.getDouble(PHPMetric.LINES_OF_CODE));
    context.saveMeasure(sonarFile, CoreMetrics.COMMENT_LINES, squidFile.getDouble(PHPMetric.COMMENT_LINES));
    context.saveMeasure(sonarFile, CoreMetrics.CLASSES, squidFile.getDouble(PHPMetric.CLASSES));
    context.saveMeasure(sonarFile, CoreMetrics.FUNCTIONS, squidFile.getDouble(PHPMetric.FUNCTIONS));
    context.saveMeasure(sonarFile, CoreMetrics.STATEMENTS, squidFile.getDouble(PHPMetric.STATEMENTS));
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY, squidFile.getDouble(PHPMetric.COMPLEXITY));
  }

  private PHPConfiguration createConfiguration() {
    return new PHPConfiguration(project.getFileSystem().getSourceCharset());
  }

  private Collection<File> getProjectMainFiles() {
    return InputFileUtils.toFiles(project.getFileSystem().mainFiles(Php.KEY));
  }
}
