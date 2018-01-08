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
package org.sonar.php.tree.impl.expression;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class AssignmentByReferenceTreeImpl extends PHPTree implements AssignmentExpressionTree {

  private static final Kind KIND = Kind.ASSIGNMENT_BY_REFERENCE;

  private final ExpressionTree variable;
  private final InternalSyntaxToken equalToken;
  private final InternalSyntaxToken ampersandToken;
  private final ExpressionTree value;

  public AssignmentByReferenceTreeImpl(ExpressionTree variable, InternalSyntaxToken equalToken, InternalSyntaxToken ampersandToken, ExpressionTree value) {
    this.variable = variable;
    this.equalToken = equalToken;
    this.ampersandToken = ampersandToken;
    this.value = value;
  }

  @Override
  public ExpressionTree variable() {
    return variable;
  }

  @Override
  public String operator() {
    return equalToken.text() + ampersandToken.text();
  }

  public SyntaxToken equalToken() {
    return equalToken;
  }

  public SyntaxToken ampersandToken() {
    return ampersandToken;
  }

  @Override
  public ExpressionTree value() {
    return value;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(variable, equalToken, ampersandToken, value);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitAssignmentExpression(this);
  }
}
