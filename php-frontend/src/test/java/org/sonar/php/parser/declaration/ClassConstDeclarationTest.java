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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class ClassConstDeclarationTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION)
      .matches("const A = 1 ;")
      .matches("const A = 1, B = 2 ;")
      .matches("const A;")
      .matches("const A, B;")
      .matches("const if = 1;")
      .matches("const bar = [\"bar\" => 3];")
      .matches("const bar = [\"bar\" => 3][\"bar\"];")
      .matches("public const A = 1;")
      .matches("protected const A = 1;")
      .matches("private const A = 1;")
      .matches("final const A = 1;")
      .matches("private final const A = 2;")
      .notMatches("static const A = 1;");
  }
}
