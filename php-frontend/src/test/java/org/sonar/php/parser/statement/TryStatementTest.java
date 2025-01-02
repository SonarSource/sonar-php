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
package org.sonar.php.parser.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class TryStatementTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.TRY_STATEMENT)
      .matches("try {}")
      .matches("try {} finally {}")
      .matches("try {} catch(Exception $e) {}")
      .matches("try {} catch(Exception) {}")
      .matches("try {} catch(Exception1 $e1) {} catch(Exception2 $e2) {}")
      .matches("try {} catch(\\NS\\Exception $e1) {}")
      .notMatches("try {} catch(finally $e1) {}");
  }
}
