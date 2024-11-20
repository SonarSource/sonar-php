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
package org.sonar.php.parser.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class SimpleEncapsVariableTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.SIMPLE_ENCAPS_VARIABLE)
      .matches("$variable")
      .matches("$variable[offset]")
      .matches("$variable->identifier")
      .matches("$variable?->identifier");
  }

  @Test
  void testRealLife() {
    assertThat(PHPLexicalGrammar.SIMPLE_ENCAPS_VARIABLE)
      .matches("$foo[0]")
      .matches("$foo[identifier]")
      .matches("$foo[$variable]");

  }

  @Test
  void nok() {
    assertThat(PHPLexicalGrammar.SIMPLE_ENCAPS_VARIABLE)
      .notMatches("$var[test()]")
      .notMatches("$var[{$test}]")
      .notMatches("$var[$$test]");
  }

}
