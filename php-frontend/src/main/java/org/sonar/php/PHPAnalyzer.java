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
package org.sonar.php;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.highlighter.SymbolHighlighter;
import org.sonar.php.highlighter.SyntaxHighlighterVisitor;
import org.sonar.php.metrics.CommentLineVisitor;
import org.sonar.php.metrics.CpdVisitor;
import org.sonar.php.metrics.CpdVisitor.CpdToken;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.metrics.MetricsVisitor;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.php.tree.visitors.PHPCheckContext;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class PHPAnalyzer {
  private static final Logger LOG = Loggers.get(PHPAnalyzer.class);

  private final ActionParser<Tree> parser;
  private final List<PHPCheck> checks;
  private final List<PHPCheck> testFileChecks;
  @Nullable
  private final File workingDir;
  private final ProjectSymbolData projectSymbolData;

  private CompilationUnitTree currentFileTree;
  private PhpFile currentFile;
  private SymbolTable currentFileSymbolTable;

  public PHPAnalyzer(List<PHPCheck> checks, @Nullable File workingDir, ProjectSymbolData projectSymbolData) {
    this(checks, Collections.emptyList(), workingDir, projectSymbolData);
  }

  public PHPAnalyzer(List<PHPCheck> checks, List<PHPCheck> testFilesChecks, @Nullable File workingDir, ProjectSymbolData projectSymbolData) {
    this.workingDir = workingDir;
    this.projectSymbolData = projectSymbolData;
    this.parser = PHPParserBuilder.createParser();
    this.checks = checks;
    this.testFileChecks = testFilesChecks;

    for (PHPCheck check : checks) {
      check.init();
    }

    for (PHPCheck check : testFilesChecks) {
      check.init();
    }
  }

  public void nextFile(PhpFile file) {
    currentFile = file;
    currentFileTree = (CompilationUnitTree) parser.parse(file.contents());
    currentFileSymbolTable = SymbolTableImpl.create(currentFileTree, projectSymbolData, file);
  }

  public List<PhpIssue> analyze() {
    List<PhpIssue> allIssues = new ArrayList<>();
    for (PHPCheck check : checks) {
      PHPCheckContext context = new PHPCheckContext(currentFile, currentFileTree, workingDir, currentFileSymbolTable);
      List<PhpIssue> issues = Collections.emptyList();
      try {
        issues = check.analyze(context);
      } catch (StackOverflowError e) {
        LOG.error("Stack overflow of {} in file {}", check.getClass().getName(), currentFile.uri());
        throw e;
      }
      allIssues.addAll(issues);
    }
    return allIssues;
  }

  public List<PhpIssue> analyzeTest() {
    PHPCheckContext context = new PHPCheckContext(currentFile, currentFileTree, workingDir, currentFileSymbolTable);
    return testFileChecks.stream()
      .map(check -> check.analyze(context))
      .flatMap(List::stream)
      .collect(Collectors.toList());
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

  public NewHighlighting getSyntaxHighlighting(SensorContext context, InputFile inputFile) {
    NewHighlighting highlighting = context.newHighlighting().onFile(inputFile);
    SyntaxHighlighterVisitor.highlight(currentFileTree, highlighting);
    return highlighting;
  }

  public NewSymbolTable getSymbolHighlighting(SensorContext context, InputFile inputFile) {
    NewSymbolTable symbolTable = context.newSymbolTable().onFile(inputFile);
    new SymbolHighlighter().highlight(currentFileSymbolTable, symbolTable);
    return symbolTable;
  }

  public List<CpdToken> computeCpdTokens() {
    return new CpdVisitor().getCpdTokens(currentFile, currentFileTree, currentFileSymbolTable);
  }

  public Set<Integer> computeNoSonarLines() {
    return new CommentLineVisitor(currentFileTree).noSonarLines();
  }
}
