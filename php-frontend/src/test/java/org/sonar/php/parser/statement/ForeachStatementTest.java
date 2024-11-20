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

class ForeachStatementTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.FOREACH_STATEMENT)
      .matches("foreach ($a as $b) {}")
      .matches("foreach ($a as $b) : {} endforeach ;")
      .matches("foreach ($a as $b) : {} {} endforeach ;")
      .matches("foreach ($a as $b) ;")
      .matches("foreach ($a as $b => $c) {}")
      .matches("foreach ($a as $b => $c) : {} endforeach ;")
      .matches("foreach ($a as [$b, $c]) {}")

      .notMatches("foreach ($a as $b) : {} endfor ;")
      .notMatches("foreach ($a as $b) : {} endforeach")
      .notMatches("foreach ($a as $b) {} {}");
  }
}
