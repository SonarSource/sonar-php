/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.tree.impl.statement;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.DefaultClauseTree;

import static org.assertj.core.api.Assertions.assertThat;

public class SwitchClauseTreeTest extends PHPTreeModelTest {

  @Test
  public void case_clause() throws Exception {
    CaseClauseTree tree = parse("case $a: $b;", PHPLexicalGrammar.SWITCH_CASE_CLAUSE);

    assertThat(tree.is(Kind.CASE_CLAUSE)).isTrue();
    assertThat(tree.caseToken().text()).isEqualTo("case");
    assertThat(tree.expression().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.caseSeparatorToken().text()).isEqualTo(":");
    assertThat(tree.statements()).hasSize(1);
  }

  @Test
  public void default_clause() throws Exception {
    DefaultClauseTree tree = parse("default: $b; break;", PHPLexicalGrammar.SWITCH_CASE_CLAUSE);

    assertThat(tree.is(Kind.DEFAULT_CLAUSE)).isTrue();
    assertThat(tree.caseToken().text()).isEqualTo("default");
    assertThat(tree.caseSeparatorToken().text()).isEqualTo(":");
    assertThat(tree.statements()).hasSize(2);
  }

}
