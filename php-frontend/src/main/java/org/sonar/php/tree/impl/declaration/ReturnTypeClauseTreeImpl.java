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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ReturnTypeClauseTreeImpl extends PHPTree implements ReturnTypeClauseTree {

  private static final Kind KIND = Kind.RETURN_TYPE_CLAUSE;
  private final InternalSyntaxToken colonToken;
  private final DeclaredTypeTree type;

  public ReturnTypeClauseTreeImpl(InternalSyntaxToken colonToken, DeclaredTypeTree type) {
    this.colonToken = colonToken;
    this.type = type;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(colonToken, type);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitReturnTypeClause(this);
  }

  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  /**
   * @deprecated since 3.11 - Use {@link #declaredType()} instead.
   */
  @Override
  @Deprecated
  public TypeTree type() {
    if (type.is(Kind.TYPE)) {
      return (TypeTree) type;
    } else {
      return ((UnionTypeTree) type).types().get(0);
    }
  }

  @Override
  public DeclaredTypeTree declaredType() {
    return type;
  }

}
