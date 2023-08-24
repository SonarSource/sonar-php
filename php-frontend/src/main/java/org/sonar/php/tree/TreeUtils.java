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
package org.sonar.php.tree;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;

import static java.util.Arrays.asList;
import static org.sonar.php.utils.collections.ListUtils.getLast;

public class TreeUtils {

  private TreeUtils() {
    // utility class - do not instantiate
  }

  public static boolean isDescendant(Tree tree, Tree potentialParent) {
    Tree parent = tree;
    while (parent != null && !potentialParent.equals(parent)) {
      parent = parent.getParent();
    }
    return potentialParent.equals(parent);
  }

  @CheckForNull
  public static Tree findAncestorWithKind(Tree tree, Collection<Tree.Kind> kinds) {
    Tree parent = tree;
    while (parent != null && !kinds.contains(parent.getKind())) {
      parent = parent.getParent();
    }
    return parent;
  }

  @CheckForNull
  public static Tree findAncestorWithKind(Tree tree, Tree.Kind... kinds) {
    return findAncestorWithKind(tree, asList(kinds));
  }

  public static Stream<Tree> descendants(@Nullable Tree root) {
    if (root == null || ((PHPTree) root).isLeaf()) {
      return Stream.empty();
    }
    Spliterator<Tree> spliterator = Spliterators.spliteratorUnknownSize(((PHPTree) root).childrenIterator(), Spliterator.ORDERED);
    Stream<Tree> stream = StreamSupport.stream(spliterator, false);
    return stream.flatMap(tree -> Stream.concat(Stream.of(tree), descendants(tree)));
  }

  public static <T extends Tree> Stream<T> descendants(@Nullable Tree root, Class<T> clazz) {
    return descendants(root)
      .filter(clazz::isInstance)
      .map(clazz::cast);
  }

  public static Optional<Tree> firstDescendant(@Nullable Tree root, Predicate<Tree> predicate) {
    return descendants(root).filter(predicate).findFirst();
  }

  public static <T extends Tree> Optional<T> firstDescendant(Tree root, Class<T> clazz) {
    return (Optional<T>) firstDescendant(root, clazz::isInstance);
  }

  public static boolean hasAnnotation(Tree declaration, String annotation) {
    if (!annotation.startsWith("@")) {
      annotation = "@" + annotation;
    }

    List<SyntaxTrivia> trivias = ((PHPTree) declaration).getFirstToken().trivias();

    if (!trivias.isEmpty()) {
      return StringUtils.containsIgnoreCase(getLast(trivias).text(), annotation);
    }

    return false;
  }
}
