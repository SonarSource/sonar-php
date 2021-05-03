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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = "S5935")
public class ExceptionTestingCheck extends PhpUnitCheck {
  private static final String MESSAGE = "Use expectException() to verify the exception throw.";
  private static final String MESSAGE_CODE = "Use expectExceptionCode() instead.";
  private static final String MESSAGE_MESSAGE = "Use expectExceptionMessage() instead.";

  private static final List<String> RELEVANT_ASSERTIONS = Arrays.asList(
    "assertEquals",
    "assertSame");

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    if (!isPhpUnitTestMethod()) {
      return;
    }

    if (isLastInMethodBody(tree) && tree.catchBlocks().size() == 1 && containsCallToFail(tree.block())) {
      CatchBlockInspector catchBlockInspector = new CatchBlockInspector(tree.catchBlocks().get(0).variable(), context().symbolTable());
      tree.catchBlocks().get(0).block().accept(catchBlockInspector);
      if (!catchBlockInspector.didFindOtherCalls) {
        raiseIssue(tree.catchBlocks().get(0).exceptionTypes(), tree.catchBlocks().get(0).variable(), catchBlockInspector);
      }
    }

    super.visitTryStatement(tree);
  }

  private static boolean isLastInMethodBody(TryStatementTree tree) {
    MethodDeclarationTree method = (MethodDeclarationTree) TreeUtils.findAncestorWithKind(tree, Collections.singletonList(Tree.Kind.METHOD_DECLARATION));

    Objects.requireNonNull(method);

    BlockTree methodBody = (BlockTree) method.body();

    return methodBody.statements().get(methodBody.statements().size() - 1) == tree;
  }

  private void raiseIssue(SeparatedList<NamespaceNameTree> exceptionTypes, @Nullable VariableIdentifierTree variable, CatchBlockInspector catchBlockInspector) {
    PreciseIssue issue;
    if (variable == null) {
      issue = context().newIssue(this, exceptionTypes.get(0), exceptionTypes.get(exceptionTypes.size() - 1), MESSAGE);
    } else {
      issue = newIssue(variable, MESSAGE);
    }
    catchBlockInspector.foundExceptionAssertions.forEach(issue::secondary);
  }

  private static boolean containsCallToFail(BlockTree block) {
    int numberOfStatements = block.statements().size();
    return numberOfStatements > 0 && isCallToFail(block.statements().get(numberOfStatements - 1));
  }

  private static boolean isCallToFail(StatementTree statementTree) {
    if (!statementTree.is(Tree.Kind.EXPRESSION_STATEMENT)
      || !((ExpressionStatementTree) statementTree).expression().is(Tree.Kind.FUNCTION_CALL)) {
      return false;
    }

    FunctionCallTree functionCall = (FunctionCallTree) ((ExpressionStatementTree) statementTree).expression();
    return "fail".equals(CheckUtils.lowerCaseFunctionName(functionCall));
  }

  private static class CatchBlockInspector extends PHPVisitorCheck {
    private final Symbol exceptionVariableSymbol;
    private final SymbolTable symbolTable;
    private boolean didFindOtherCalls = false;
    private final Map<Tree, String> foundExceptionAssertions = new HashMap<>();

    public CatchBlockInspector(@Nullable VariableIdentifierTree variable, SymbolTable symbolTable) {
      this.symbolTable = symbolTable;
      exceptionVariableSymbol = variable != null ? symbolTable.getSymbol(variable) : null;
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      Optional<Assertion> assertion = getAssertion(tree);

      if (!assertion.isPresent() || !RELEVANT_ASSERTIONS.contains(assertion.get().name())) {
        didFindOtherCalls = true;
        return;
      }

      CallArgumentTree arg1 = CheckUtils.argument(tree, "expected", 0).orElse(null);
      CallArgumentTree arg2 = CheckUtils.argument(tree, "actual", 1).orElse(null);

      String exceptionMethodCall = getExceptionVariableMethodCall(arg1)
        .orElse(getExceptionVariableMethodCall(arg2).orElse(null));

      if ("getmessage".equals(exceptionMethodCall)) {
        foundExceptionAssertions.put(tree, MESSAGE_MESSAGE);
      } else if ("getcode".equals(exceptionMethodCall)) {
        foundExceptionAssertions.put(tree, MESSAGE_CODE);
      } else {
        didFindOtherCalls = true;
      }
    }

    private Optional<String> getExceptionVariableMethodCall(@Nullable CallArgumentTree callArgument) {
      if (callArgument == null) {
        return Optional.empty();
      }

      ExpressionTree expressionTree = callArgument.value();
      if (!expressionTree.is(Tree.Kind.FUNCTION_CALL) ||
        !((FunctionCallTree) expressionTree).callee().is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
        return Optional.empty();
      }

      ExpressionTree object = ((MemberAccessTree) ((FunctionCallTree) expressionTree).callee()).object();

      if (!object.is(Tree.Kind.VARIABLE_IDENTIFIER) || symbolTable.getSymbol(object) != exceptionVariableSymbol) {
        return Optional.empty();
      }

      return Optional.ofNullable(CheckUtils.lowerCaseFunctionName(((FunctionCallTree) expressionTree)));
    }
  }
}
