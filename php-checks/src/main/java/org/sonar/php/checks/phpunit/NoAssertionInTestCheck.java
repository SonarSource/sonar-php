/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.checks.phpunit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2699")
public class NoAssertionInTestCheck extends PhpUnitCheck {
  private static final String MESSAGE = "Add at least one assertion to this test case.";

  private static final Pattern ASSERTION_METHODS_PATTERN = Pattern.compile("(assert|verify|fail|pass|should|will|check|expect|validate|.*test).*");
  private static final List<String> TEST_CONTROL_FUNCTIONS = Arrays.asList(
    "addtoassertioncount",
    "marktestskipped",
    "marktestincomplete");

  private final Map<MethodDeclarationTree, Boolean> assertionInMethod = new HashMap<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assertionInMethod.clear();
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isTestCaseMethod(tree)) {
      return;
    }

    if (CheckUtils.hasAnnotation(tree, "expectedException")
      || CheckUtils.hasAnnotation(tree, "doesNotPerformAssertions")
      || CheckUtils.hasAnnotation(tree, "expectedDeprecation")) {
      return;
    }

    AssertionsFindVisitor assertionsFindVisitor = new AssertionsFindVisitor(context().symbolTable());
    tree.accept(assertionsFindVisitor);

    if (!assertionsFindVisitor.hasFoundAssertion) {
      newIssue(tree.name(), MESSAGE);
    }
  }

  private class AssertionsFindVisitor extends PHPVisitorCheck {
    private boolean hasFoundAssertion = false;
    private final SymbolTable symbolTable;

    private AssertionsFindVisitor(SymbolTable symbolTable) {
      this.symbolTable = symbolTable;
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      String functionName = CheckUtils.lowerCaseFunctionName(tree);

      if (isAssertion(tree)
        || functionNameCountsAsAssertion(functionName)
        || isDynamicFunctionCall(tree)
        || isLocalMethodWithAssertion(tree)) {
        hasFoundAssertion = true;
      }

      super.visitFunctionCall(tree);
    }

    private boolean functionNameCountsAsAssertion(@Nullable String functionName) {
      if (functionName == null) {
        return false;
      }

      return ASSERTION_METHODS_PATTERN.matcher(functionName).matches()
        || TEST_CONTROL_FUNCTIONS.contains(functionName);
    }

    private boolean isDynamicFunctionCall(FunctionCallTree tree) {
      Tree functionNameTree = tree.callee();

      if (functionNameTree.is(Tree.Kind.CLASS_MEMBER_ACCESS, Tree.Kind.OBJECT_MEMBER_ACCESS)) {
        functionNameTree = ((MemberAccessTree) functionNameTree).member();
      }

      return !functionNameTree.is(Tree.Kind.NAMESPACE_NAME, Tree.Kind.NAME_IDENTIFIER);
    }

    private boolean isLocalMethodWithAssertion(FunctionCallTree tree) {
      MethodDeclarationTree methodDeclaration;
      Optional<MethodDeclarationTree> optionalMethodDeclaration = getMethodDeclarationTree(tree);
      if (optionalMethodDeclaration.isPresent()) {
        methodDeclaration = optionalMethodDeclaration.get();
      } else {
        return false;
      }

      if (!assertionInMethod.containsKey(methodDeclaration)) {
        assertionInMethod.put(methodDeclaration, false);
        AssertionsFindVisitor v = new AssertionsFindVisitor(symbolTable);
        methodDeclaration.accept(v);
        assertionInMethod.put(methodDeclaration, v.hasFoundAssertion);
      }
      return assertionInMethod.get(methodDeclaration);
    }

    private Optional<MethodDeclarationTree> getMethodDeclarationTree(FunctionCallTree tree) {
      ExpressionTree callee = tree.callee();
      if (!callee.is(Tree.Kind.CLASS_MEMBER_ACCESS, Tree.Kind.OBJECT_MEMBER_ACCESS)) {
        return Optional.empty();
      }

      Symbol symbol = symbolTable.getSymbol(((MemberAccessTree) callee).member());
      if (symbol != null && symbol.is(Symbol.Kind.FUNCTION)) {
        return Optional.ofNullable((MethodDeclarationTree) TreeUtils.findAncestorWithKind(symbol.declaration(),
          SetUtils.immutableSetOf(Tree.Kind.METHOD_DECLARATION)));
      }

      return Optional.empty();
    }
  }
}
