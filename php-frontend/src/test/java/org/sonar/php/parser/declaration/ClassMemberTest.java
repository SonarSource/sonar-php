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

class ClassMemberTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.CLASS_MEMBER)
      .matches("var $a;")
      .matches("const A;")
      .matches("private function f() {}")
      .matches("public readonly string $prop;")
      .matches("public int|null|(A&B) $a;")
      .matches("public(set) string $a;")
      .matches("protected(set) string $a;")
      .matches("private(set) string $a;")
      .notMatches("public( set) string $a;") // spaces are not accepted
      .notMatches("public(set ) string $a;")
      .notMatches("public( set ) string $a;");
    ;
  }
}
