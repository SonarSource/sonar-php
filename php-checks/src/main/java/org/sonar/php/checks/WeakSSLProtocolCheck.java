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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.isStringLiteralWithValue;

@Rule(key = WeakSSLProtocolCheck.KEY)
public class WeakSSLProtocolCheck extends PHPVisitorCheck {
  public static final String KEY = "S4423";

  private static final String STREAM_CONTEXT_CREATE = "stream_context_create";
  private static final String STREAM_SOCKET_ENABLE_CRYPTO = "stream_socket_enable_crypto";
  private static final String CURL_SETOPT = "curl_setopt";

  private static final Map<String, List<String>> STREAM_WEAK_PROTOCOLS = ImmutableMap.of(
    STREAM_CONTEXT_CREATE, Arrays.asList(
      "STREAM_CRYPTO_METHOD_ANY_CLIENT",
      "STREAM_CRYPTO_METHOD_ANY_SERVER",
      "STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT",
      "STREAM_CRYPTO_METHOD_TLSv1_0_SERVER",
      "STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT",
      "STREAM_CRYPTO_METHOD_TLSv1_1_SERVER"),
    STREAM_SOCKET_ENABLE_CRYPTO, Arrays.asList(
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
      "STREAM_CRYPTO_METHOD_TLSv1_1_SERVER"));

  private static final List<String> CURL_WEAK_PROTOCOLS = ImmutableList.of(
    "CURL_SSLVERSION_TLSv1",
    "CURL_SSLVERSION_SSLv2",
    "CURL_SSLVERSION_SSLv3",
    "CURL_SSLVERSION_TLSv1_0",
    "CURL_SSLVERSION_TLSv1_1");

  private static final String MESSAGE = "Change this code to use a stronger protocol.";
  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getFunctionName(tree);
    List<ExpressionTree> arguments = tree.arguments();
    if (STREAM_CONTEXT_CREATE.equals(functionName) && !arguments.isEmpty()) {
      checkStreamSSLConfig(arguments.get(0));
    }
    if (STREAM_SOCKET_ENABLE_CRYPTO.equals(functionName) && arguments.size() > 2) {
      checkStreamWeakProtocol(getAssignedValue(arguments.get(2)), STREAM_SOCKET_ENABLE_CRYPTO);
    }
    if (CURL_SETOPT.equals(functionName) && arguments.size() > 2) {
      ExpressionTree optionArgument = arguments.get(1);
      if (optionArgument.is(Tree.Kind.NAMESPACE_NAME) && "CURLOPT_SSLVERSION".equals(((NamespaceNameTree) optionArgument).name().text())) {
        checkCURLWeakProtocol(getAssignedValue(arguments.get(2)));
      }
    }
    super.visitFunctionCall(tree);
  }

  private void checkStreamSSLConfig(ExpressionTree expressionTree) {
    ExpressionTree config = getAssignedValue(expressionTree);
    if (!isArrayInitializer(config)) {
      return;
    }
    getProperty((ArrayInitializerTree) config, "SSL")
      .flatMap(sslConfig -> {
        if (isArrayInitializer(sslConfig)) {
          return getProperty((ArrayInitializerTree) sslConfig, "crypto_method");
        }
        return Optional.empty();
      })
      .ifPresent(value -> checkStreamWeakProtocol(value, STREAM_CONTEXT_CREATE));
  }

  private static boolean isArrayInitializer(ExpressionTree param) {
    return param.is(Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION);
  }

  private void checkStreamWeakProtocol(ExpressionTree expressionTree, String functionName) {
    Stream<ExpressionTree> protocols = expressionTree.is(Tree.Kind.BITWISE_OR)
      ? getOperands((BinaryExpressionTree) expressionTree)
      : Stream.of(expressionTree);
    List<String> weakProtocols = STREAM_WEAK_PROTOCOLS.get(functionName);
    if (weakProtocols != null) {
      protocols.forEach(protocol -> {
        if (protocol.is(Tree.Kind.NAMESPACE_NAME)) {
          NamespaceNameTree cryptoMethod = (NamespaceNameTree) protocol;
          if (weakProtocols.contains(cryptoMethod.name().text())) {
            context().newIssue(this, cryptoMethod, MESSAGE);
          }
        }
      });
    }
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

  private Optional<ExpressionTree> getProperty(ArrayInitializerTree params, String property) {
    return params.arrayPairs().stream()
      .filter(pair -> isStringLiteralWithValue(pair.key(), property))
      .map(pair -> getAssignedValue(pair.value()))
      .findFirst();
  }

  private ExpressionTree getAssignedValue(ExpressionTree value) {
    if (value.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol valueSymbol = context().symbolTable().getSymbol(value);
      return assignmentExpressionVisitor
        .getUniqueAssignedValue(valueSymbol)
        .orElse(value);
    }
    return value;
  }
}
