/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ThrowExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S5632")
public class ThrowThrowableCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Throw an object derived from \"Throwable\".";
  private static final String SECONDARY_MESSAGE = "Class definition.";
  private static final QualifiedName THROWABLE_FQN = QualifiedName.qualifiedName("Throwable");

  @Override
  public void visitThrowExpression(ThrowExpressionTree tree) {
    if (tree.expression().is(Tree.Kind.NEW_EXPRESSION)) {
      extractNamespaceTree(((NewExpressionTree) tree.expression()).expression())
        .ifPresent(n -> verifyClass(n, tree));
    }
    super.visitThrowExpression(tree);
  }

  private static Optional<NamespaceNameTree> extractNamespaceTree(ExpressionTree expression) {
    if (expression.is(Tree.Kind.FUNCTION_CALL)) {
      expression = ((FunctionCallTree) expression).callee();
    }
    if (expression.is(Tree.Kind.NAMESPACE_NAME)) {
      return Optional.of((NamespaceNameTree) expression);
    }

    return Optional.empty();
  }

  private void verifyClass(NamespaceNameTree namespaceNameTree, ThrowExpressionTree tree) {
    ClassSymbol classSymbol = Symbols.getClass(namespaceNameTree);
    if (classSymbol.isSubTypeOf(THROWABLE_FQN).isFalse()) {
      context().newIssue(this, tree, MESSAGE).secondary(classSymbol.location(), SECONDARY_MESSAGE);
    }
  }
}
