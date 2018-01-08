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
package org.sonar.php.tree.impl;

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public abstract class PHPTree implements Tree {

  @Nullable
  private Tree parent;

  public void setParent(Tree parent) {
    this.parent = parent;
  }

  @Nullable
  public Tree getParent() {
    return parent;
  }

  public int getLine() {
    return getFirstToken().line();
  }

  @Override
  public final boolean is(Kind... kind) {
    if (getKind() != null) {
      for (Kind kindIter : kind) {
        if (getKind() == kindIter) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Creates iterator for children of this node.
   * Note that iterator may contain {@code null} elements.
   *
   * @throws UnsupportedOperationException if {@link #isLeaf()} returns {@code true}
   */
  public abstract Iterator<Tree> childrenIterator();

  public boolean isLeaf() {
    return false;
  }

  public SyntaxToken getLastToken() {
    SyntaxToken lastToken = null;
    Iterator<Tree> childrenIterator = childrenIterator();
    while (childrenIterator.hasNext()) {
      PHPTree child = (PHPTree) childrenIterator.next();
      if (child != null) {
        SyntaxToken childLastToken = child.getLastToken();
        if (childLastToken != null) {
          lastToken = childLastToken;
        }
      }
    }
    return lastToken;
  }

  public SyntaxToken getFirstToken() {
    Iterator<Tree> childrenIterator = childrenIterator();
    Tree child;
    do {
      if (childrenIterator.hasNext()) {
        child = childrenIterator.next();
      } else {
        throw new IllegalStateException("Tree has no non-null children " + getKind());
      }
    } while (child == null);
    return ((PHPTree) child).getFirstToken();
  }

  @Override
  public String toString() {
    if (this.is(Kind.TOKEN, Kind.INLINE_HTML_TOKEN)) {
      return ((SyntaxToken) this).text();

    } else {
      StringBuilder sb = new StringBuilder();
      Iterator<Tree> treeIterator = childrenIterator();
      SyntaxToken prevToken = null;

      while (treeIterator.hasNext()) {
        Tree child = treeIterator.next();

        if (child != null) {
          appendChild(sb, prevToken, child);
          prevToken = ((PHPTree) child).getLastToken();
        }
      }
      return sb.toString();
    }
  }

  private static void appendChild(StringBuilder sb, @Nullable SyntaxToken prevToken, Tree child) {
    if (prevToken != null) {
      SyntaxToken firstToken = ((PHPTree) child).getFirstToken();
      if (isSpaceRequired(prevToken, firstToken)) {
        sb.append(" ");
      }
    }
    sb.append(child.toString());
  }

  private static boolean isSpaceRequired(SyntaxToken prevToken, SyntaxToken token) {
    return (token.line() > prevToken.line()) || (prevToken.column() + prevToken.text().length() < token.column());
  }
}

