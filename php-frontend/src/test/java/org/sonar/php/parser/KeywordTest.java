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
package org.sonar.php.parser;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.php.api.PHPKeyword;

import static org.sonar.php.utils.Assertions.assertThat;

class KeywordTest {

  @Test
  void test() {
    // Exact list of keywords from PHP reference manual
    assertThat(PHPLexicalGrammar.KEYWORDS)
      .matches("__halt_compiler")
      .matches("abstract")
      .matches("and")
      .matches("array")
      .matches("as")
      .matches("break")
      .matches("callable")
      .matches("case")
      .matches("catch")
      .matches("class")
      .matches("clone")
      .matches("const")
      .matches("continue")
      .matches("declare")
      .matches("default")
      .matches("die")
      .matches("do")
      .matches("echo")
      .matches("else")
      .matches("elseif")
      .matches("empty")
      .matches("enddeclare")
      .matches("endfor")
      .matches("endforeach")
      .matches("endif")
      .matches("endswitch")
      .matches("endwhile")
      .matches("eval")
      .matches("exit")
      .matches("extends")
      .matches("final")
      .matches("finally")
      .matches("fn")
      .matches("for")
      .matches("foreach")
      .matches("function")
      .matches("global")
      .matches("goto")
      .matches("if")
      .matches("implements")
      .matches("include")
      .matches("include_once")
      .matches("instanceof")
      .matches("insteadof")
      .matches("interface")
      .matches("isset")
      .matches("list")
      .matches("match")
      .matches("namespace")
      .matches("new")
      .matches("or")
      .matches("print")
      .matches("private")
      .matches("protected")
      .matches("public")
      .matches("readonly")
      .matches("require")
      .matches("require_once")
      .matches("return")
      .matches("static")
      .matches("switch")
      .matches("throw")
      .matches("trait")
      .matches("try")
      .matches("unset")
      .matches("use")
      .matches("var")
      .matches("while")
      .matches("xor")
      .matches("yield");
  }

  @Test
  void shouldMatchCaseInsensitiveKeywords() {
    assertThat(PHPLexicalGrammar.KEYWORDS)
      .matches("AbStRaCt")
      .matches("AND");
  }

  @Test
  void getKeywordValues() {
    Assertions.assertThat(PHPKeyword.getKeywordValues()).hasSize(70);
  }

}
