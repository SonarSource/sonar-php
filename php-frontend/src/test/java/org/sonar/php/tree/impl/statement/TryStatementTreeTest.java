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
package org.sonar.php.tree.impl.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class TryStatementTreeTest extends PHPTreeModelTest {

  @Test
  void test_2_catch_blocks() {
    TryStatementTree tree = parse("try {} catch(Exception1 $e1) {} catch(Exception2 $e2) {}", PHPLexicalGrammar.TRY_STATEMENT);

    assertThat(tree.is(Kind.TRY_STATEMENT)).isTrue();
    assertThat(tree.tryToken().text()).isEqualTo("try");
    assertThat(expressionToString(tree.block())).isEqualTo("{}");
    assertThat(tree.catchBlocks()).hasSize(2);
    assertThat(tree.finallyToken()).isNull();
    assertThat(tree.finallyBlock()).isNull();
  }

  @Test
  void testFinallyBlock() {
    TryStatementTree tree = parse("try {} finally {}", PHPLexicalGrammar.TRY_STATEMENT);

    assertThat(tree.is(Kind.TRY_STATEMENT)).isTrue();
    assertThat(tree.tryToken().text()).isEqualTo("try");
    assertThat(expressionToString(tree.block())).isEqualTo("{}");
    assertThat(tree.catchBlocks()).isEmpty();
    assertThat(tree.finallyToken().text()).isEqualTo("finally");
    assertThat(expressionToString(tree.finallyBlock())).isEqualTo("{}");
  }

}
