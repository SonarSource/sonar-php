/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.checks.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.argument;
import static org.sonar.php.checks.utils.CheckUtils.lowerCaseFunctionName;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;
import static org.sonar.php.tree.TreeUtils.firstDescendant;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;
import static org.sonar.plugins.php.api.tree.Tree.Kind.ARRAY_INITIALIZER_BRACKET;
import static org.sonar.plugins.php.api.tree.Tree.Kind.ARRAY_INITIALIZER_FUNCTION;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAMESPACE_NAME;
import static org.sonar.plugins.php.api.tree.Tree.Kind.OBJECT_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.REGULAR_STRING_LITERAL;

@Rule(key = "S5122")
public class CORSPolicyCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure this permissive CORS policy is safe here.";

  @Override
  public void visitCompilationUnit(CompilationUnitTree compilationUnitTree) {
    if ("cors.php".equals(context().getPhpFile().filename())) {
      firstDescendant(compilationUnitTree, ReturnStatementTree.class).ifPresent(this::checkCorsPhpFile);
    }
    super.visitCompilationUnit(compilationUnitTree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree functionCallTree) {
    FunctionCallHelper call = FunctionCallHelper.create(functionCallTree);

    if ((call.isResponseConstuctorVulnerable()) || (call.isCoreHeaderVulnerable()) || (call.isSetOrHeaderVulnerable())) {
      context().newIssue(this, functionCallTree, MESSAGE);
    }
    super.visitFunctionCall(functionCallTree);
  }

  private void checkCorsPhpFile(ReturnStatementTree returnStatementTree) {
    ExpressionTree returnExpression = returnStatementTree.expression();
    if (returnExpression.is(ARRAY_INITIALIZER_BRACKET, ARRAY_INITIALIZER_FUNCTION)) {
      ((ArrayInitializerTree) returnExpression)
        .arrayPairs().stream()
        .filter(pair -> isLiteralTreeEqualsTo(pair.key(), "allowed_origins") && isSensitiveArray(pair.value()))
        .forEach(pair -> context().newIssue(this, pair, MESSAGE));
    }
  }

  private static boolean isSensitiveArray(ExpressionTree arrayValue) {
    return arrayValue.is(ARRAY_INITIALIZER_BRACKET, ARRAY_INITIALIZER_FUNCTION) &&
      ((ArrayInitializerTree) arrayValue)
        .arrayPairs().stream()
        .anyMatch(pair -> isLiteralTreeEqualsTo(pair.value(), "*"));
  }

  private static boolean isLiteralTreeEqualsTo(ExpressionTree tree, String expected) {
    return tree.is(REGULAR_STRING_LITERAL) && expected.equalsIgnoreCase(trimQuotes(((LiteralTree) tree).value().replaceAll("\\s", "")));
  }

  private static class FunctionCallHelper {

    private static final QualifiedName HEADER_FUNCTION_NAME = qualifiedName("header");

    private FunctionCallTree functionCallTree;
    private ExpressionTree callee;

    private static final Set<QualifiedName> RESPONSE_CLASSES = new HashSet<>(Arrays.asList(
      qualifiedName("Symfony\\Component\\Httpfoundation\\Response"),
      qualifiedName("Illuminate\\Http\\Response")));

    private FunctionCallHelper(FunctionCallTree functionCallTree) {
      this.functionCallTree = functionCallTree;
      this.callee = functionCallTree.callee();
    }

    static FunctionCallHelper create(FunctionCallTree functionCallTree) {
      return new FunctionCallHelper(functionCallTree);
    }

    private boolean isResponseConstuctorVulnerable() {
      return isResponseConstructorFunctionCall() &&
        argument(functionCallTree, "headers", 2)
          .map(CallArgumentTree::value)
          .map(CheckUtils::assignedValue)
          .filter(ArrayInitializerTree.class::isInstance)
          .map(ArrayInitializerTree.class::cast)
          .filter(a -> a.arrayPairs()
            .stream()
            .anyMatch(FunctionCallHelper::isPairVulnerable))
          .isPresent();
    }

    private static boolean isPairVulnerable(ArrayPairTree pair) {
      ExpressionTree key = pair.key();
      return key != null && isLiteralTreeEqualsTo(key, "Access-Control-Allow-Origin") && isLiteralTreeEqualsTo(pair.value(), "*");
    }

    private boolean isCoreHeaderVulnerable() {
      return isCoreHeaderFunctionCall() &&
        retrieveArgumentAndVerifyItIsEqualsTo("header", 0, "Access-Control-Allow-Origin:*");
    }

    private boolean isSetOrHeaderVulnerable() {
      return isSetOrHeaderFunctionCall() &&
        retrieveArgumentAndVerifyItIsEqualsTo("key", 0, "Access-Control-Allow-Origin") &&
        retrieveArgumentAndVerifyItIsEqualsTo("values", 1, "*");
    }

    private boolean retrieveArgumentAndVerifyItIsEqualsTo(String name, int position, String expected) {
      Optional<CallArgumentTree> argumentTree = argument(functionCallTree, name, position);
      return argumentTree.isPresent() && isLiteralTreeEqualsTo(argumentTree.get().value(), expected);
    }

    private boolean isResponseConstructorFunctionCall() {
      return callee.is(NAMESPACE_NAME) && RESPONSE_CLASSES.contains(Symbols.getClass((NamespaceNameTree) callee).qualifiedName());
    }

    private boolean isCoreHeaderFunctionCall() {
      return Symbols.get(functionCallTree).qualifiedName().equals(HEADER_FUNCTION_NAME);
    }

    private boolean isSetOrHeaderFunctionCall() {
      return callee.is(OBJECT_MEMBER_ACCESS)
        && ("set".equals(lowerCaseFunctionName(functionCallTree)) || HEADER_FUNCTION_NAME.simpleName().equals(lowerCaseFunctionName(functionCallTree)));
    }
  }
}
