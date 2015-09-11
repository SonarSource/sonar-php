/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.visitors;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.TreeVisitorContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class SubscriptionAstTreeVisitor implements PHPCheck {

  private TreeVisitorContext context;
  private Collection<Tree.Kind> nodesToVisit;

  public abstract List<Tree.Kind> nodesToVisit();

  @Override
  public TreeVisitorContext getContext() {
    return context;
  }

  public void visitNode(Tree tree) {
    // Default behavior : do nothing.
  }

  public void leaveNode(Tree tree) {
    // Default behavior : do nothing.
  }

  public void visitToken(SyntaxToken syntaxToken) {
    // default behaviour is to do nothing
  }

  public void visitTrivia(SyntaxTrivia syntaxTrivia) {
    // default behaviour is to do nothing
  }

  public void visitFile(Tree scriptTree) {
    // default behaviour is to do nothing
  }

  @Override
  public void scanFile(TreeVisitorContext context) {
    this.context = context;
    visitFile(context.getTopTree());
    scanTree(context.getTopTree());
  }

  protected void scanTree(Tree tree) {
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

  protected boolean isSubscribed(Tree tree) {
    return nodesToVisit.contains(((PHPTree) tree).getKind());
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

