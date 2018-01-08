/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.checks.utils;

import com.google.common.collect.ImmutableSet;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public abstract class FunctionUsageCheck extends PHPVisitorCheck {

  protected abstract ImmutableSet<String> functionNames();

  protected abstract void createIssue(FunctionCallTree tree);

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (isForbiddenFunction(tree.callee())) {
      createIssue(tree);
    }

    super.visitFunctionCall(tree);
  }

  private boolean isForbiddenFunction(ExpressionTree callee) {
    return callee.is(Kind.NAMESPACE_NAME) && functionNames().contains(((NamespaceNameTree) callee).qualifiedName());
  }

}
