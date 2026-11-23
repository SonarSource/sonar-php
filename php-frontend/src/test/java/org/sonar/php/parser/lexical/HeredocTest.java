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
package org.sonar.php.parser.lexical;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class HeredocTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.HEREDOC)
      .matches("""
        <<<EOF
        <html> content </html>

        <p> content </p>
        EOF""")
      .matches("""
        <<< "EOD"
          content
        EOD""")
      .matches("<<<EOD\n"
        + "EOD")
      .matches("<<<EOD\n"
        + "\n"
        + "EOD")
      .matches("<<<EOD\n"
        + "\t EOD")
      .matches("""
        <<<EOD
            \s
             EOD""")
      .matches("""
        <<<EOD
             content
             EOD""")
      .matches("""
        <<<EOD
        \t\t content
        \t\t EOD""")
      .matches("""
        <<<END
        \t\t EN
        \t\t ENDING
        \t\t END""")
      .notMatches("<<<END\n"
        + "\t\t ")
      .notMatches("<<<END\n"
        + "\t\t ENDING")
      .notMatches("""
        <<<'EOD'
        content
        EOD""")
      .notMatches("<<<EOD \n"
        + "\n"
        + "EOD");
  }
}
