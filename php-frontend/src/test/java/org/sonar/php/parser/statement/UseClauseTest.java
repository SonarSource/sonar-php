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
package org.sonar.php.parser.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class UseClauseTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.USE_CLAUSE)
      .matches("ArrayObject")
      .matches("My\\Full\\functionName")
      .matches("My\\Full\\functionName as func")
      .notMatches("My\\Full\\functionName as");
  }

  @Test
  void testGroupUseClause() {
    assertThat(PHPLexicalGrammar.GROUP_USE_CLAUSE)
      .matches("ArrayObject")
      .matches("My\\Full\\functionName")
      .matches("My\\Full\\functionName as func")
      .matches("function My\\Full\\Name as func")
      .matches("function My\\Full\\Name")
      .matches("const My\\Full\\Name as func")
      .matches("const My\\Full\\Name")
      .notMatches("My\\Full\\functionName as");
  }

}
