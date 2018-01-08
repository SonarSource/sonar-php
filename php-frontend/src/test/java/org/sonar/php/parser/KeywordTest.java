/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.parser;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.sonar.php.api.PHPKeyword;

import static org.sonar.php.utils.Assertions.assertThat;

public class KeywordTest {

  @Test
  public void test() {
    // Exact list of keywords from PHP reference manual
    assertThat(PHPLexicalGrammar.KEYWORDS)
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
      .matches("namespace")
      .matches("new")
      .matches("or")
      .matches("print")
      .matches("private")
      .matches("protected")
      .matches("public")
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
  public void getKeywordValues() {
    Assertions.assertThat(PHPKeyword.getKeywordValues().length).isEqualTo(67);
  }

}
