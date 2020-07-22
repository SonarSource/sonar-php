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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.Optional;

@Rule(key = "S5632")
public class ThrowThrowableCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Throw an object derived from \"Throwable\".";
  private static final QualifiedName THROWABLE_FQN = QualifiedName.qualifiedName("Throwable");

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    if (tree.expression().is(Tree.Kind.NEW_EXPRESSION)) {
      Optional<NamespaceNameTree> namespaceNameTree = extractNamespaceTree(((NewExpressionTree) tree.expression()).expression());
      namespaceNameTree.ifPresent(n -> verifyClass(n, tree));
    }
    super.visitThrowStatement(tree);
  }

  private static Optional<NamespaceNameTree> extractNamespaceTree(ExpressionTree expression) {
    if (expression.is(Tree.Kind.FUNCTION_CALL) && ((FunctionCallTree) expression).callee().is(Tree.Kind.NAMESPACE_NAME)) {
      return Optional.of((NamespaceNameTree) ((FunctionCallTree) expression).callee());
    } else if (expression.is(Tree.Kind.NAMESPACE_NAME)) {
      return Optional.of((NamespaceNameTree) expression);
    }

    return Optional.empty();
  }

  private void verifyClass(NamespaceNameTree namespaceNameTree, ThrowStatementTree tree) {
    if (Symbols.getClass(namespaceNameTree).isSubTypeOf(THROWABLE_FQN).isFalse()) {
      context().newIssue(this, tree, MESSAGE);
    }
  }
}
