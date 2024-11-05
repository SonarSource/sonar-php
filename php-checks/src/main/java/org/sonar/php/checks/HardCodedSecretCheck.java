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

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.HeredocStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

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
  public void visitVariableDeclaration(VariableDeclarationTree tree) {
    if (tree.initValue() instanceof LiteralTree literalTree) {
      detectSecret(tree.identifier().text(), trimQuotes(literalTree.value()), literalTree);
    }
    if (tree.initValue() instanceof HeredocStringLiteralTree heredoc) {
      for (ExpandableStringCharactersTree heredocLine : heredoc.strings()) {
        detectSecret(tree.identifier().text(), heredocLine.value(), heredocLine);
      }
    }
    super.visitVariableDeclaration(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    var functionName = CheckUtils.getLowerCaseFunctionName(tree);
    if ("define".equals(functionName)) {
      CheckUtils.argumentValue(tree, "constant_name", 0)
        .filter(constantName -> constantName.is(Tree.Kind.REGULAR_STRING_LITERAL))
        .map(LiteralTree.class::cast)
        .ifPresent(constantName -> {
          CheckUtils.argumentValue(tree, "value", 1)
            .filter(value -> value.is(Tree.Kind.REGULAR_STRING_LITERAL))
            .map(LiteralTree.class::cast)
            .ifPresent(value -> {
              detectSecret(trimQuotes(constantName.value()), trimQuotes(value.value()), value);
            });

        });
    } else if ("strcasecmp".equals(functionName) || "strcmp".equals(functionName)) {
      var string1 = CheckUtils.resolvedArgumentLiteral(tree, "string1", 0);
      var string2 = CheckUtils.resolvedArgumentLiteral(tree, "string2", 1);
      if (string1.isPresent()) {
        var callArg = tree.callArguments().get(1).value();
        if (callArg instanceof VariableIdentifierTree variableIdentifier) {
          detectSecret(variableIdentifier.text(), string1.get().value(), string1.get());
        }
      }
      if (string2.isPresent()) {
        var callArg = tree.callArguments().get(0).value();
        if (callArg instanceof VariableIdentifierTree variableIdentifier) {
          detectSecret(variableIdentifier.text(), string2.get().value(), string2.get());
        }
      }
    } else if (tree.callArguments().size() == 2) {
      var firstArg = tree.callArguments().get(0).value();
      var secondArg = tree.callArguments().get(1).value();
      if (firstArg instanceof LiteralTree firstLiteralTree) {
        if (secondArg instanceof LiteralTree secondLiteralTree) {
          detectSecret(firstLiteralTree.value(), secondLiteralTree.value(), secondLiteralTree);
          detectSecret(secondLiteralTree.value(), firstLiteralTree.value(), firstLiteralTree);
        }
      }
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    var variableIdentifier = tree.variable();
    if (variableIdentifier instanceof VariableIdentifierTree identifier) {
      var valueTree = tree.value();
      if (valueTree instanceof LiteralTree literalTree) {
        detectSecret(trimQuotes(identifier.text()), trimQuotes(literalTree.value()), literalTree);
      }
    }
    super.visitAssignmentExpression(tree);
  }

  @Override
  public void visitParameter(ParameterTree tree) {
    if (tree.initValue() instanceof LiteralTree valueTree) {
      detectSecret(tree.variableIdentifier().text(), valueTree.value(), valueTree);
    }
    super.visitParameter(tree);
  }

  @Override
  public void visitArrayPair(ArrayPairTree tree) {
    if (tree.key() instanceof LiteralTree keyTree) {
      if (tree.value() instanceof LiteralTree valueTree) {
        detectSecret(trimQuotes(keyTree.value()), trimQuotes(valueTree.value()), valueTree);
      }
    }
    super.visitArrayPair(tree);
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    var leftOperant = tree.leftOperand();
    var rightOperant = tree.rightOperand();
    if (rightOperant instanceof LiteralTree secretValueTree) {
      if (leftOperant instanceof VariableIdentifierTree variableTree) {
        detectSecret(variableTree.text(), secretValueTree.value(), rightOperant);
      }
    }
    if (leftOperant instanceof LiteralTree secretValueTree) {
      if (rightOperant instanceof VariableIdentifierTree variableTree) {
        detectSecret(variableTree.text(), secretValueTree.value(), leftOperant);
      }
    }
    super.visitBinaryExpression(tree);
  }

  private void detectSecret(String identifierName, String secretValue, Tree tree) {
    var identifier = trimQuotes(identifierName);
    var secret = trimQuotes(secretValue);
    getSecretLikeName(identifier).ifPresent(secretName -> {
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
