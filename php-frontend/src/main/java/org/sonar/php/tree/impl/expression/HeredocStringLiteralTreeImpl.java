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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.HeredocStringLiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class HeredocStringLiteralTreeImpl extends PHPTree implements HeredocStringLiteralTree {

  private final SyntaxToken openingToken;
  private final List<ExpressionTree> elements;
  private final SyntaxToken closingToken;

  private static final Pattern pattern = Pattern.compile(LexicalConstant.HEREDOC);

  public HeredocStringLiteralTreeImpl(SyntaxToken tmpHeredocToken) {
    String tokenText = tmpHeredocToken.text();
    Matcher matcher = pattern.matcher(tokenText);
    Preconditions.checkArgument(matcher.matches());
    int openingTagEndIndex = matcher.end(1);
    String content = matcher.group(3);
    int contentStartIndex = matcher.start(3);
    int contentEndIndex = contentStartIndex == -1 ? openingTagEndIndex : matcher.end(3);
    if (content != null && content.length() > 0) {
      HeredocBody heredoc = (HeredocBody) PHPParserBuilder.createParser(PHPLexicalGrammar.HEREDOC_BODY, tmpHeredocToken.line()).parse(content);
      this.elements = heredoc.expressions();
    } else {
      this.elements = Collections.emptyList();
    }
    int startIndex = ((InternalSyntaxToken) tmpHeredocToken).startIndex();
    this.openingToken = new InternalSyntaxToken(
      tmpHeredocToken.line(),
      tmpHeredocToken.column(),
      tokenText.substring(0, openingTagEndIndex),
      tmpHeredocToken.trivias(),
      startIndex,
      false);

    SyntaxToken tokenBeforeClosingToken = openingToken;
    if (!this.elements.isEmpty()) {
      tokenBeforeClosingToken = ((PHPTree) this.elements.get(this.elements.size() - 1)).getLastToken();
    }
    String closingTag = tokenText.substring(contentEndIndex);
    this.closingToken = new InternalSyntaxToken(
      tokenBeforeClosingToken.endLine(),
      tokenBeforeClosingToken.endColumn(),
      closingTag,
      ImmutableList.of(),
      startIndex + tokenText.length() - closingTag.length(),
      false);
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(openingToken),
      elements.iterator(),
      Iterators.singletonIterator(closingToken));
  }

  @Override
  public SyntaxToken openingToken() {
    return openingToken;
  }

  @Override
  public List<ExpandableStringCharactersTree> strings() {
    return ImmutableList.copyOf(Iterables.filter(elements, ExpandableStringCharactersTree.class));
  }

  @Override
  public List<ExpressionTree> expressions() {
    return ImmutableList.copyOf(Iterables.filter(elements, input -> !input.is(Kind.HEREDOC_STRING_CHARACTERS)));
  }

  @Override
  public SyntaxToken closingToken() {
    return closingToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitHeredoc(this);
  }

  @Override
  public Kind getKind() {
    return Kind.HEREDOC_LITERAL;
  }

  public static class HeredocBody extends PHPTree {

    private final List<ExpressionTree> expressions;

    public HeredocBody(List<ExpressionTree> expressions) {
      this.expressions = expressions;
    }

    @Override
    public Kind getKind() {
      throw new UnsupportedOperationException("Used only internally for building the tree");
    }

    public List<ExpressionTree> expressions() {
      return expressions;
    }

    @Override
    public Iterator<Tree> childrenIterator() {
      // parsing utility class, it does not really have children
      return Collections.<Tree>emptyList().iterator();
    }

    @Override
    public void accept(VisitorCheck visitor) {
      throw new UnsupportedOperationException("Used only internally for building the tree");
    }

  }
}
