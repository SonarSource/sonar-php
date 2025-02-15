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
package org.sonar.php.parser;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import com.sonar.sslr.api.typed.NodeBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.impl.lexical.InternalSyntaxTrivia;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.VisitorCheck;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class PHPNodeBuilder implements NodeBuilder {

  public static final char BYTE_ORDER_MARK = '\uFEFF';
  private int lineOffset;

  public PHPNodeBuilder(int lineOffset) {
    this.lineOffset = lineOffset;
  }

  public PHPNodeBuilder() {
    this.lineOffset = 0;
  }

  @Override
  public Object createNonTerminal(GrammarRuleKey ruleKey, Rule rule, List<Object> children, int startIndex, int endIndex) {
    for (Object child : children) {
      if (child instanceof InternalSyntaxToken) {
        return child;
      }
    }
    return new InternalSyntaxSpacing();
  }

  @Override
  public Object createTerminal(Input input, int startIndex, int endIndex, List<Trivia> trivias, TokenType type) {
    char[] fileChars = input.input();
    boolean hasByteOrderMark = fileChars.length > 0 && fileChars[0] == BYTE_ORDER_MARK;
    boolean isEof = GenericTokenType.EOF.equals(type);
    LineColumnValue lineColumnValue = tokenPosition(input, startIndex, endIndex);
    return new InternalSyntaxToken(
      lineColumnValue.line + lineOffset,
      column(hasByteOrderMark, lineColumnValue.line, lineColumnValue.column),
      lineColumnValue.value,
      createTrivias(trivias, hasByteOrderMark),
      startIndex - (hasByteOrderMark ? 1 : 0),
      isEof);
  }

  private static int column(boolean hasByteOrderMark, int line, int column) {
    if (hasByteOrderMark && line == 1) {
      return column - 1;
    }
    return column;
  }

  private static List<SyntaxTrivia> createTrivias(List<Trivia> trivias, boolean hasByteOrderMark) {
    List<SyntaxTrivia> result = new ArrayList<>();
    for (Trivia trivia : trivias) {
      Token trivialToken = trivia.getToken();
      int column = column(hasByteOrderMark, trivialToken.getLine(), trivialToken.getColumn());
      result.add(InternalSyntaxTrivia.create(trivialToken.getValue(), trivialToken.getLine(), column));
    }
    return result;
  }

  private static LineColumnValue tokenPosition(Input input, int startIndex, int endIndex) {
    int[] lineAndColumn = input.lineAndColumnAt(startIndex);
    String value = input.substring(startIndex, endIndex);
    return new LineColumnValue(lineAndColumn[0], lineAndColumn[1] - 1, value);
  }

  private static class LineColumnValue {
    final int line;
    final int column;
    final String value;

    private LineColumnValue(int line, int column, String value) {
      this.line = line;
      this.column = column;
      this.value = value;
    }
  }

  private static class InternalSyntaxSpacing extends PHPTree {

    @Override
    public void accept(VisitorCheck visitor) {
      // nothing to do
    }

    @Override
    public Kind getKind() {
      return Kind.TRIVIA;
    }

    @Override
    public Iterator<Tree> childrenIterator() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLeaf() {
      return true;
    }

  }

}
