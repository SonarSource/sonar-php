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
package org.sonar.plugins.php.api.tests;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonarsource.analyzer.commons.checks.verifier.MultiFileVerifier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class PHPCheckVerifier {

  private static final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  private final boolean readExpectedIssuesFromComments;
  private final boolean frameworkDetectionEnabled;

  /**
   * Internal use only. Subject to changes.
   */
  protected PHPCheckVerifier(boolean readExpectedIssuesFromComments, boolean frameworkDetectionEnabled) {
    this.readExpectedIssuesFromComments = readExpectedIssuesFromComments;
    this.frameworkDetectionEnabled = frameworkDetectionEnabled;
  }

  public static void verify(File sourceFile, PHPCheck check) {
    new PHPCheckVerifier(true, true).createVerifier(Collections.singletonList(sourceFile), check).assertOneOrMoreIssues();
  }

  public static void verifyNoIssue(File sourceFile, PHPCheck check) {
    new PHPCheckVerifier(true, true).createVerifier(Collections.singletonList(sourceFile), check).assertNoIssues();
  }

  public static void verify(PHPCheck check, File... files) {
    new PHPCheckVerifier(true, true).createVerifier(Arrays.asList(files), check).assertOneOrMoreIssues();
  }

  /**
   * Internal use only. Subject to changes.
   */
  protected MultiFileVerifier createVerifier(List<File> files, PHPCheck check) {
    MultiFileVerifier verifier = MultiFileVerifier.create(files.get(0).toPath(), UTF_8);

    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    Map<File, CompilationUnitTree> astByFile = new HashMap<>();
    for (File file : files) {
      PhpTestFile phpFile = new PhpTestFile(file);
      CompilationUnitTree ast = (CompilationUnitTree) parser.parse(phpFile.contents());
      astByFile.put(file, ast);
      var symbolTable = SymbolTableImpl.create(ast, new ProjectSymbolData(), phpFile, frameworkDetectionEnabled);
      symbolTable.classSymbolDatas().forEach(projectSymbolData::add);
      symbolTable.functionSymbolDatas().forEach(projectSymbolData::add);
    }

    for (File file : files) {
      addFile(check, verifier, astByFile.get(file), new PhpTestFile(file), projectSymbolData);
    }
    return verifier;
  }

  private void addFile(PHPCheck check, MultiFileVerifier verifier, CompilationUnitTree tree, PhpFile phpFile, ProjectSymbolData projectSymbolData) {
    var symbolTable = SymbolTableImpl.create(tree, projectSymbolData, phpFile, frameworkDetectionEnabled);
    check.init();
    for (PhpIssue issue : check.analyze(phpFile, tree, symbolTable)) {
      if (!issue.check().equals(check)) {
        throw new IllegalStateException("Verifier support only one kind of issue " + issue.check() + " != " + check);
      }
      addIssue(verifier, issue, phpFile).withGap(issue.cost());
    }
    if (readExpectedIssuesFromComments) {
      PHPVisitorCheck commentVisitor = new PHPVisitorCheck() {
        @Override
        public void visitTrivia(SyntaxTrivia trivia) {
          super.visitTrivia(trivia);
          int suffixLength = trivia.text().startsWith("//") ? 0 : 2;
          verifier.addComment(path(context().getPhpFile()), trivia.line(), trivia.column() + 1, trivia.text(), 2, suffixLength);
        }
      };
      commentVisitor.analyze(phpFile, tree);
    }
  }

  private static Path path(PhpFile phpFile) {
    return Paths.get(phpFile.uri());
  }

  private static MultiFileVerifier.Issue addIssue(MultiFileVerifier verifier, PhpIssue issue, PhpFile file) {
    if (issue instanceof LineIssue lineIssue) {
      return addLineIssue(verifier, lineIssue, file);
    } else if (issue instanceof FileIssue fileIssue) {
      return addFileIssue(verifier, fileIssue, file);
    } else {
      return addPreciseIssue(verifier, (PreciseIssue) issue, file);
    }
  }

  private static MultiFileVerifier.Issue addLineIssue(MultiFileVerifier verifier, LineIssue lineIssue, PhpFile file) {
    return verifier.reportIssue(path(file), lineIssue.message())
      .onLine(lineIssue.line());
  }

  private static MultiFileVerifier.Issue addFileIssue(MultiFileVerifier verifier, FileIssue fileIssue, PhpFile file) {
    return verifier.reportIssue(path(file), fileIssue.message())
      .onFile();
  }

  private static MultiFileVerifier.Issue addPreciseIssue(MultiFileVerifier verifier, PreciseIssue preciseIssue, PhpFile file) {
    IssueLocation location = preciseIssue.primaryLocation();
    String message = location.message();
    requireNonNull(message, "Primary location message should never be null.");
    MultiFileVerifier.Issue issueBuilder = verifier.reportIssue(path(file), message)
      .onRange(location.startLine(), location.startLineOffset() + 1, location.endLine(), location.endLineOffset());
    for (IssueLocation secondary : preciseIssue.secondaryLocations()) {
      String filePath = secondary.filePath();
      Path path = filePath == null ? path(file) : (new File(filePath)).toPath();
      issueBuilder.addSecondary(path, secondary.startLine(), secondary.startLineOffset() + 1, secondary.endLine(), secondary.endLineOffset(), secondary.message());
    }
    return issueBuilder;
  }

}
