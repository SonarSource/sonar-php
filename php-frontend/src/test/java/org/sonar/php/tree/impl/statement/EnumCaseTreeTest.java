/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumCaseTreeTest extends PHPTreeModelTest {

  @Test
  public void simple_case() {
    EnumCaseTreeImpl tree = parse("case A;", PHPLexicalGrammar.ENUM_CASE);
    assertThat(tree.is(Tree.Kind.ENUM_CASE)).isTrue();
    assertThat(tree.childrenIterator()).hasSize(5);
    assertThat(tree.caseToken()).hasToString("case");
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.equalToken()).isNull();
    assertThat(tree.value()).isNull();
    assertThat(tree.eosToken()).hasToString(";");
  }

  @Test
  public void enum_case_can_have_attributes() {
    EnumCaseTreeImpl tree = parse("#[A1(1)] case A;", PHPLexicalGrammar.ENUM_CASE);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes().get(0).name()).hasToString("A1");
  }

  @Test
  public void enum_case_with_value() {
    EnumCaseTreeImpl tree = parse("case A = 'A';", PHPLexicalGrammar.ENUM_CASE);
    assertThat(tree.equalToken()).hasToString("=");
    assertThat(tree.value().is(Tree.Kind.REGULAR_STRING_LITERAL)).isTrue();
  }
}
