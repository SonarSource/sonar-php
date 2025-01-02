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
import org.sonar.plugins.php.api.tree.statement.StaticStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class StaticStatementTreeTest extends PHPTreeModelTest {

  @Test
  void test() throws Exception {
    StaticStatementTree tree = parse("static $a, $b = $c;", PHPLexicalGrammar.STATIC_STATEMENT);

    assertThat(tree.is(Kind.STATIC_STATEMENT)).isTrue();
    assertThat(tree.staticToken().text()).isEqualTo("static");
    assertThat(tree.variables()).hasSize(2);
    assertThat(tree.variables().get(0).equalToken()).isNull();
    assertThat(tree.variables().get(1).equalToken()).isNotNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
