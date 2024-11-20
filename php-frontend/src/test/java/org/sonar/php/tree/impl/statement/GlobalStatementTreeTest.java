/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalStatementTreeTest extends PHPTreeModelTest {

  @Test
  void test() throws Exception {
    GlobalStatementTree tree = parse("global $a, $b;", PHPLexicalGrammar.GLOBAL_STATEMENT);

    assertThat(tree.is(Kind.GLOBAL_STATEMENT)).isTrue();
    assertThat(tree.globalToken().text()).isEqualTo("global");
    assertThat(tree.variables()).hasSize(2);
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
