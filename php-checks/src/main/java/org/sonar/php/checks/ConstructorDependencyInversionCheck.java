/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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

import java.util.ArrayDeque;
import java.util.Deque;
import org.sonar.check.Rule;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ConstructorDependencyInversionCheck.KEY)
public class ConstructorDependencyInversionCheck extends PHPVisitorCheck {

  public static final String KEY = "S2830";
  private static final String MESSAGE = "Remove this creation of object in constructor. Use dependency injection instead.";

  private Deque<Boolean> inConstructor = new ArrayDeque<>();

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    visitClass(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    visitClass(tree);
  }

  private void visitClass(ClassTree tree) {
    inConstructor.addLast(false);

    MethodDeclarationTree constructor = tree.fetchConstructor();

    for (ClassMemberTree memberTree : tree.members()) {
      if (memberTree.equals(constructor)) {
        inConstructor.addLast(true);
        scan(memberTree);
        inConstructor.removeLast();
        inConstructor.addLast(false);

      } else {
        scan(memberTree);
      }
    }

    inConstructor.removeLast();
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    // Ignore instantiation in throw statement.
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    if (!inConstructor.isEmpty() && inConstructor.getLast().booleanValue() && !isInParameterDefault(tree)) {
      context().newIssue(this, tree.newToken(), MESSAGE);
    }
    super.visitNewExpression(tree);
  }

  private static boolean isInParameterDefault(NewExpressionTree newExpr) {
    return TreeUtils.findAncestorWithKind(newExpr, Tree.Kind.PARAMETER) != null;
  }

}
