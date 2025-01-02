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
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class DoWhileStatementTreeTest extends PHPTreeModelTest {

  @Test
  void test() throws Exception {
    DoWhileStatementTree tree = parse("do {} while ($a) ;", PHPLexicalGrammar.DO_WHILE_STATEMENT);

    assertThat(tree.is(Kind.DO_WHILE_STATEMENT)).isTrue();
    assertThat(tree.doToken().text()).isEqualTo("do");
    assertThat(tree.condition().is(Kind.PARENTHESISED_EXPRESSION)).isTrue();
    assertThat(tree.statement().is(Kind.BLOCK)).isTrue();
    assertThat(tree.whileToken().text()).isEqualTo("while");
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
