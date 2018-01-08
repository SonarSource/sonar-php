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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableVariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;
import java.util.List;

public class VariableVariableTreeImpl extends PHPTree implements VariableVariableTree {

  private static final Kind KIND = Kind.VARIABLE_VARIABLE;
  private final ImmutableList<SyntaxToken> dollars;
  private final ExpressionTree variable;

  public VariableVariableTreeImpl(List<InternalSyntaxToken> dollars, ExpressionTree variable) {
    this.dollars = ImmutableList.<SyntaxToken>copyOf(dollars);
    this.variable = variable;
  }

  @Override
  public List<SyntaxToken> dollarTokens() {
    return dollars;
  }

  @Override
  public ExpressionTree variableExpression() {
    return variable;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(dollars.iterator(), Iterators.singletonIterator(variable));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitVariableVariable(this);
  }

}
