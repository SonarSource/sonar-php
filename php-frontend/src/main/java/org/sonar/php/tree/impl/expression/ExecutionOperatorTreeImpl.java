/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExecutionOperatorTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ExecutionOperatorTreeImpl extends PHPTree implements ExecutionOperatorTree {

  private static final Kind KIND = Kind.EXECUTION_OPERATOR;

  private final ExpandableStringLiteralTree literal;

  public ExecutionOperatorTreeImpl(ExpandableStringLiteralTree literal) {
    this.literal = literal;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.singletonIterator(literal);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitExecutionOperator(this);
  }

  @Override
  public ExpandableStringLiteralTree literal() {
    return literal;
  }
}
