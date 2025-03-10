/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api.visitors;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

public abstract class PHPTreeSubscriber {

  private Collection<Tree.Kind> nodesToVisit;

  public abstract List<Kind> nodesToVisit();

  public void visitNode(Tree tree) {
    // Default behavior : do nothing.
  }

  public void leaveNode(Tree tree) {
    // Default behavior : do nothing.
  }

  public void scanTree(Tree tree) {
    nodesToVisit = nodesToVisit();
    visit(tree);
  }

  private void visit(Tree tree) {
    boolean isSubscribed = isSubscribed(tree);
    if (isSubscribed) {
      visitNode(tree);
    }
    visitChildren(tree);
    if (isSubscribed) {
      leaveNode(tree);
    }
  }

  private boolean isSubscribed(Tree tree) {
    return nodesToVisit.contains(tree.getKind());
  }

  private void visitChildren(Tree tree) {
    PHPTree javaTree = (PHPTree) tree;

    if (!javaTree.isLeaf()) {
      for (Iterator<Tree> iter = javaTree.childrenIterator(); iter.hasNext();) {
        Tree next = iter.next();

        if (next != null) {
          visit(next);
        }
      }
    }
  }

}
