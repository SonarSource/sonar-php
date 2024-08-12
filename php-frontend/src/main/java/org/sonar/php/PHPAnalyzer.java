/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.php.filters.SuppressWarningFilter;
import org.sonar.php.metrics.CommentLineVisitor;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.metrics.MetricsVisitor;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.php.tree.visitors.PHPCheckContext;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpInputFileContext;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class PHPAnalyzer {
  private static final Logger LOG = LoggerFactory.getLogger(PHPAnalyzer.class);

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser();
  private final List<PHPCheck> checks;
  private final List<PHPCheck> testFileChecks;
  @Nullable
  private final File workingDir;
  private final ProjectSymbolData projectSymbolData;
  private final DurationStatistics statistics;
  private final CacheContext cacheContext;
  private final SuppressWarningFilter suppressWarningFilter;
  private final boolean frameworkDetectionEnabled;

  private PhpInputFileContext currentFileContext;

  private CompilationUnitTree currentFileTree;
  private PhpFile currentFile;
  private SymbolTable currentFileSymbolTable;

  public PHPAnalyzer(List<PHPCheck> checks,
    List<PHPCheck> testFileChecks,
    @Nullable File workingDir,
    ProjectSymbolData projectSymbolData,
    DurationStatistics statistics,
    @Nullable CacheContext cacheContext,
    SuppressWarningFilter suppressWarningFilter,
    boolean frameworkDetectionEnabled) {
    this.checks = checks;
    this.testFileChecks = testFileChecks;
    this.workingDir = workingDir;
    this.projectSymbolData = projectSymbolData;
    this.statistics = statistics;
    this.cacheContext = cacheContext;
    this.suppressWarningFilter = suppressWarningFilter;
    this.frameworkDetectionEnabled = frameworkDetectionEnabled;
    for (PHPCheck check : checks) {
      check.init();
    }
  }

  public void nextFile(InputFile inputFile) throws RecognitionException {
    currentFile = PhpFileImpl.create(inputFile);
    currentFileContext = new PhpInputFileContext(currentFile, workingDir, cacheContext);
    currentFileTree = (CompilationUnitTree) statistics.time("CheckParsing", () -> parser.parse(currentFile.contents()));
    currentFileSymbolTable = statistics.time("CheckSymbolTable",
      () -> SymbolTableImpl.create(currentFileTree, projectSymbolData, PhpFileImpl.create(inputFile), frameworkDetectionEnabled));
  }

  public List<PhpIssue> analyze() {
    List<PhpIssue> allIssues = new ArrayList<>();
    for (PHPCheck check : checks) {
      PHPCheckContext context = new PHPCheckContext(currentFileContext, currentFileTree, currentFileSymbolTable);
      List<PhpIssue> issues = statistics.time(check.getClass().getSimpleName(), () -> {
        try {
          return check.analyze(context);
        } catch (StackOverflowError e) {
          LOG.error("Stack overflow of {} in file {}", check.getClass().getName(), currentFile.uri());
          throw e;
        }
      });
      allIssues.addAll(issues);
    }
    PHPCheckContext context = new PHPCheckContext(currentFileContext, currentFileTree, currentFileSymbolTable);
    suppressWarningFilter.analyze(context);

    return allIssues;
  }

  public List<PhpIssue> analyzeTest() {
    PHPCheckContext context = new PHPCheckContext(currentFileContext, currentFileTree, currentFileSymbolTable);
    suppressWarningFilter.analyze(context);
    return testFileChecks.stream()
      .map(check -> check.analyze(new PHPCheckContext(currentFileContext, currentFileTree, currentFileSymbolTable)))
      .flatMap(List::stream)
      .toList();
  }

  public void terminate() {
    for (PHPCheck check : checks) {
      try {
        check.terminate();
      } catch (Exception e) {
        LOG.warn("An error occurred while trying to terminate checks:", e);
      }
    }
  }

  public FileMeasures computeMeasures(FileLinesContext fileLinesContext) {
    return new MetricsVisitor().getFileMeasures(currentFile, currentFileTree, currentFileSymbolTable, fileLinesContext);
  }

  public Set<Integer> computeNoSonarLines() {
    return new CommentLineVisitor(currentFileTree).noSonarLines();
  }

  public CompilationUnitTree currentFileTree() {
    return currentFileTree;
  }

  public SymbolTable currentFileSymbolTable() {
    return currentFileSymbolTable;
  }
}
