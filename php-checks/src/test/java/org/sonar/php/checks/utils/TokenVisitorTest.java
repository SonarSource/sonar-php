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
package org.sonar.php.checks.utils;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class TokenVisitorTest {

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  void prevAndNextTokenNavigateAdjacentTokens() {
    Tree tree = parser.parse("if ($a) { foo(); }");
    TokenVisitor visitor = new TokenVisitor(tree);
    List<SyntaxToken> tokens = TokenVisitor.tokens(tree);

    SyntaxToken openParenthesis = tokens.get(1);
    SyntaxToken variable = tokens.get(2);
    SyntaxToken closeParenthesis = tokens.get(3);

    assertThat(visitor.prevToken(variable)).isEqualTo(openParenthesis);
    assertThat(visitor.nextToken(variable)).isEqualTo(closeParenthesis);
  }

  @Test
  void prevAndNextTokenReturnNullAtTreeBoundaries() {
    Tree tree = parser.parse("if ($a) { foo(); }");
    TokenVisitor visitor = new TokenVisitor(tree);
    List<SyntaxToken> tokens = TokenVisitor.tokens(tree);

    SyntaxToken firstToken = tokens.get(0);
    SyntaxToken lastToken = tokens.get(tokens.size() - 1);

    assertThat(visitor.prevToken(firstToken)).isNull();
    assertThat(visitor.nextToken(lastToken)).isNull();
  }

  @Test
  void prevAndNextTokenReturnNullOnNullInput() {
    Tree tree = parser.parse("if ($a) { foo(); }");
    TokenVisitor visitor = new TokenVisitor(tree);

    assertThat(visitor.prevToken(null)).isNull();
    assertThat(visitor.nextToken(null)).isNull();
  }

  @Test
  void firstKeywordFindsLeadingKeyword() {
    Tree tree = parser.parse("if ($a) { foo(); }");
    TokenVisitor visitor = new TokenVisitor(tree);

    assertThat(visitor.firstKeyword().text()).isEqualTo("if");
  }

  @Test
  void firstKeywordReturnsNullWhenNoKeywordPresent() {
    Tree tree = parser.parse("$a = 1;");
    TokenVisitor visitor = new TokenVisitor(tree);

    assertThat(visitor.firstKeyword()).isNull();
  }

}
