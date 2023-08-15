/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S6344")
public class RedefineConstantCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure to not redefine a constant.";
  private static final String SECONDARY_MESSAGE = "Initial constant definition.";

  @Override
  public void visitScript(ScriptTree tree) {
    visitStatements(tree.statements());
    super.visitScript(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    visitStatements(tree.body().statements());
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (tree.body().is(Tree.Kind.BLOCK)) {
      visitStatements(((BlockTree) tree.body()).statements());
    }
    super.visitMethodDeclaration(tree);
  }

  private void visitStatements(List<StatementTree> statements) {
    Map<String, Tree> constants = new HashMap<>();
    statements.stream().filter(ExpressionStatementTree.class::isInstance)
      .map(e -> ((ExpressionStatementTree) e).expression())
      .filter(RedefineConstantCheck::isDefine)
      .map(FunctionCallTree.class::cast)
      .forEach(define -> handleDefine(define, constants));
  }

  private static boolean isDefine(ExpressionTree tree) {
    return tree.is(Tree.Kind.FUNCTION_CALL) && "define".equals(CheckUtils.getLowerCaseFunctionName((FunctionCallTree) tree));
  }

  private void handleDefine(FunctionCallTree tree, Map<String, Tree> constants) {
    constantName(tree).ifPresent(name -> {
      if (constants.containsKey(name)) {
        newIssue(tree, MESSAGE).secondary(constants.get(name), SECONDARY_MESSAGE);
      } else {
        constants.put(name, tree);
      }
    });
  }

  private static Optional<String> constantName(Tree tree) {
    return CheckUtils.argument((FunctionCallTree) tree, "constant_name", 0)
      .map(CallArgumentTree::value)
      .filter(a -> a.is(Tree.Kind.REGULAR_STRING_LITERAL))
      .map(a -> CheckUtils.trimQuotes((LiteralTree) a));
  }

}
