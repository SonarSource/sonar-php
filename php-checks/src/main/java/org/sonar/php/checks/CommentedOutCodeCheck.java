/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.squidbridge.recognizer.CodeRecognizer;
import org.sonar.squidbridge.recognizer.ContainsDetector;
import org.sonar.squidbridge.recognizer.Detector;
import org.sonar.squidbridge.recognizer.EndWithDetector;
import org.sonar.squidbridge.recognizer.KeywordsDetector;
import org.sonar.squidbridge.recognizer.LanguageFootprint;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Set;
import java.util.regex.Pattern;

@Rule(
  key = "S125",
  name = "Sections of code should not be \"commented out\"",
  priority = Priority.MAJOR,
  tags = {Tags.UNUSED, Tags.MISRA})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class CommentedOutCodeCheck extends SquidCheck<LexerlessGrammar> implements AstAndTokenVisitor {

  private static final double THRESHOLD = 0.9;

  private final CodeRecognizer codeRecognizer = new CodeRecognizer(THRESHOLD, new PHPRecognizer());
  private final Pattern regexpToDivideStringByLine = Pattern.compile("(\r?\n)|(\r)");

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
  public void visitToken(Token token) {
    Trivia previousTrivia = null;

    for (Trivia trivia : token.getTrivia()) {
      checkTrivia(previousTrivia, trivia);
      previousTrivia = trivia;
    }
  }

  private void checkTrivia(Trivia previousTrivia, Trivia trivia) {
    if (isInlineComment(trivia)) {

      if (isCommentedCode(getContext().getCommentAnalyser().getContents(trivia.getToken().getValue())) && !previousLineIsCommentedCode(trivia, previousTrivia)) {
        reportIssue(trivia.getToken().getLine());
      }

    } else if (!isPHPDoc(trivia)) {
      String[] lines = regexpToDivideStringByLine.split(getContext().getCommentAnalyser().getContents(trivia.getToken().getOriginalValue()));

      for (int lineOffset = 0; lineOffset < lines.length; lineOffset++) {
        if (isCommentedCode(lines[lineOffset])) {
          reportIssue(trivia.getToken().getLine() + lineOffset);
          break;
        }
      }
    }
  }

  private void reportIssue(int line) {
    getContext().createLineViolation(this, "Remove this commented out code.", line);
  }

  private boolean previousLineIsCommentedCode(Trivia trivia, Trivia previousTrivia) {
    return previousTrivia != null && (trivia.getToken().getLine() == previousTrivia.getToken().getLine() + 1)
      && isCommentedCode(previousTrivia.getToken().getValue());
  }

  private boolean isCommentedCode(String line) {
    return codeRecognizer.isLineOfCode(line);
  }

  private boolean isInlineComment(Trivia trivia) {
    return trivia.getToken().getValue().startsWith("//");
  }

  private boolean isPHPDoc(Trivia trivia) {
    return trivia.getToken().getValue().startsWith("/**");
  }

}
