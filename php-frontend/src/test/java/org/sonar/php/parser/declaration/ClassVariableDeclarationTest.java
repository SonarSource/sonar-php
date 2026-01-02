/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class ClassVariableDeclarationTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION)
      .matches("var $a;")
      .matches("var $a, $b;")
      .matches("public static $a;")
      .matches("public int $id;")
      .matches("protected ClassName $classType;")
      .matches("public static iterable $staticProp;")
      .matches("var bool $flag;")
      .matches("public string $str = \"foo\";")
      .matches("public float $x, $y;")
      .matches("public self $x;")
      .matches("public parent $x;")
      .matches("public object $x;")
      .matches("public array $x;")
      .matches("public array|int $x;")
      .matches("public A&B $x;")
      .notMatches("public static var $a;")
      .notMatches("const $a;");
  }
}
