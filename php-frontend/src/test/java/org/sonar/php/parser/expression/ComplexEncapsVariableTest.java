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
package org.sonar.php.parser.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class ComplexEncapsVariableTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.COMPLEX_ENCAPS_VARIABLE)
      .matches("{ $var }")
      .matches("{ ${expression} }");
  }

  @Test
  void testRealLife() {
    assertThat(PHPLexicalGrammar.COMPLEX_ENCAPS_VARIABLE)
      .matches("{ $var }")
      .matches("{ ${$var} }")
      .matches("{ $var[/* ... */ 42 - 2*21] }")
      .matches("{ ${method()} }")
      .matches("{ ${'test'} }")
      .matches("{ $foo['}'] }");
  }

  @Test
  void nok() {
    assertThat(PHPLexicalGrammar.COMPLEX_ENCAPS_VARIABLE)
      .notMatches("{$'str'}")
      .notMatches("{$0}");
  }

}
