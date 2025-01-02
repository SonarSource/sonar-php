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
package org.sonar.php.tree.impl.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnStatementTreeTest extends PHPTreeModelTest {

  @Test
  void empty() {
    ReturnStatementTree tree = parse("return ;", PHPLexicalGrammar.RETURN_STATEMENT);

    assertThat(tree.is(Kind.RETURN_STATEMENT)).isTrue();
    assertThat(tree.returnToken().text()).isEqualTo("return");
    assertThat(tree.expression()).isNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  void notEmpty() {
    ReturnStatementTree tree = parse("return $a;", PHPLexicalGrammar.RETURN_STATEMENT);

    assertThat(tree.returnToken().text()).isEqualTo("return");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.expression().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
