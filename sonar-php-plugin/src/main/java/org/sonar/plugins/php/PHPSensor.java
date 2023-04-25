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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.cache.CacheContextImpl;
import org.sonar.php.checks.CheckList;
import org.sonar.php.filters.SuppressWarningFilter;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;

public class PHPSensor implements Sensor {

  private static final Logger LOG = Loggers.get(PHPSensor.class);
  private final FileLinesContextFactory fileLinesContextFactory;
  private final PHPChecks checks;
  private final NoSonarFilter noSonarFilter;
  @Nullable
  private final SuppressWarningFilter suppressWarningFilter;


  // Needed for SonarLint
  public PHPSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter, SuppressWarningFilter suppressWarningFilter) {
    this(fileLinesContextFactory, checkFactory, noSonarFilter, null, suppressWarningFilter);
  }

  public PHPSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter,
    @Nullable PHPCustomRuleRepository[] customRuleRepositories, @Nullable SuppressWarningFilter suppressWarningFilter) {
    this(fileLinesContextFactory,
      PHPChecks.createPHPCheck(checkFactory).addChecks(CheckList.REPOSITORY_KEY, CheckList.getPhpChecks()).addCustomChecks(customRuleRepositories),
      noSonarFilter, suppressWarningFilter);
  }

  PHPSensor(FileLinesContextFactory fileLinesContextFactory, PHPChecks checks, NoSonarFilter noSonarFilter, @Nullable SuppressWarningFilter suppressWarningFilter) {
    this.checks = checks;
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.suppressWarningFilter = suppressWarningFilter;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(Php.KEY)
      .name("PHP sensor");
  }

  @Override
  public void execute(SensorContext context) {
    DurationStatistics statistics = new DurationStatistics(context.config());
    List<InputFile> inputFiles = getInputFiles(context);

    CacheContext cacheContext = CacheContextImpl.of(context);
    SymbolScanner symbolScanner = SymbolScanner.create(context, statistics, cacheContext);

    try {
      symbolScanner.execute(inputFiles);
      ProjectSymbolData projectSymbolData = symbolScanner.getProjectSymbolData();
      AnalysisScanner analysisScanner = new AnalysisScanner(context,
        checks,
        fileLinesContextFactory,
        noSonarFilter,
        projectSymbolData,
        statistics,
        cacheContext,
        suppressWarningFilter);
      analysisScanner.execute(inputFiles);
    } catch (CancellationException e) {
      LOG.info(e.getMessage());
    }

    statistics.log();
  }

  private static List<InputFile> getInputFiles(SensorContext context) {
    FileSystem fileSystem = context.fileSystem();

    FilePredicate phpFilePredicate = fileSystem.predicates().hasLanguage(Php.KEY);

    List<InputFile> inputFiles = new ArrayList<>();
    fileSystem.inputFiles(phpFilePredicate).forEach(inputFiles::add);
    return inputFiles;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
