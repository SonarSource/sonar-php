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
package org.sonar.php.tree.impl.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class BreakStatementTreeTest extends PHPTreeModelTest {

  @Test
  void empty() {
    BreakStatementTree tree = parse("break ;", PHPLexicalGrammar.BREAK_STATEMENT);

    assertThat(tree.is(Kind.BREAK_STATEMENT)).isTrue();
    assertThat(tree.breakToken().text()).isEqualTo("break");
    assertThat(tree.argument()).isNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  void notEmpty() {
    BreakStatementTree tree = parse("break $a;", PHPLexicalGrammar.BREAK_STATEMENT);

    assertThat(tree.breakToken().text()).isEqualTo("break");
    assertThat(tree.argument()).isNotNull();
    assertThat(tree.argument().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
