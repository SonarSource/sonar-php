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
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class ContinueStatementTreeTest extends PHPTreeModelTest {

  @Test
  void empty() {
    ContinueStatementTree tree = parse("continue ;", PHPLexicalGrammar.CONTINUE_STATEMENT);

    assertThat(tree.is(Kind.CONTINUE_STATEMENT)).isTrue();
    assertThat(tree.continueToken().text()).isEqualTo("continue");
    assertThat(tree.argument()).isNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  void notEmpty() {
    ContinueStatementTree tree = parse("continue $a;", PHPLexicalGrammar.CONTINUE_STATEMENT);

    assertThat(tree.continueToken().text()).isEqualTo("continue");
    assertThat(tree.argument()).isNotNull();
    assertThat(tree.argument().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
