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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.IntersectionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public abstract class CombinedTypeTreeImpl extends PHPTree {

  private final Kind kind;
  private final SeparatedList<TypeTree> types;

  protected CombinedTypeTreeImpl(Tree.Kind kind, SeparatedList<TypeTree> types) {
    this.kind = kind;
    this.types = types;
  }

  public static class UnionTypeTreeImpl extends CombinedTypeTreeImpl implements UnionTypeTree {

    public UnionTypeTreeImpl(SeparatedList<TypeTree> types) {
      super(Kind.UNION_TYPE, types);
    }

    @Override
    public void accept(VisitorCheck visitor) {
      visitor.visitUnionType(this);
    }
  }

  public static class IntersectionTypeTreeImpl extends CombinedTypeTreeImpl implements IntersectionTypeTree {

    public IntersectionTypeTreeImpl(SeparatedList<TypeTree> types) {
      super(Kind.INTERSECTION_TYPE, types);
    }

    @Override
    public void accept(VisitorCheck visitor) {
      visitor.visitIntersectionType(this);
    }
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return types.elementsAndSeparators();
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  public SeparatedList<TypeTree> types() {
    return types;
  }

  public boolean isSimple() {
    return false;
  }
}
