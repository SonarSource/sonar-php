/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.ReadWriteUsages;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.utils.collections.MapBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.isStringLiteralWithValue;

@Rule(key = WeakSSLProtocolCheck.KEY)
public class WeakSSLProtocolCheck extends PHPVisitorCheck {
  public static final String KEY = "S4423";

  private static final String STREAM_CONTEXT_CREATE = "stream_context_create";
  private static final String STREAM_SOCKET_ENABLE_CRYPTO = "stream_socket_enable_crypto";
  private static final String CURL_SETOPT = "curl_setopt";
  private static final String CRYPTO_METHOD_KEY = "crypto_method";

  private static final Map<String, List<String>> STREAM_WEAK_PROTOCOLS = MapBuilder.<String, List<String>>builder()
    .put(STREAM_CONTEXT_CREATE, Arrays.asList(
      "STREAM_CRYPTO_METHOD_ANY_CLIENT",
      "STREAM_CRYPTO_METHOD_ANY_SERVER",
      "STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT",
      "STREAM_CRYPTO_METHOD_TLSv1_0_SERVER",
      "STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT",
      "STREAM_CRYPTO_METHOD_TLSv1_1_SERVER"))
    .put(STREAM_SOCKET_ENABLE_CRYPTO, Arrays.asList(
      "STREAM_CRYPTO_METHOD_SSLv2_CLIENT",
      "STREAM_CRYPTO_METHOD_SSLv3_CLIENT",
      "STREAM_CRYPTO_METHOD_SSLv23_CLIENT",
      "STREAM_CRYPTO_METHOD_ANY_CLIENT",
      "STREAM_CRYPTO_METHOD_TLS_CLIENT",
      "STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT",
      "STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT",
      "STREAM_CRYPTO_METHOD_SSLv2_SERVER",
      "STREAM_CRYPTO_METHOD_SSLv3_SERVER",
      "STREAM_CRYPTO_METHOD_SSLv23_SERVER",
      "STREAM_CRYPTO_METHOD_ANY_SERVER",
      "STREAM_CRYPTO_METHOD_TLS_SERVER",
      "STREAM_CRYPTO_METHOD_TLSv1_0_SERVER",
      "STREAM_CRYPTO_METHOD_TLSv1_1_SERVER"))
    .build();

  private static final List<String> CURL_WEAK_PROTOCOLS = Arrays.asList(
    "CURL_SSLVERSION_TLSv1",
    "CURL_SSLVERSION_SSLv2",
    "CURL_SSLVERSION_SSLv3",
    "CURL_SSLVERSION_TLSv1_0",
    "CURL_SSLVERSION_TLSv1_1");

  private static final String MESSAGE = "Change this code to use a stronger protocol.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);

    if (STREAM_CONTEXT_CREATE.equals(functionName)) {
      CheckUtils.argument(tree, "options", 0).ifPresent(
        options -> checkStreamSSLConfig(options.value()));
    }
    if (STREAM_SOCKET_ENABLE_CRYPTO.equals(functionName)) {
      CheckUtils.argument(tree, "crypto_type", 2).ifPresent(
        cryptoType -> checkStreamWeakProtocol(getAssignedValue(cryptoType.value()), STREAM_SOCKET_ENABLE_CRYPTO));
    }
    if (CURL_SETOPT.equals(functionName)) {
      Optional<CallArgumentTree> optionArgument = CheckUtils.argument(tree, "option", 1);
      Optional<CallArgumentTree> valueArgument = CheckUtils.argument(tree, "value", 2);
      if (optionArgument.isPresent() && valueArgument.isPresent()) {
        ExpressionTree optionArgumentValue = optionArgument.get().value();
        if (optionArgumentValue.is(Tree.Kind.NAMESPACE_NAME) && "CURLOPT_SSLVERSION".equals(((NamespaceNameTree) optionArgumentValue).name().text())) {
          checkCURLWeakProtocol(getAssignedValue(valueArgument.get().value()));
        }
      }
    }
    super.visitFunctionCall(tree);
  }

  private void checkStreamSSLConfig(ExpressionTree expressionTree) {
    var enclosingBlock = TreeUtils.findAncestorWithKind(expressionTree, Tree.Kind.BLOCK);
    if (enclosingBlock != null) {
      var usages = new ReadWriteUsages(enclosingBlock, context().symbolTable());
      var hasSensitiveReassignment = checkOptionsReassignment(usages, expressionTree);
      if (hasSensitiveReassignment) {
        // sensitive reassignment detected, no need to check the initial assignment
        return;
      }
    }

    ExpressionTree config = getAssignedValue(expressionTree);
    if (!isArrayInitializer(config)) {
      return;
    }
    getProperty((ArrayInitializerTree) config, "SSL")
      .flatMap(sslConfig -> {
        if (isArrayInitializer(sslConfig)) {
          return getProperty((ArrayInitializerTree) sslConfig, CRYPTO_METHOD_KEY);
        }
        return Optional.empty();
      })
      .ifPresent(value -> checkStreamWeakProtocol(value, STREAM_CONTEXT_CREATE));
  }

  private boolean checkOptionsReassignment(ReadWriteUsages usages, ExpressionTree expressionTree) {
    var symbol = context().symbolTable().getSymbol(expressionTree);
    if (symbol == null) {
      return false;
    }

    var optionsSslAccesses = usages.getWritesSorted(symbol).stream()
      .map(SyntaxToken::getParent)
      .filter(tree -> isArrayObjectWithOffset(tree, "ssl"))
      .map(Tree::getParent)
      .toList();

    // In a sensitive case, `$options['ssl'] is either assigned with an array initializer or a value is directly written by an array key
    if (hasOptionsSslSensitiveAssignment(optionsSslAccesses)) {
      // `$options['ssl']` is assigned with a sensitive array initializer
      return true;
    }

    // check if `$options['ssl']['crypto_method']` is set directly
    return optionsSslAccesses.stream()
      .filter(tree -> isArrayObjectWithOffset(tree, CRYPTO_METHOD_KEY))
      .map(Tree::getParent)
      .map(WeakSSLProtocolCheck::getAssignedValueFromParent)
      .filter(Objects::nonNull)
      .findFirst()
      .map(tree -> checkStreamWeakProtocol(tree, STREAM_CONTEXT_CREATE))
      .orElse(false);
  }

  private static boolean isArrayInitializer(ExpressionTree param) {
    return param.is(Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION);
  }

  private boolean hasOptionsSslSensitiveAssignment(List<Tree> optionsSslAccesses) {
    return optionsSslAccesses.stream()
      .map(WeakSSLProtocolCheck::getAssignedValueFromParent)
      .filter(param -> param != null && isArrayInitializer(param))
      .findFirst()
      .flatMap(c -> getProperty((ArrayInitializerTree) c, CRYPTO_METHOD_KEY))
      .map(tree -> checkStreamWeakProtocol(tree, STREAM_CONTEXT_CREATE))
      .orElse(false);
  }

  private boolean checkStreamWeakProtocol(ExpressionTree expressionTree, String functionName) {
    Stream<ExpressionTree> protocols = expressionTree.is(Tree.Kind.BITWISE_OR)
      ? getOperands((BinaryExpressionTree) expressionTree)
      : Stream.of(expressionTree);
    List<String> weakProtocols = STREAM_WEAK_PROTOCOLS.get(functionName);
    if (weakProtocols != null) {
      return protocols.map((ExpressionTree protocol) -> {
        if (protocol.is(Tree.Kind.NAMESPACE_NAME)) {
          NamespaceNameTree cryptoMethod = (NamespaceNameTree) protocol;
          if (weakProtocols.contains(cryptoMethod.name().text())) {
            context().newIssue(this, cryptoMethod, MESSAGE);
            return true;
          }
        }
        return false;
      })
        .reduce(false, Boolean::logicalOr);
    }
    return false;
  }

  private void checkCURLWeakProtocol(ExpressionTree expressionTree) {

    if (expressionTree.is(Tree.Kind.NAMESPACE_NAME)) {
      NamespaceNameTree protocol = (NamespaceNameTree) expressionTree;
      CURL_WEAK_PROTOCOLS.forEach(weakProtocol -> {
        if (weakProtocol.equals(protocol.name().text())) {
          context().newIssue(this, protocol, MESSAGE);
        }
      });
    }
  }

  private static Stream<ExpressionTree> getOperands(BinaryExpressionTree binaryExpressionTree) {
    if (binaryExpressionTree.leftOperand().is(Tree.Kind.BITWISE_OR)) {
      return Stream.concat(
        Stream.of(binaryExpressionTree.rightOperand()),
        getOperands((BinaryExpressionTree) binaryExpressionTree.leftOperand()));
    }
    return Stream.of(binaryExpressionTree.leftOperand(), binaryExpressionTree.rightOperand());
  }

  private static Optional<ExpressionTree> getProperty(ArrayInitializerTree params, String property) {
    return params.arrayPairs().stream()
      .filter(pair -> isStringLiteralWithValue(pair.key(), property))
      .map(pair -> getAssignedValue(pair.value()))
      .findFirst();
  }

  private static ExpressionTree getAssignedValue(ExpressionTree value) {
    if (value.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) value).orElse(value);
    }
    return value;
  }

  @Nullable
  private static ExpressionTree getAssignedValueFromParent(@Nullable Tree tree) {
    return Optional.ofNullable(tree)
      .map(Tree::getParent)
      .filter(parent -> parent != null && parent.is(Tree.Kind.ASSIGNMENT))
      .map(parent -> ((AssignmentExpressionTree) parent).value())
      .orElse(null);
  }

  private static boolean isArrayObjectWithOffset(@Nullable Tree tree, String offset) {
    if (tree == null) {
      return false;
    }
    return tree.getParent() instanceof ArrayAccessTree arrayAccessTree &&
      isStringLiteralWithValue(arrayAccessTree.offset(), offset);
  }
}
