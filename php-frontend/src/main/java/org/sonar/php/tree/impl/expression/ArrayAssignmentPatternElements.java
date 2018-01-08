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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.php.parser.TreeFactory.Tuple;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public class ArrayAssignmentPatternElements {

  private final List<Tree> elementsAndSeparators = new ArrayList<>();
  private final List<Optional<ArrayAssignmentPatternElementTree>> elements = new ArrayList<>();
  private final List<SyntaxToken> separators = new ArrayList<>();

  public ArrayAssignmentPatternElements(
    @Nullable ArrayAssignmentPatternElementTree firstElement,
    List<Tuple<SyntaxToken,Optional<ArrayAssignmentPatternElementTree>>> rest
  ) {
    elements.add(Optional.ofNullable(firstElement));
    if (firstElement != null) {
      elementsAndSeparators.add(firstElement);
    }
    for (Tuple<SyntaxToken, Optional<ArrayAssignmentPatternElementTree>> tuple : rest) {
      SyntaxToken separator = tuple.first();
      Optional<ArrayAssignmentPatternElementTree> element = tuple.second();
      separators.add(separator);
      elementsAndSeparators.add(separator);
      elements.add(element);
      if (element.isPresent()) {
        elementsAndSeparators.add(element.get());
      }
    }
  }

  public List<Optional<ArrayAssignmentPatternElementTree>> elements() {
    return elements;
  }

  public List<SyntaxToken> separators() {
    return separators;
  }

  public List<Tree> elementsAndSeparators() {
    return elementsAndSeparators;
  }

}
