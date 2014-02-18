/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.php.parser.RuleTest;

public class PHPKeywordTest extends RuleTest {

  @Before
  public void setUp() {
    p.setRootRule(p.getGrammar().rule(PHPGrammar.KEYWORDS));
  }

  @Test
  public void test() {
    // Exact list of keywords from PHP reference manual
      matches("__halt_compiler");
      matches("abstract");
      matches("and");
      matches("array");
      matches("as");
      matches("break");
      matches("callable");
      matches("case");
      matches("catch");
      matches("class");
      matches("clone");
      matches("const");
      matches("continue");
      matches("declare");
      matches("default");
      matches("die");
      matches("do");
      matches("echo");
      matches("else");
      matches("elseif");
      matches("empty");
      matches("enddeclare");
      matches("endfor");
      matches("endforeach");
      matches("endif");
      matches("endswitch");
      matches("endwhile");
      matches("eval");
      matches("exit");
      matches("extends");
      matches("final");
      matches("for");
      matches("foreach");
      matches("function");
      matches("global");
      matches("goto");
      matches("if");
      matches("implements");
      matches("include");
      matches("include_once");
      matches("instanceof");
      matches("insteadof");
      matches("interface");
      matches("isset");
      matches("list");
      matches("namespace");
      matches("new");
      matches("or");
      matches("print");
      matches("private");
      matches("protected");
      matches("public");
      matches("require");
      matches("require_once");
      matches("return");
      matches("static");
      matches("switch");
      matches("throw");
      matches("trait");
      matches("try");
      matches("unset");
      matches("use");
      matches("var");
      matches("while");
      matches("xor");
  }

  @Test
  public void getKeywordValues() {
    Assertions.assertThat(PHPKeyword.getKeywordValues().length).isGreaterThan(0);

  }
}
