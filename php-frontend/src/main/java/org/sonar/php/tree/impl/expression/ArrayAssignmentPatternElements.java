/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
    List<Tuple<SyntaxToken, Optional<ArrayAssignmentPatternElementTree>>> rest) {
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
