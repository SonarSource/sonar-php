/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.php.metrics;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;

import static org.fest.assertions.Assertions.assertThat;

public class ComplexityVisitorTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT, Charsets.UTF_8);

  @Test
  public void declarations() throws Exception {
    assertThat(complexity("function f() {}")).isEqualTo(1);
    assertThat(complexity("$f = function() {};")).isEqualTo(1);
    assertThat(complexity("class A {}")).isEqualTo(0);
    assertThat(complexity("class A { public function f() {} }")).isEqualTo(1);

    assertThat(complexity("function f() { f(); return; }")).isEqualTo(1);
    assertThat(complexity("function f() { return; f(); }")).isEqualTo(2);
    assertThat(complexity("class A { abstract function f(); }")).isEqualTo(1);
  }

  @Test
  public void statements() throws Exception {
    assertThat(complexity("$a = 0;")).isEqualTo(0);
    assertThat(complexity("if ($a) {}")).isEqualTo(1);
    assertThat(complexity("if ($a) {} else {}")).isEqualTo(1);
    assertThat(complexity("if ($a) {} else if($b) {} else {}")).isEqualTo(2);
    assertThat(complexity("if ($a): endif;")).isEqualTo(1);
    assertThat(complexity("for (;;) {}")).isEqualTo(1);
    assertThat(complexity("foreach ($a as $b) {}")).isEqualTo(1);
    assertThat(complexity("while ($a) {}")).isEqualTo(1);
    assertThat(complexity("do {} while($a);")).isEqualTo(1);
    assertThat(complexity("switch ($a) {}")).isEqualTo(0);
    assertThat(complexity("switch ($a) {case 1:}")).isEqualTo(1);
    assertThat(complexity("try {}")).isEqualTo(0);
    assertThat(complexity("try {} catch(E $s) {}")).isEqualTo(1);
    assertThat(complexity("return 1;")).isEqualTo(1);
    assertThat(complexity("throw e;")).isEqualTo(1);
    assertThat(complexity("goto x;")).isEqualTo(1);
  }

  @Test
  public void expressions() throws Exception {
    assertThat(complexity("$a;")).isEqualTo(0);
    assertThat(complexity("$a + $b;")).isEqualTo(0);
    assertThat(complexity("$f();")).isEqualTo(0);

    assertThat(complexity("$a || $b;")).isEqualTo(1);
    assertThat(complexity("$a && $b;")).isEqualTo(1);
    assertThat(complexity("$a or $b;")).isEqualTo(1);
    assertThat(complexity("$a and $b;")).isEqualTo(1);
    assertThat(complexity("$a ? $b : $c;")).isEqualTo(1);
  }

  @Test
  public void without_nested_functions() throws Exception {
    assertThat(complexityWithoutNestedFunctions("$a && $b && $c;")).isEqualTo(2);
    assertThat(complexityWithoutNestedFunctions("$a && f(function () { return $a && $b; });")).isEqualTo(1);
    assertThat(complexityWithoutNestedFunctions("function f() { f(function () { return $a && $b; }); $a && $b; }")).isEqualTo(2);
  }

  private int complexity(String toParse) {
    Tree tree = parser.parse(toParse);
    return NewComplexityVisitor.complexity(tree);
  }

  private int complexityWithoutNestedFunctions(String toParse) {
    Tree tree = parser.parse(toParse);
    return NewComplexityVisitor.complexityWithoutNestedFunctions(tree);
  }

}
