/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.checks.security;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.argument;
import static org.sonar.php.checks.utils.CheckUtils.lowerCaseFunctionName;
import static org.sonar.php.checks.utils.CheckUtils.nameOf;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;
import static org.sonar.php.tree.TreeUtils.firstDescendant;
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
    FunctionCallHelper functionCallHelper = FunctionCallHelper.create(functionCallTree);

    if ((functionCallHelper.isResponseConstuctorVulnerable()) || (functionCallHelper.isCoreHeaderVulnerable()) || (functionCallHelper.isSetOrHeaderVulnerable())) {
      context().newIssue(this, functionCallTree, MESSAGE);
    }
    super.visitFunctionCall(functionCallTree);
  }
  
  private void checkCorsPhpFile(ReturnStatementTree returnStatementTree) {
    ExpressionTree returnExpression = returnStatementTree.expression();
    if (returnExpression.is(ARRAY_INITIALIZER_BRACKET, ARRAY_INITIALIZER_FUNCTION)) {
      ((ArrayInitializerTree) returnExpression)
      .arrayPairs().stream()
      .filter(pair -> isLiteralTreeEqualsTo(pair.key(), "allowed_origins") && arrayContainsSensitiveValue(pair.value()))
      .forEach(pair -> context().newIssue(this, pair, MESSAGE));
    }
  }

  private static boolean arrayContainsSensitiveValue(ExpressionTree arrayValue) {
    return arrayValue.is(ARRAY_INITIALIZER_BRACKET, ARRAY_INITIALIZER_FUNCTION) &&
      ((ArrayInitializerTree) arrayValue)
        .arrayPairs().stream()
        .anyMatch(pair -> isLiteralTreeEqualsTo(pair.value(), "*"));
  }

  private static boolean isLiteralTreeEqualsTo(ExpressionTree tree, String expected) {
    return tree.is(REGULAR_STRING_LITERAL) && expected.equalsIgnoreCase(trimQuotes(((LiteralTree) tree).value().replaceAll("\\s", "")));
  }

  private static class FunctionCallHelper {

    private static final String HEADER = "header";

    private FunctionCallTree functionCallTree;
    private ExpressionTree callee;

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
          .filter(ArrayInitializerTree.class::isInstance)
          .map(ArrayInitializerTree.class::cast)
          .filter(a -> a.arrayPairs()
            .stream()
            .anyMatch(b -> isLiteralTreeEqualsTo(b.key(), "Access-Control-Allow-Origin") && isLiteralTreeEqualsTo(b.value(), "*")))
          .isPresent();
    }

    private boolean isCoreHeaderVulnerable() {
      return isCoreHeaderFunctionCall() &&
        retrieveArgumentAndVerifyItIsEqualsTo(HEADER, 0, "Access-Control-Allow-Origin:*");
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
      return callee instanceof ClassNamespaceNameTreeImpl && "Response".equalsIgnoreCase(nameOf(callee));
    }

    private boolean isCoreHeaderFunctionCall() {
      return callee.is(NAMESPACE_NAME) && HEADER.equals(lowerCaseFunctionName(functionCallTree));
    }

    private boolean isSetOrHeaderFunctionCall() {
      return callee.is(OBJECT_MEMBER_ACCESS) && ("set".equals(lowerCaseFunctionName(functionCallTree)) || HEADER.equals(lowerCaseFunctionName(functionCallTree)));
    }
  }
}
