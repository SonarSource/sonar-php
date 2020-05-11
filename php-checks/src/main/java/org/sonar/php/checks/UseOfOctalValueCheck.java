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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.*;
import org.sonar.plugins.php.api.tree.expression.ArrowFunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.regex.Pattern;

@Rule(key = UseOfOctalValueCheck.KEY)
public class UseOfOctalValueCheck extends PHPVisitorCheck {

  public static final String KEY = "S1314";
  private static final String MESSAGE = " Use decimal rather than octal values.";

  // Pattern syntax from https://www.php.net/manual/en/language.types.integer.php#language.types.integer.syntax
  private static final Pattern OCTAL_NUMERIC_PATTERN = Pattern.compile("^0[0-7]+(_[0-7]+)*$");

  @Override
  public void visitVariableDeclaration(VariableDeclarationTree tree) {
    if (tree.initValue() != null && tree.initValue().is(Tree.Kind.NUMERIC_LITERAL)) {
      checkNumericValue((LiteralTree) tree.initValue());
    }

    super.visitVariableDeclaration(tree);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (tree.value().is(Tree.Kind.NUMERIC_LITERAL)) {
      checkNumericValue((LiteralTree) tree.value());
    }

    super.visitAssignmentExpression(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkParameterInitValue(tree.parameters());

    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    checkParameterInitValue(tree.parameters());

    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    checkParameterInitValue(tree.parameters());

    super.visitFunctionExpression(tree);
  }

  @Override
  public void visitArrowFunctionExpression(ArrowFunctionExpressionTree tree) {
    checkParameterInitValue(tree.parameters());

    super.visitArrowFunctionExpression(tree);
  }

  private void checkParameterInitValue(ParameterListTree tree) {
    for (ParameterTree parameterTree : tree.parameters()) {
      if (parameterTree.initValue() != null && parameterTree.initValue().is(Tree.Kind.NUMERIC_LITERAL)) {
        checkNumericValue((LiteralTree) parameterTree.initValue());
      }
    }
  }

  private void checkNumericValue(LiteralTree tree) {
    if (OCTAL_NUMERIC_PATTERN.matcher(tree.value()).find()) {
      context().newIssue(this, tree, MESSAGE);
    }
  }


}
