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
package org.sonar.php.checks;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.HeredocStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S6418")
public class HardCodedSecretCheck extends PHPVisitorCheck {
  private static final String DEFAULT_SECRET_WORDS = "api[_.-]?key,auth,credential,secret,token";
  private static final String DEFAULT_RANDOMNESS_SENSIBILITY = "5.0";

  @RuleProperty(
    key = "secretWords",
    description = "Comma separated list of words identifying potential secrets",
    defaultValue = DEFAULT_SECRET_WORDS)
  public String secretWords = DEFAULT_SECRET_WORDS;

  @RuleProperty(
    key = "randomnessSensibility",
    description = "Allows to tune the Randomness Sensibility (from 0 to 10)",
    defaultValue = DEFAULT_RANDOMNESS_SENSIBILITY)
  public double randomnessSensibility = Double.parseDouble(DEFAULT_RANDOMNESS_SENSIBILITY);

  private List<Pattern> variablePatterns = null;

  @Override
  public void visitConstDeclaration(ConstantDeclarationTree tree) {
    // TODO remove
    for (Iterator<Tree> it = tree.declarations().elementsAndSeparators(); it.hasNext();) {
      Tree elementsOrSeparator = it.next();
      if (elementsOrSeparator.is(Tree.Kind.VARIABLE_DECLARATION)) {
        var variableDeclaration = (VariableDeclarationTree) elementsOrSeparator;
        visitVariableDeclaration(variableDeclaration);
      }
    }
  }

  @Override
  public void visitVariableDeclaration(VariableDeclarationTree tree) {
    // Pattern.compile("=\\s*+([^\\\\ &;#,|]+)");
    System.out.println("BBBB variable declaration: " + tree.identifier() + "   kind " + tree.getKind());
    if (tree.initValue() instanceof LiteralTree literalTree) {
      detectSecret(tree.identifier().text(), CheckUtils.trimQuotes(literalTree.value()), literalTree);
    }
    if (tree.initValue() instanceof HeredocStringLiteralTree heredoc) {
      for (ExpandableStringCharactersTree heredocLine : heredoc.strings()) {
        detectSecret(tree.identifier().text(), heredocLine.value(), heredocLine);
      }
    }
  }

  @Override
  public void visitFunctionCall(FunctionCallTree functionCall) {
    if ("define".equals(CheckUtils.getLowerCaseFunctionName(functionCall))) {
      CheckUtils.argumentValue(functionCall, "constant_name", 0)
        .filter(constantName -> constantName.is(Tree.Kind.REGULAR_STRING_LITERAL))
        .map(LiteralTree.class::cast)
        .ifPresent(constantName -> {
          // TODO remove
          System.out.println("DDDD constantName " + constantName.value());
          CheckUtils.argumentValue(functionCall, "value", 1)
            .filter(value -> value.is(Tree.Kind.REGULAR_STRING_LITERAL))
            .map(LiteralTree.class::cast)
            .ifPresent(value -> {
              // TODO remove
              System.out.println("DDDD value: " + value.value());
              detectSecret(CheckUtils.trimQuotes(constantName.value()), CheckUtils.trimQuotes(value.value()), value);
            });

        });
    }
    super.visitFunctionCall(functionCall);
  }

  private void detectSecret(String identifierName, String secret, Tree tree) {
    // TODO remove
    // System.out.println("AAAA detectSecret " + identifierName);
    getSecretLikeName(identifierName).ifPresent(secretName -> {
      if (secret.equals("abcdefghijklmnopqrs")) {
        newIssue(tree, "'%s' detected in this expression, review this potentially hard-coded secret.".formatted(secretName));
      }
    });
  }

  private Optional<String> getSecretLikeName(String identifierName) {
    if (identifierName.isBlank()) {
      return Optional.empty();
    }
    return variableSecretPatterns()
      .map(pattern -> pattern.matcher(identifierName))
      .filter(Matcher::find)
      .map(matcher -> matcher.group(1))
      .findAny();
  }

  private Stream<Pattern> variableSecretPatterns() {
    if (variablePatterns == null) {
      variablePatterns = toPatterns("");
    }
    return variablePatterns.stream();
  }

  private List<Pattern> toPatterns(String suffix) {
    return Stream.of(secretWords.split(","))
      .map(String::trim)
      .map(word -> Pattern.compile("(" + word + ")" + suffix, Pattern.CASE_INSENSITIVE))
      .toList();
  }
}
