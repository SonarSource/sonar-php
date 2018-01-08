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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.recognizer.CodeRecognizer;
import org.sonar.squidbridge.recognizer.ContainsDetector;
import org.sonar.squidbridge.recognizer.Detector;
import org.sonar.squidbridge.recognizer.EndWithDetector;
import org.sonar.squidbridge.recognizer.KeywordsDetector;
import org.sonar.squidbridge.recognizer.LanguageFootprint;

@Rule(
  key = CommentedOutCodeCheck.KEY)
public class CommentedOutCodeCheck extends PHPVisitorCheck {

  public static final String KEY = "S125";
  private static final String MESSAGE = "Remove this commented out code.";

  private static final double THRESHOLD = 0.9;

  private final CodeRecognizer codeRecognizer = new CodeRecognizer(THRESHOLD, new PHPRecognizer());
  private final Pattern regexpToDivideStringByLine = Pattern.compile("(\r?\n)|(\r)");

  private SyntaxTrivia previousTrivia;

  private static class PHPRecognizer implements LanguageFootprint {

    @Override
    public Set<Detector> getDetectors() {
      return ImmutableSet.of(
        new EndWithDetector(0.95, '}', ';', '{'),
        new KeywordsDetector(0.3, PHPKeyword.getKeywordValues()),
        new ContainsDetector(0.95, "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", "&=", "^=", "|="),
        new ContainsDetector(0.95, "!=", "!=="));
    }

  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    previousTrivia = null;
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitTrivia(SyntaxTrivia trivia) {
    super.visitTrivia(trivia);

    if (isInlineComment(trivia)) {

      if (isCommentedCode(getContents(trivia.text())) && !previousLineIsCommentedCode(trivia, previousTrivia)) {
        reportIssue(trivia.line());
      }

    } else if (!isPHPDoc(trivia)) {
      String[] lines = regexpToDivideStringByLine.split(getContents(trivia.text()));

      for (int lineOffset = 0; lineOffset < lines.length; lineOffset++) {
        if (isCommentedCode(lines[lineOffset])) {
          reportIssue(trivia.line() + lineOffset);
          break;
        }
      }
    }
    previousTrivia = trivia;
  }

  private void reportIssue(int line) {
    context().newLineIssue(this, line, MESSAGE);
  }

  private boolean previousLineIsCommentedCode(SyntaxTrivia trivia, @Nullable SyntaxTrivia previousTrivia) {
    return previousTrivia != null && (trivia.line() == previousTrivia.line() + 1)
      && isCommentedCode(previousTrivia.text());
  }

  private boolean isCommentedCode(String line) {
    return codeRecognizer.isLineOfCode(line);
  }

  private static boolean isInlineComment(SyntaxTrivia trivia) {
    return trivia.text().startsWith("//");
  }

  private static boolean isPHPDoc(SyntaxTrivia trivia) {
    return trivia.text().startsWith("/**");
  }

  private static String getContents(String comment) {
    if (comment.startsWith("//")) {
      return comment.substring(2);
    } else if (comment.charAt(0) == '#') {
      return comment.substring(1);
    } else {
      return comment.substring(2, comment.length() - 2);
    }
  }

}
