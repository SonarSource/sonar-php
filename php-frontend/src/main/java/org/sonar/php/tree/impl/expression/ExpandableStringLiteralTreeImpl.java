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

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

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
    return ImmutableList.copyOf(Iterables.filter(elements, ExpandableStringCharactersTree.class));
  }

  @Override
  public List<ExpressionTree> expressions() {
    return ImmutableList.copyOf(Iterables.filter(elements, new Predicate<ExpressionTree>() {
      @Override
      public boolean apply(@Nullable ExpressionTree input) {
        return input != null ? !input.is(Kind.EXPANDABLE_STRING_CHARACTERS) : false;
      }
    }));
  }

  @Override
  public SyntaxToken closeDoubleQuoteToken() {
    return closeDoubleQuote;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(openDoubleQuote),
      elements.iterator(),
      Iterators.singletonIterator(closeDoubleQuote));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitExpandableStringLiteral(this);
  }
}
