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

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.Iterator;

public final class SyntacticEquivalence {

  private SyntacticEquivalence() {
  }

  public static boolean areSyntacticallyEquivalent(@Nullable Tree tree1, @Nullable Tree tree2) {
    if (tree1 == tree2) {
      return true;
    }

    if (tree1 == null || tree2 == null) {
      return false;
    }

    PHPTree phpTree1 = (PHPTree) tree1;
    PHPTree phpTree2 = (PHPTree) tree2;

    if (phpTree1.getKind() != phpTree2.getKind()) {
      return false;
    } else if (phpTree1.isLeaf()) {
      return phpTree1.getFirstToken().text().equals(phpTree2.getFirstToken().text());
    }

    Iterator<Tree> iterator1 = phpTree1.childrenIterator();
    Iterator<Tree> iterator2 = phpTree2.childrenIterator();
    return areSyntacticallyEquivalent(iterator1, iterator2);
  }

  public static boolean areSyntacticallyEquivalent(Iterable<? extends Tree> iterable1, Iterable<? extends Tree> iterable2) {
    return areSyntacticallyEquivalent(iterable1.iterator(), iterable2.iterator());
  }

  public static boolean areSyntacticallyEquivalent(Iterator<? extends Tree> iterator1, Iterator<? extends Tree> iterator2) {
    while (iterator1.hasNext() && iterator2.hasNext()) {
      if (!areSyntacticallyEquivalent(iterator1.next(), iterator2.next())) {
        return false;
      }
    }

    return !iterator1.hasNext() && !iterator2.hasNext();
  }

}
