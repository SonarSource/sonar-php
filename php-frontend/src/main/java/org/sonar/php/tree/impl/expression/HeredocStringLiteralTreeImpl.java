/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.utils.Preconditions;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
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
      Collections.emptyList(),
      startIndex + tokenText.length() - closingTag.length(),
      false);
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(openingToken),
      elements.iterator(),
      IteratorUtils.iteratorOf(closingToken));
  }

  @Override
  public SyntaxToken openingToken() {
    return openingToken;
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
    return elements.stream().filter(input -> !input.is(Kind.HEREDOC_STRING_CHARACTERS)).toList();
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
