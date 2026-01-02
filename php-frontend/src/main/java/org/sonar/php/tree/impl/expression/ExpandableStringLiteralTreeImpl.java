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
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ExpandableStringLiteralTreeImpl extends PHPTree implements ExpandableStringLiteralTree {

  private static final Kind KIND = Kind.EXPANDABLE_STRING_LITERAL;

  private final SyntaxToken openDoubleQuote;
  private final List<ExpressionTree> elements;
  private final SyntaxToken closeDoubleQuote;

  public ExpandableStringLiteralTreeImpl(InternalSyntaxToken openDoubleQuote, List<ExpressionTree> elements, InternalSyntaxToken closeDoubleQuote) {
    this.openDoubleQuote = openDoubleQuote;
    this.elements = elements;
    this.closeDoubleQuote = closeDoubleQuote;

  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public SyntaxToken openDoubleQuoteToken() {
    return openDoubleQuote;
  }

  @Override
  public List<ExpandableStringCharactersTree> strings() {
    return elements.stream()
      .filter(ExpandableStringCharactersTree.class::isInstance)
      .map(ExpandableStringCharactersTree.class::cast)
      .toList();
  }

  @Override
  public List<ExpressionTree> expressions() {
    return elements.stream().filter(input -> !input.is(Kind.EXPANDABLE_STRING_CHARACTERS)).toList();
  }

  @Override
  public SyntaxToken closeDoubleQuoteToken() {
    return closeDoubleQuote;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(openDoubleQuote),
      elements.iterator(),
      IteratorUtils.iteratorOf(closeDoubleQuote));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitExpandableStringLiteral(this);
  }
}
