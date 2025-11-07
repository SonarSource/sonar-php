/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.parser.expression;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.sonar.php.utils.Assertions.assertThat;

class ExpandableStringLiteralTest {

  @Test
  void test() {
    assertThat(Kind.EXPANDABLE_STRING_LITERAL)
      .matches("\"$var\"")
      .matches("\"str $var\"")
      .matches("\"$var $var\"")
      .matches("\"$var str\"")
      .matches("\"no escape for `backtick` \"")
      .matches("\"$var str $var\"");
  }

  @Test
  void executionOperator() {
    assertThat(Kind.EXECUTION_OPERATOR)
      .matches("`$var`")
      .matches("`without expression`")
      .matches("`str $var`")
      .matches("`$var $var`")
      .matches("`$var str`")
      .matches("`no escape for quotes \" '`")
      .matches("`$var str $var`");
  }

  @Test
  void testRealLife() {
    assertThat(Kind.EXPANDABLE_STRING_LITERAL)
      .matches("\"{$var[\"foo\"]}\"");
  }

}
