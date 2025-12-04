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

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class LiteralTreeTest extends PHPTreeModelTest {

  @Test
  void nullLiteral() {
    assertLiteral("NULL", Kind.NULL_LITERAL);
  }

  @Test
  void nowdocLiteral() {
    assertLiteral("<<<'EOD'\n content \nEOD", Kind.NOWDOC_LITERAL);
    assertLiteral("<<< 'EOD'\n content \nEOD", Kind.NOWDOC_LITERAL);
    assertLiteral("<<<'EOD'\nEOD", Kind.NOWDOC_LITERAL);
  }

  @Test
  void numericLiteral() {
    assertLiteral("0", Kind.NUMERIC_LITERAL);
  }

  @Test
  void regularStringLiteral() {
    assertLiteral("\"content\"", Kind.REGULAR_STRING_LITERAL);
  }

  @Test
  void booleanLiteral() {
    assertLiteral("true", Kind.BOOLEAN_LITERAL);
  }

  @Test
  void magicConstantLiteral() {
    assertLiteral("__FILE__", Kind.MAGIC_CONSTANT);
  }

  private void assertLiteral(String toParse, Kind expectedKind) {
    LiteralTreeImpl tree = parse(toParse, PHPLexicalGrammar.COMMON_SCALAR);

    assertThat(tree.is(expectedKind)).isTrue();
    assertThat(tree.value()).isEqualTo(toParse);
  }
}
