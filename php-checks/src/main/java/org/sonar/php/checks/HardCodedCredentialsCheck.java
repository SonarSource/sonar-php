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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = HardCodedCredentialsCheck.KEY)
public class HardCodedCredentialsCheck extends PHPVisitorCheck {

  public static final String KEY = "S2068";
  private static final String MESSAGE = "'%s' detected in this variable name, review this potentially hardcoded credential.";
  private static final String DEFAULT_CREDENTIAL_WORDS = "password,passwd,pwd";

  @RuleProperty(
    key = "credentialWords",
    description = "Comma separated list of words identifying potential credentials",
    defaultValue = DEFAULT_CREDENTIAL_WORDS)
  public String credentialWords = DEFAULT_CREDENTIAL_WORDS;

  private List<Pattern> variablePatterns = null;
  private List<Pattern> literalPatterns = null;

  private Stream<Pattern> variablePatterns() {
    if (variablePatterns == null) {
      variablePatterns = toPatterns("");
    }
    return variablePatterns.stream();
  }

  private Stream<Pattern> literalPatterns() {
    if (literalPatterns == null) {
      literalPatterns = toPatterns("=..");
    }
    return literalPatterns.stream();
  }

  private List<Pattern> toPatterns(String suffix) {
    return Stream.of(credentialWords.split(","))
      .map(String::trim)
      .map(word -> Pattern.compile(word + suffix, Pattern.CASE_INSENSITIVE))
      .collect(Collectors.toList());
  }

  @Override
  public void visitLiteral(LiteralTree literal) {
    if (literal.is(Kind.REGULAR_STRING_LITERAL)) {
      checkCredential(literal, literal.token().text(), literalPatterns());
    }
    super.visitLiteral(literal);
  }

  @Override
  public void visitVariableDeclaration(VariableDeclarationTree declaration) {
    checkVariable((declaration.identifier()).token(), declaration.initValue());
    super.visitVariableDeclaration(declaration);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree assignment) {
    checkVariable(((PHPTree) assignment.variable()).getLastToken(), assignment.value());
    super.visitAssignmentExpression(assignment);
  }

  private void checkVariable(SyntaxToken reportTree, @Nullable Tree assignedValue) {
    if (assignedValue != null && assignedValue.is(Kind.REGULAR_STRING_LITERAL)) {
      checkCredential(reportTree, reportTree.text(), variablePatterns());
    }
  }

  private void checkCredential(Tree reportTree, String target, Stream<Pattern> patterns) {
    patterns.filter(pattern -> pattern.matcher(target).find()).findAny().ifPresent(pattern -> addIssue(pattern, reportTree));

  }

  private void addIssue(Pattern pattern, Tree tree) {
    context().newIssue(this, tree, String.format(MESSAGE, cleanedPattern(pattern.pattern())));
  }

  private static String cleanedPattern(String pattern) {
    if (pattern.endsWith("=..")) {
      return pattern.substring(0, pattern.length() - 3);
    }
    return pattern;
  }

}
