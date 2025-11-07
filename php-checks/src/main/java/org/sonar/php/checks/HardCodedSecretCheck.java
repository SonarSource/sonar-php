/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
import org.sonarsource.analyzer.commons.EntropyDetector;
import org.sonarsource.analyzer.commons.HumanLanguageDetector;

import static org.sonar.php.checks.HardCodedIpAddressCheck.IP_V4;
import static org.sonar.php.checks.HardCodedIpAddressCheck.IP_V6;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = "S6418")
public class HardCodedSecretCheck extends PHPVisitorCheck {
  private static final String DEFAULT_SECRET_WORDS = "api[_.-]?key,auth,credential,secret,token";
  private static final String DEFAULT_RANDOMNESS_SENSIBILITY = "5.0";
  private static final double LANGUAGE_SCORE_INCREMENT = 0.3;
  private static final int MAX_RANDOMNESS_SENSIBILITY = 10;
  private static final int MINIMUM_CREDENTIAL_LENGTH = 17;

  private static final String FIRST_ACCEPTED_CHARACTER = "[\\w.+/~$:&-]";
  private static final String FOLLOWING_ACCEPTED_CHARACTER = "[=\\w.+/~$:&-]";
  private static final Pattern SECRET_PATTERN = Pattern.compile(FIRST_ACCEPTED_CHARACTER + "(" + FOLLOWING_ACCEPTED_CHARACTER + "|\\\\\\\\" + FOLLOWING_ACCEPTED_CHARACTER + ")++");
  private static final Pattern IP_PATTERN = Pattern.compile("%s|%s".formatted(IP_V4, IP_V6));

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

  private List<Pattern> variablePatterns;
  private List<Pattern> literalPatterns;
  private EntropyDetector entropyDetector;
  private double maxLanguageScore;

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
      visitDefineFunctionCall(tree);
    } else if ("strcasecmp".equals(functionName) || "strcmp".equals(functionName)) {
      visitStringCompareFunctionCall(tree);
    } else if (tree.callArguments().size() == 2) {
      visitTwoArgumentsFunctionCall(tree);
    }
    super.visitFunctionCall(tree);
  }

  private void visitDefineFunctionCall(FunctionCallTree tree) {
    CheckUtils.argumentValue(tree, "constant_name", 0)
      .filter(constantName -> constantName.is(Tree.Kind.REGULAR_STRING_LITERAL))
      .map(LiteralTree.class::cast)
      .ifPresent((LiteralTree constantName) -> CheckUtils.argumentValue(tree, "value", 1)
        .filter(value -> value.is(Tree.Kind.REGULAR_STRING_LITERAL))
        .map(LiteralTree.class::cast)
        .ifPresent(value -> detectSecret(trimQuotes(constantName.value()), trimQuotes(value.value()), value)));
  }

  private void visitStringCompareFunctionCall(FunctionCallTree tree) {
    var string1 = CheckUtils.resolvedArgumentLiteral(tree, "string1", 0);
    var string2 = CheckUtils.resolvedArgumentLiteral(tree, "string2", 1);
    if (string1.isPresent() && tree.callArguments().size() == 2) {
      var callArg = tree.callArguments().get(1).value();
      if (callArg instanceof VariableIdentifierTree variableIdentifier) {
        detectSecret(variableIdentifier.text(), string1.get().value(), string1.get());
      }
    }
    if (string2.isPresent() && tree.callArguments().size() == 2) {
      var callArg = tree.callArguments().get(0).value();
      if (callArg instanceof VariableIdentifierTree variableIdentifier) {
        detectSecret(variableIdentifier.text(), string2.get().value(), string2.get());
      }
    }
  }

  private void visitTwoArgumentsFunctionCall(FunctionCallTree tree) {
    var firstArg = tree.callArguments().get(0).value();
    var secondArg = tree.callArguments().get(1).value();
    if (firstArg instanceof LiteralTree firstLiteralTree && secondArg instanceof LiteralTree secondLiteralTree) {
      detectSecret(firstLiteralTree.value(), secondLiteralTree.value(), secondLiteralTree);
      detectSecret(secondLiteralTree.value(), firstLiteralTree.value(), firstLiteralTree);
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    var variableIdentifier = tree.variable();
    if (variableIdentifier instanceof VariableIdentifierTree identifier) {
      var valueTree = tree.value();
      if (valueTree instanceof LiteralTree literalTree) {
        detectSecret(identifier.text(), literalTree.value(), literalTree);
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
    if (tree.key() instanceof LiteralTree keyTree && tree.value() instanceof LiteralTree valueTree) {
      detectSecret(keyTree.value(), valueTree.value(), valueTree);
    }
    super.visitArrayPair(tree);
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    var leftOperand = tree.leftOperand();
    var rightOperand = tree.rightOperand();
    if (rightOperand instanceof LiteralTree secretValueTree && leftOperand instanceof VariableIdentifierTree variableTree) {
      detectSecret(variableTree.text(), secretValueTree.value(), rightOperand);
    }
    if (leftOperand instanceof LiteralTree secretValueTree && rightOperand instanceof VariableIdentifierTree variableTree) {
      detectSecret(variableTree.text(), secretValueTree.value(), leftOperand);
    }
    super.visitBinaryExpression(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    var literal = trimQuotes(tree.value());
    literalPatterns().map(pattern -> pattern.matcher(literal))
      .filter(Matcher::find)
      .filter(matcher -> !isExcludedLiteral(matcher.group("suffix")))
      .findAny()
      .ifPresent(matcher -> reportIssue(tree, matcher.group(1)));
    super.visitLiteral(tree);
  }

  private void detectSecret(String identifierName, String secretValue, Tree tree) {
    var identifier = trimQuotes(identifierName);
    var secret = trimQuotes(secretValue);
    getSecretLikeName(identifier).ifPresent((String secretName) -> {
      if (isSecret(secret)) {
        reportIssue(tree, secretName);
      }
    });
  }

  private void reportIssue(Tree tree, String secretName) {
    newIssue(tree, "'%s' detected in this expression, review this potentially hard-coded secret.".formatted(secretName));
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

  private Stream<Pattern> literalPatterns() {
    if (literalPatterns == null) {
      literalPatterns = toPatterns("=\\s*+(?<suffix>[^\\\\ &;#,|]+)");
    }
    return literalPatterns.stream();
  }

  private List<Pattern> toPatterns(String suffix) {
    return Stream.of(secretWords.split(","))
      .map(String::trim)
      .map(word -> Pattern.compile("(" + word + ")" + suffix, Pattern.CASE_INSENSITIVE))
      .toList();
  }

  private boolean isSecret(String literal) {
    if (literal.length() < MINIMUM_CREDENTIAL_LENGTH || !SECRET_PATTERN.matcher(literal).matches()) {
      return false;
    }
    return isRandom(literal) && isNotIpV6(literal);
  }

  private boolean isRandom(String literal) {
    return entropyDetector().hasEnoughEntropy(literal) && HumanLanguageDetector.humanLanguageScore(literal) < maxLanguageScore();
  }

  private static boolean isNotIpV6(String literal) {
    return !IP_PATTERN.matcher(literal).matches();
  }

  private static boolean isExcludedLiteral(String followingString) {
    return !isPotentialCredential(followingString)
      || followingString.startsWith("?")
      || followingString.startsWith(":")
      || followingString.contains("%s");
  }

  private static boolean isPotentialCredential(String literal) {
    String trimmed = literal.trim();
    return trimmed.length() >= MINIMUM_CREDENTIAL_LENGTH;
  }

  private EntropyDetector entropyDetector() {
    if (entropyDetector == null) {
      entropyDetector = new EntropyDetector(randomnessSensibility);
    }
    return entropyDetector;
  }

  private double maxLanguageScore() {
    if (maxLanguageScore == 0.0) {
      maxLanguageScore = (MAX_RANDOMNESS_SENSIBILITY - randomnessSensibility) * LANGUAGE_SCORE_INCREMENT;
    }
    return maxLanguageScore;
  }
}
