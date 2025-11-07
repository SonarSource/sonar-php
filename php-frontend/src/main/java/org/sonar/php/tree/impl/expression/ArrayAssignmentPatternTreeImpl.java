/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ArrayAssignmentPatternTreeImpl extends PHPTree implements ArrayAssignmentPatternTree {

  private static final Kind KIND = Kind.ARRAY_ASSIGNMENT_PATTERN;
  private final SyntaxToken openBracket;
  private final ArrayAssignmentPatternElements elements;
  private final SyntaxToken closeBracket;

  public ArrayAssignmentPatternTreeImpl(
    SyntaxToken openBracket,
    ArrayAssignmentPatternElements elements,
    SyntaxToken closeBracket) {
    this.openBracket = openBracket;
    this.elements = elements;
    this.closeBracket = closeBracket;
  }

  @Override
  public SyntaxToken openBracketToken() {
    return openBracket;
  }

  @Override
  public List<Optional<ArrayAssignmentPatternElementTree>> elements() {
    return elements.elements();
  }

  @Override
  public List<SyntaxToken> separators() {
    return elements.separators();
  }

  @Override
  public SyntaxToken closeBracketToken() {
    return closeBracket;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(openBracket),
      elements.elementsAndSeparators().iterator(),
      IteratorUtils.iteratorOf(closeBracket));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitArrayAssignmentPattern(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

}
