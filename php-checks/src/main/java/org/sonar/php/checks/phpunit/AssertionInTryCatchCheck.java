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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = "S5779")
public class AssertionInTryCatchCheck extends PhpUnitCheck {
  private static final String MESSAGE = "Don't use this assertion inside a try-catch catching an assertion exception.";
  private static final String SECONDARY_MESSAGE = "Exception type that catches assertion exceptions.";

  private static final Set<String> RELEVANT_EXCEPTIONS = SetUtils.immutableSetOf(
    "exception",
    "phpunit\\framework\\expectationfailedexception",
    "phpunit\\framework\\assertionfailederror"
  );

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    if (!isPhpUnitTestCase()) {
      return;
    }

    List<NamespaceNameTree> caughtRelevantExceptionTypes = getCaughtRelevantExceptionTypes(tree.catchBlocks());
    if (!caughtRelevantExceptionTypes.isEmpty()) {
      AssertionsFindVisitor assertionsFindVisitor = new AssertionsFindVisitor();
      tree.block().accept(assertionsFindVisitor);
      assertionsFindVisitor.foundAssertions.forEach(a -> raiseIssue(a, caughtRelevantExceptionTypes));
    }

    super.visitTryStatement(tree);
  }

  private void raiseIssue(FunctionCallTree assertion, List<NamespaceNameTree> caughtRelevantExceptionTypes) {
    PreciseIssue issue = newIssue(assertion, MESSAGE);
    caughtRelevantExceptionTypes.forEach(e -> issue.secondary(e, SECONDARY_MESSAGE));
  }

  private List<NamespaceNameTree> getCaughtRelevantExceptionTypes(List<CatchBlockTree> catchBlocks) {
    List<NamespaceNameTree> result = new ArrayList<>();
    for(CatchBlockTree catchBlockTree : catchBlocks) {
      if (variableIsUsed(catchBlockTree.variable())) {
        continue;
      }

      result.addAll(
        catchBlockTree.exceptionTypes().stream().
          filter(AssertionInTryCatchCheck::isRelevantExceptionType).
          collect(Collectors.toList())
      );
    }

    return result;
  }

  private boolean variableIsUsed(@Nullable VariableIdentifierTree variable) {
    return variable != null && !context().symbolTable().getSymbol(variable).usages().isEmpty();
  }

  private static boolean isRelevantExceptionType(NamespaceNameTree tree) {
    return RELEVANT_EXCEPTIONS.contains(Symbols.getClass(tree).qualifiedName().toString());
  }

  private static class AssertionsFindVisitor extends PHPVisitorCheck {
    private final List<FunctionCallTree> foundAssertions = new ArrayList<>();

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (isAssertion(tree)) {
        foundAssertions.add(tree);
      }

      super.visitFunctionCall(tree);
    }
  }
}
