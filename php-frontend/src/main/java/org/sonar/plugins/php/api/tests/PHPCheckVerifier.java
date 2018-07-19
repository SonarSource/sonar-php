/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.base.Preconditions;
import com.sonar.sslr.api.typed.ActionParser;
import com.sonarsource.checks.verifier.SingleFileVerifier;
import java.io.File;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.visitors.LegacyIssue;
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

import static java.nio.charset.StandardCharsets.UTF_8;

public class PHPCheckVerifier {

  private static final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  private final boolean readExpectedIssuesFromComments;

  /**
   * Internal use only. Subject to changes.
   */
  protected PHPCheckVerifier(boolean readExpectedIssuesFromComments) {
    this.readExpectedIssuesFromComments = readExpectedIssuesFromComments;
  }

  public static void verify(File sourceFile, PHPCheck check) {
    new PHPCheckVerifier(true).createVerifier(sourceFile, check).assertOneOrMoreIssues();
  }

  public static void verifyNoIssue(File sourceFile, PHPCheck check) {
    new PHPCheckVerifier(true).createVerifier(sourceFile, check).assertNoIssues();
  }

  /**
   * @deprecated since 2.14. Use {@link PHPCheckVerifier#verify(File, PHPCheck)}}
   */
  @Deprecated
  public static void verify(PhpFile sourceFile, PHPCheck check) {
    new PHPCheckVerifier(true).createVerifier(sourceFile, check).assertOneOrMoreIssues();
  }

  /**
   * @deprecated since 2.14. Use {@link PHPCheckVerifier#verifyNoIssue(File, PHPCheck)}}
   */
  @Deprecated
  public static void verifyNoIssue(PhpFile sourceFile, PHPCheck check) {
    new PHPCheckVerifier(true).createVerifier(sourceFile, check).assertNoIssues();
  }

  /**
   * Internal use only. Subject to changes.
   * @deprecated since 2.14
   */
  @Deprecated
  protected SingleFileVerifier createVerifier(PhpFile file, PHPCheck check) {
    SingleFileVerifier verifier = SingleFileVerifier.create(file.relativePath(), UTF_8);
    return getSingleFileVerifier(check, verifier, file);
  }

  /**
   * Internal use only. Subject to changes.
   */
  protected SingleFileVerifier createVerifier(File file, PHPCheck check) {
    SingleFileVerifier verifier = SingleFileVerifier.create(file.toPath(), UTF_8);
    PhpTestFile phpFile = new PhpTestFile(file);
    return getSingleFileVerifier(check, verifier, phpFile);
  }

  private SingleFileVerifier getSingleFileVerifier(PHPCheck check, SingleFileVerifier verifier, PhpFile phpFile) {
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(phpFile.contents());
    check.init();
    for (PhpIssue issue : check.analyze(phpFile, tree)) {
      if (!issue.check().equals(check)) {
        throw new IllegalStateException("Verifier support only one kind of issue " + issue.check() + " != " + check);
      }
      addIssue(verifier, issue).withGap(issue.cost());
    }
    if (readExpectedIssuesFromComments) {
      PHPVisitorCheck commentVisitor = new PHPVisitorCheck() {
        @Override
        public void visitTrivia(SyntaxTrivia trivia) {
          super.visitTrivia(trivia);
          int suffixLength = trivia.text().startsWith("//") ? 0 : 2;
          verifier.addComment(trivia.line(), trivia.column() + 1, trivia.text(), 2, suffixLength);
        }
      };
      commentVisitor.analyze(phpFile, tree);
    }
    return verifier;
  }

  private static SingleFileVerifier.Issue addIssue(SingleFileVerifier verifier, PhpIssue issue) {
    if (issue instanceof LegacyIssue) {
      return addLegacyIssue(verifier, (LegacyIssue) issue);
    } else if (issue instanceof LineIssue) {
      return addLineIssue(verifier, (LineIssue) issue);
    } else if (issue instanceof FileIssue) {
      return addFileIssue(verifier, (FileIssue) issue);
    } else {
      return addPreciseIssue(verifier, (PreciseIssue) issue);
    }
  }

  private static SingleFileVerifier.Issue addLegacyIssue(SingleFileVerifier verifier, LegacyIssue legacyIssue) {
    if (legacyIssue.line() > 0) {
      return verifier.reportIssue(legacyIssue.message())
        .onLine(legacyIssue.line());
    } else {
      return verifier.reportIssue(legacyIssue.message())
        .onFile();
    }
  }

  private static SingleFileVerifier.Issue addLineIssue(SingleFileVerifier verifier, LineIssue lineIssue) {
    return verifier.reportIssue(lineIssue.message())
      .onLine(lineIssue.line());
  }

  private static SingleFileVerifier.Issue addFileIssue(SingleFileVerifier verifier, FileIssue fileIssue) {
    return verifier.reportIssue(fileIssue.message())
      .onFile();
  }

  private static SingleFileVerifier.Issue addPreciseIssue(SingleFileVerifier verifier, PreciseIssue preciseIssue) {
    IssueLocation location = preciseIssue.primaryLocation();
    String message = location.message();
    Preconditions.checkNotNull(message, "Primary location message should never be null.");
    SingleFileVerifier.Issue issueBuilder = verifier.reportIssue(message)
      .onRange(location.startLine(), location.startLineOffset() + 1, location.endLine(), location.endLineOffset());
    for (IssueLocation secondary : preciseIssue.secondaryLocations()) {
      issueBuilder.addSecondary(secondary.startLine(), secondary.startLineOffset() + 1, secondary.endLine(), secondary.endLineOffset(), secondary.message());
    }
    return issueBuilder;
  }

}
