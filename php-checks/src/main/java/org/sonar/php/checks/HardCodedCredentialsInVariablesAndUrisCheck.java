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
package org.sonar.php.checks;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2068")
public class HardCodedCredentialsInVariablesAndUrisCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Detected '%s' in this variable name, review this potentially hardcoded credential.";
  private static final String MESSAGE_URI = "Detected URI with password, review this potentially hardcoded credential.";
  private static final String DEFAULT_CREDENTIAL_WORDS = "password,passwd,pwd";

  private static final String LITERAL_PATTERN_SUFFIX = "=(?!([\\?:']|%s))..";

  private static final int LITERAL_PATTERN_SUFFIX_LENGTH = LITERAL_PATTERN_SUFFIX.length();

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
      literalPatterns = toPatterns(LITERAL_PATTERN_SUFFIX);
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
    checkForCredentialQuery(literal);
    checkForCredentialUri(literal);

    super.visitLiteral(literal);
  }

  private void checkForCredentialQuery(LiteralTree literal) {
    literalPatterns()
      .filter(pattern -> pattern.matcher(literal.token().text()).find())
      .findAny().ifPresent(pattern -> addIssue(pattern, literal));
  }

  private void checkForCredentialUri(LiteralTree literal) {
    String possibleUrl = CheckUtils.trimQuotes(literal.value());
    URI uri;

    try {
      uri = new URI(possibleUrl);
    } catch (URISyntaxException e) {
      return;
    }

    if (uri.getUserInfo() != null) {
      String userInfo = uri.getUserInfo();

      String[] splitUserInfo = userInfo.split(":");
      if (splitUserInfo.length < 2 || splitUserInfo[0].equals(splitUserInfo[1]) || isCommonTestCredential(userInfo)) {
        return;
      }
      context().newIssue(this, literal, MESSAGE_URI);
    }
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
    if (assignedValue != null && assignedValue.is(Kind.REGULAR_STRING_LITERAL) && !isEmptyStringLiteral((LiteralTree) assignedValue)) {
      variablePatterns().filter(pattern -> pattern.matcher(reportTree.text()).find()).findAny().ifPresent(pattern -> checkAssignedValue(pattern, reportTree, assignedValue));
    }
  }

  private void checkAssignedValue(Pattern pattern, SyntaxToken reportTree, Tree assignedValue) {
    if (!pattern.matcher(assignedValue.toString()).find()) {
      addIssue(pattern, reportTree);
    }
  }

  private static boolean isEmptyStringLiteral(LiteralTree literal) {
    return literal.value().substring(1, literal.value().length() - 1).isEmpty();
  }

  private void addIssue(Pattern pattern, Tree tree) {
    context().newIssue(this, tree, String.format(MESSAGE, cleanedPattern(pattern.pattern())));
  }

  private static String cleanedPattern(String pattern) {
    if (pattern.endsWith(LITERAL_PATTERN_SUFFIX)) {
      return pattern.substring(0, pattern.length() - LITERAL_PATTERN_SUFFIX_LENGTH);
    }
    return pattern;
  }

  private static boolean isCommonTestCredential(String userInfo) {
    return "user:password".equals(userInfo)
      || "username:password".equals(userInfo);
  }
}
