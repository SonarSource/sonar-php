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
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

class EnumCaseTreeTest extends PHPTreeModelTest {

  @Test
  void simpleCase() {
    EnumCaseTreeImpl tree = parse("case A;", PHPLexicalGrammar.ENUM_CASE);
    assertThat(tree.is(Tree.Kind.ENUM_CASE)).isTrue();
    assertThat(tree.childrenIterator()).toIterable().hasSize(5);
    assertThat(tree.caseToken()).hasToString("case");
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.equalToken()).isNull();
    assertThat(tree.value()).isNull();
    assertThat(tree.eosToken()).hasToString(";");
  }

  @Test
  void enumCaseCanHaveAttributes() {
    EnumCaseTreeImpl tree = parse("#[A1(1)] case A;", PHPLexicalGrammar.ENUM_CASE);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes().get(0).name()).hasToString("A1");
  }

  @Test
  void enumCaseWithValue() {
    EnumCaseTreeImpl tree = parse("case A = 'A';", PHPLexicalGrammar.ENUM_CASE);
    assertThat(tree.equalToken()).hasToString("=");
    assertThat(tree.value().is(Tree.Kind.REGULAR_STRING_LITERAL)).isTrue();
  }
}
