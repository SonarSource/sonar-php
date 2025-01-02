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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class EnumDeclarationTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.ENUM_DECLARATION)
      .matches("enum A {}")
      .matches("enum A { case A; }")
      .matches("enum A {\n case A;\n case B; }")
      .matches("enum A { function foo() {} }")
      .matches("enum A { const CONSTANT = 'foo'; }")
      .matches("enum A implements B {}")
      .matches("enum A implements B,C {}")
      .matches("#[A1(1)] enum A {}")
      .matches("enum A: string {}")
      .notMatches("enum A {")
      .notMatches("enum A { case A}")
      .notMatches("enum A { public $property; }")
      .notMatches("enum A extends B { }")
      .notMatches("enum A: { }");
  }
}
