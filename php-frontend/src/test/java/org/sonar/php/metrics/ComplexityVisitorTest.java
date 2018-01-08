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
package org.sonar.php.metrics;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.List;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

public class ComplexityVisitorTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  public void declarations() throws Exception {
    assertOneComplexityToken("function f() {}", "function");
    assertOneComplexityToken("$f = function() {};", "function");
    assertThat(complexity("class A {}")).isEqualTo(0);
    assertOneComplexityToken("class A { public function f() {} }", "function");

    assertThat(complexity("function f() { f(); return; }")).isEqualTo(1);
    assertThat(complexity("function f() { return; f(); }")).isEqualTo(1);
    assertThat(complexity("class A { abstract function f(); }")).isEqualTo(1);
  }

  @Test
  public void statements() throws Exception {
    assertThat(complexity("$a = 0;")).isEqualTo(0);
    assertOneComplexityToken("if ($a) {}", "if");
    assertOneComplexityToken("if ($a) {} else {}", "if");
    assertThat(complexity("if ($a) {} else if($b) {} else {}")).isEqualTo(2);
    assertOneComplexityToken("if ($a): endif;", "if");
    assertOneComplexityToken("for (;;) {}", "for");
    assertOneComplexityToken("foreach ($a as $b) {}", "foreach");
    assertOneComplexityToken("while ($a) {}", "while");
    assertOneComplexityToken("do {} while($a);", "do");
    assertThat(complexity("switch ($a) {}")).isEqualTo(0);
    assertOneComplexityToken("switch ($a) {case 1:}", "case");
    assertThat(complexity("switch ($a) {default:}")).isEqualTo(0);

    assertThat(complexity("try {}")).isEqualTo(0);
    assertThat(complexity("try {} catch(E $s) {}")).isEqualTo(0);
    assertThat(complexity("return 1;")).isEqualTo(0);
    assertThat(complexity("throw e;")).isEqualTo(0);
    assertThat(complexity("goto x;")).isEqualTo(0);
  }

  @Test
  public void expressions() throws Exception {
    assertThat(complexity("$a;")).isEqualTo(0);
    assertThat(complexity("$a + $b;")).isEqualTo(0);
    assertThat(complexity("$f();")).isEqualTo(0);

    assertOneComplexityToken("$a || $b;", "||");
    assertOneComplexityToken("$a && $b;", "&&");
    assertOneComplexityToken("$a or $b;", "or");
    assertOneComplexityToken("$a and $b;", "and");
    assertOneComplexityToken("$a ? $b : $c;", "?");
  }

  @Test
  public void without_nested_functions() throws Exception {
    assertThat(complexityWithoutNestedFunctions("$a && $b && $c;")).isEqualTo(2);
    assertThat(complexityWithoutNestedFunctions("$a && f(function () { return $a && $b; });")).isEqualTo(1);
    assertThat(complexityWithoutNestedFunctions("function f() { f(function () { return $a && $b; }); $a && $b; }")).isEqualTo(2);
  }

  private void assertOneComplexityToken(String codeToParse, String complexityToken) {
    Tree tree = parser.parse(codeToParse);
    List<Tree> trees = ComplexityVisitor.complexityTrees(tree);

    assertThat(trees).hasSize(1);
    assertThat(((SyntaxToken) trees.get(0)).text()).isEqualTo(complexityToken);
  }

  private int complexity(String toParse) {
    Tree tree = parser.parse(toParse);
    return ComplexityVisitor.complexity(tree);
  }

  private int complexityWithoutNestedFunctions(String toParse) {
    Tree tree = parser.parse(toParse);
    return ComplexityVisitor.complexityNodesWithoutNestedFunctions(tree).size();
  }

}
