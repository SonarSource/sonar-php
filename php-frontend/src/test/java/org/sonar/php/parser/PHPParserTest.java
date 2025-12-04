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
package org.sonar.php.parser;

import com.sonar.sslr.api.typed.ActionParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PHPParserTest {

  @Test
  void shouldParseExpression() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.EXPRESSION);
    BinaryExpressionTree tree = (BinaryExpressionTree) parser.parse("$a + $b + $c");
    Assertions.assertThat(tree.getParent()).isNull();
    Assertions.assertThat(tree.leftOperand().getParent()).isSameAs(tree);
    Assertions.assertThat(tree.rightOperand().getParent()).isSameAs(tree);

    BinaryExpressionTree subExpression = (BinaryExpressionTree) tree.leftOperand();
    Assertions.assertThat(subExpression.leftOperand().getParent()).isSameAs(subExpression);
    Assertions.assertThat(subExpression.rightOperand().getParent()).isSameAs(subExpression);
  }

  @Test
  void shouldParseForEach() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.FOREACH_STATEMENT);
    ForEachStatementTree tree = (ForEachStatementTree) parser.parse("foreach ($arr as &$value) { }");
    Assertions.assertThat(tree.expression().getParent()).isSameAs(tree);
  }

  @Test
  void shouldParseAsymmetricVisibilityInConstructor() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.COMPILATION_UNIT);
    assertDoesNotThrow(() -> parser.parse("""
      <?php
      class Book
      {
        public function __construct(
          public private(set) string $title,
              public protected(set) string $author,
              protected private(set) int $pubYear,
          ) {}
      }"""));
  }
}
