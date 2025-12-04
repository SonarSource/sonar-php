/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.sonar.check.Rule;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

@Rule(key = "S3984")
public class UnusedExceptionCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Throw this exception or remove this useless statement";

  private static final QualifiedName EXCEPTION_FQN = qualifiedName("Exception");

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    if (tree.expression().is(Tree.Kind.NEW_EXPRESSION)) {
      NewExpressionTree newExpressionTree = (NewExpressionTree) tree.expression();
      ExpressionTree checkForException = newExpressionTree.expression();
      if (checkForException.is(Tree.Kind.FUNCTION_CALL)) {
        checkForException = ((FunctionCallTree) checkForException).callee();
      }

      if (checkForException.is(Tree.Kind.NAMESPACE_NAME) && Symbols.getClass((NamespaceNameTree) checkForException).isOrSubClassOf(EXCEPTION_FQN).isTrue()) {
        context().newIssue(this, newExpressionTree, MESSAGE);
      }
    }
    super.visitExpressionStatement(tree);
  }
}
