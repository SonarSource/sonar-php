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

import java.io.File;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class PHPCheckVerifierTest {

  @Test
  public void no_issue() throws Exception {
    PHPCheckVerifier.verifyNoIssue(new File("src/test/resources/tests/no-issue.php"),
      new PHPVisitorCheck() {
      });
  }

  @Test
  public void legacy_file_issue() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/file-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newIssue(this, "File issue").cost(2.5d);
        }
      });
  }

  @Test(expected = ComparisonFailure.class)
  public void legacy_file_issue_wrong_message() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/file-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newIssue(this, "Wrong message").cost(2.5d);
        }
      });
  }

  @Test(expected = ComparisonFailure.class)
  public void legacy_file_issue_wrong_cost() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/file-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newIssue(this, "File issue").cost(5.2d);
        }
      });
  }

  @Test(expected = IllegalStateException.class)
  public void legacy_file_issue_wrong_check() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/file-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          PHPVisitorCheck otherCheck = new PHPVisitorCheck() {
          };
          context().newIssue(otherCheck, "File issue").cost(2.5d);
        }
      });
  }

  @Test
  public void legacy_line_issue() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/line-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newIssue(this, "Line issue").line(2);
        }
      });
  }

  @Test(expected = ComparisonFailure.class)
  public void legacy_line_issue_wrong_message() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/line-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newIssue(this, "Wrong message").line(2);
        }
      });
  }

  @Test
  public void file_issue() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/file-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newFileIssue(this, "File issue").cost(2.5d);
        }
      });
  }

  @Test(expected = ComparisonFailure.class)
  public void file_issue_wrong_message() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/file-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newFileIssue(this, "Wrong message").cost(2.5d);
        }
      });
  }

  @Test
  public void line_issue() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/line-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newLineIssue(this, 2, "Line issue");
        }
      });
  }

  @Test(expected = ComparisonFailure.class)
  public void line_issue_wrong_message() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/line-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitCompilationUnit(CompilationUnitTree tree) {
          context().newLineIssue(this, 2, "Wrong message");
        }
      });
  }

  @Test
  public void precise_issue() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/precise-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitFunctionCall(FunctionCallTree tree) {
          SyntaxToken echoToken = ((NamespaceNameTree) tree.callee()).name().token();
          SyntaxToken literalToken = ((LiteralTree) tree.arguments().get(0)).token();
          context().newIssue(this, echoToken, "Precise issue")
            .secondary(literalToken, "Secondary");
          super.visitFunctionCall(tree);
        }
      });
  }

  @Test(expected = ComparisonFailure.class)
  public void precise_issue_wrong_secondary_message() throws Exception {
    PHPCheckVerifier.verify(new File("src/test/resources/tests/precise-issue.php"),
      new PHPVisitorCheck() {
        @Override
        public void visitFunctionCall(FunctionCallTree tree) {
          SyntaxToken echoToken = ((NamespaceNameTree) tree.callee()).name().token();
          SyntaxToken literalToken = ((LiteralTree) tree.arguments().get(0)).token();
          context().newIssue(this, echoToken, "Precise issue")
            .secondary(literalToken, "Wrong message");
          super.visitFunctionCall(tree);
        }
      });
  }

  @Test
  public void ignore_expected_issues() throws Exception {
    class CustomVerifier extends PHPCheckVerifier {
      private CustomVerifier() {
        super(false);
      }

      private void verifyNoIssueIgnoringExpected(File sourceFile, PHPCheck check) {
        createVerifier(sourceFile, check).assertNoIssues();
      }
    }
    new CustomVerifier().verifyNoIssueIgnoringExpected(new File("src/test/resources/tests/precise-issue.php"),
      new PHPVisitorCheck() {
      });
  }

}
