/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerBracketTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ArrayInitializerBracketTreeImpl extends PHPTree implements ArrayInitializerBracketTree {

  private static final Kind KIND = Kind.ARRAY_INITIALIZER_BRACKET;
  private final InternalSyntaxToken openBracket;
  private final SeparatedListImpl<ArrayPairTree> arrayPairs;
  private final InternalSyntaxToken closeBracket;

  public ArrayInitializerBracketTreeImpl(InternalSyntaxToken openBracket, SeparatedListImpl<ArrayPairTree> arrayPairs, InternalSyntaxToken closeBracket) {
    this.openBracket = openBracket;
    this.arrayPairs = arrayPairs;
    this.closeBracket = closeBracket;
  }

  @Override
  public SyntaxToken openBracketToken() {
    return openBracket;
  }

  @Override
  public SeparatedListImpl<ArrayPairTree> arrayPairs() {
    return arrayPairs;
  }

  @Override
  public SyntaxToken closeBracketToken() {
    return closeBracket;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(openBracket),
      arrayPairs.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closeBracket));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitArrayInitializerBracket(this);
  }

}
