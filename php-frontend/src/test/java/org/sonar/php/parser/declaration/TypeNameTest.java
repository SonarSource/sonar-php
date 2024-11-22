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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class TypeNameTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.TYPE_NAME)
      .matches("self")
      .matches("static")
      .matches("array")
      .matches("callable")
      .matches("bool")
      .matches("float")
      .matches("int")
      .matches("string")
      .matches("object")
      .matches("MyClass")
      .matches("MyInterface")
      .matches("namespace\\MyInterface")
      .matches("\\A\\MyInterface")
      .matches("\\MyInterface")
      .matches("NS\\MyInterface");
  }
}
