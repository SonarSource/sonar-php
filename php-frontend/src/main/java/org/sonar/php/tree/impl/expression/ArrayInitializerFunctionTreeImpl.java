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

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerFunctionTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ArrayInitializerFunctionTreeImpl extends PHPTree implements ArrayInitializerFunctionTree {

  private static final Kind KIND = Kind.ARRAY_INITIALIZER_FUNCTION;
  private final InternalSyntaxToken arrayToken;
  private final InternalSyntaxToken openParenthesis;
  private final SeparatedListImpl<ArrayPairTree> arrayPairs;
  private final InternalSyntaxToken closeParenthesis;

  public ArrayInitializerFunctionTreeImpl(
    InternalSyntaxToken arrayToken,
    InternalSyntaxToken openParenthesis,
    SeparatedListImpl<ArrayPairTree> arrayPairs,
    InternalSyntaxToken closeParenthesis) {
    this.arrayToken = arrayToken;
    this.openParenthesis = openParenthesis;
    this.arrayPairs = arrayPairs;
    this.closeParenthesis = closeParenthesis;
  }

  @Override
  public SyntaxToken arrayToken() {
    return arrayToken;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesis;
  }

  @Override
  public SeparatedListImpl<ArrayPairTree> arrayPairs() {
    return arrayPairs;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesis;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(arrayToken),
      IteratorUtils.iteratorOf(openParenthesis),
      arrayPairs.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closeParenthesis));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitArrayInitializerFunction(this);
  }

}
