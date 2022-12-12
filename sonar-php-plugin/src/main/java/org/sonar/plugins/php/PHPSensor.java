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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.DurationStatistics;
import org.sonar.api.SonarProduct;
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
import org.sonar.php.checks.CheckList;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;
import org.sonar.plugins.php.reports.phpunit.CoverageResultImporter;
import org.sonar.plugins.php.reports.phpunit.TestResultImporter;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

import static org.sonar.plugins.php.warning.DefaultAnalysisWarningsWrapper.NOOP_ANALYSIS_WARNINGS;

public class PHPSensor implements Sensor {

  private static final Logger LOG = Loggers.get(PHPSensor.class);
  private final FileLinesContextFactory fileLinesContextFactory;
  private final PHPChecks checks;
  private final NoSonarFilter noSonarFilter;
  private final AnalysisWarningsWrapper analysisWarningsWrapper;


  // Needed for SonarLint
  public PHPSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter) {
    this(fileLinesContextFactory, checkFactory, noSonarFilter, null, NOOP_ANALYSIS_WARNINGS);
  }

  public PHPSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter,
    @Nullable PHPCustomRuleRepository[] customRuleRepositories) {
    this(fileLinesContextFactory, checkFactory, noSonarFilter, customRuleRepositories , NOOP_ANALYSIS_WARNINGS);
  }

  public PHPSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter,
    @Nullable PHPCustomRuleRepository[] customRuleRepositories, AnalysisWarningsWrapper analysisWarningsWrapper) {
    this(fileLinesContextFactory,
      PHPChecks.createPHPCheck(checkFactory).addChecks(CheckList.REPOSITORY_KEY, CheckList.getPhpChecks()).addCustomChecks(customRuleRepositories),
      noSonarFilter,
      analysisWarningsWrapper);
  }

  PHPSensor(FileLinesContextFactory fileLinesContextFactory, PHPChecks checks, NoSonarFilter noSonarFilter, AnalysisWarningsWrapper analysisWarningsWrapper) {
    this.checks = checks;
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.analysisWarningsWrapper = analysisWarningsWrapper;
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

    SymbolScanner symbolScanner = SymbolScanner.create(context, statistics);

    try {
      symbolScanner.execute(inputFiles);
      ProjectSymbolData projectSymbolData = symbolScanner.getProjectSymbolData();
      AnalysisScanner analysisScanner = new AnalysisScanner(context,
        checks,
        fileLinesContextFactory,
        noSonarFilter,
        projectSymbolData,
        statistics);
      analysisScanner.execute(inputFiles);
      if (!inSonarLint(context)) {
        processTestsAndCoverage(context);
      }
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

  private static boolean inSonarLint(SensorContext context) {
    return context.runtime().getProduct() == SonarProduct.SONARLINT;
  }

  private void processTestsAndCoverage(SensorContext context) {
    new TestResultImporter(analysisWarningsWrapper).execute(context);
    new CoverageResultImporter(analysisWarningsWrapper).execute(context);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
