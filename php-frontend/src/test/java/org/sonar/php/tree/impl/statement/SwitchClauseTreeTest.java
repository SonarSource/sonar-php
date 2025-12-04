/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.DefaultClauseTree;

import static org.assertj.core.api.Assertions.assertThat;

class SwitchClauseTreeTest extends PHPTreeModelTest {

  @Test
  void caseClause() {
    CaseClauseTree tree = parse("case $a: $b;", PHPLexicalGrammar.SWITCH_CASE_CLAUSE);

    assertThat(tree.is(Kind.CASE_CLAUSE)).isTrue();
    assertThat(tree.caseToken().text()).isEqualTo("case");
    assertThat(tree.expression().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.caseSeparatorToken().text()).isEqualTo(":");
    assertThat(tree.statements()).hasSize(1);
  }

  @Test
  void defaultClause() {
    DefaultClauseTree tree = parse("default: $b; break;", PHPLexicalGrammar.SWITCH_CASE_CLAUSE);

    assertThat(tree.is(Kind.DEFAULT_CLAUSE)).isTrue();
    assertThat(tree.caseToken().text()).isEqualTo("default");
    assertThat(tree.caseSeparatorToken().text()).isEqualTo(":");
    assertThat(tree.statements()).hasSize(2);
  }

}
