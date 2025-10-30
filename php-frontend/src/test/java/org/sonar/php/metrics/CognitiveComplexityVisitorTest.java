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
package org.sonar.php.metrics;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.metrics.CognitiveComplexityVisitor.CognitiveComplexity;
import org.sonar.php.metrics.CognitiveComplexityVisitor.ComplexityComponent;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;

import static org.assertj.core.api.Assertions.assertThat;

class CognitiveComplexityVisitorTest extends PHPTreeModelTest {

  @Test
  void ifElse() {
    assertThat(complexity("if ($a) {}")).isEqualTo(1);
    assertThat(complexity("if ($a): endif;")).isEqualTo(1);

    assertThat(complexity("if ($a) {} elseif ($a) {} elseif ($a) {} else {} ")).isEqualTo(4);
    assertThat(complexity("if ($a) {} else if ($a) {} else if ($a) {} else {} ")).isEqualTo(4);
    assertThat(complexity("if ($a) {} elseif ($a) {} else if ($a) {} else {} ")).isEqualTo(4);

    assertThat(complexity("if ($a): elseif ($a): elseif ($a): else: endif; ")).isEqualTo(4);

    assertThat(components("if ($a) {} else {}")).containsExactly(cc("if", 1), cc("else", 1));
  }

  @Test
  void ifElseNesting() {
    assertThat(complexity("if ($a) { if ($a) {} }")).isEqualTo(3);
    assertThat(complexity("if ($a): if ($a) {} endif;")).isEqualTo(3);

    assertThat(complexity("if ($a) {} else { if ($a) {} }")).isEqualTo(4);
    assertThat(complexity("if ($a):  else: if ($a) {} endif;")).isEqualTo(4);

    assertThat(complexity("if ($a) {} elseif ($a) { if ($a) {} }")).isEqualTo(4);
    assertThat(complexity("if ($a):  elseif ($a): if ($a) {} endif;")).isEqualTo(4);

    assertThat(complexity("if ($a) { if ($a) {} elseif ($a) {} else {} }")).isEqualTo(5);

    assertThat(components("if ($a) { if ($a) {} }")).containsExactly(cc("if", 1), cc("if", 2));
  }

  @Test
  void switchStatement() {
    assertThat(complexity("switch ($a) { case 1: break; case 2: break; default: }")).isEqualTo(1);
    assertThat(complexity("switch ($a) { case 1: if ($a) {} }")).isEqualTo(3);
    assertThat(complexity("if ($a) { switch ($a) {  } } ")).isEqualTo(3);

    assertThat(components("switch ($a) { }")).containsExactly(cc("switch", 1));
  }

  @Test
  void whileLoop() {
    assertThat(complexity("while ($a) {}")).isEqualTo(1);
    assertThat(complexity("while ($a):  endwhile;")).isEqualTo(1);

    assertThat(complexity("while ($a) { if ($a) {} }")).isEqualTo(3);
    assertThat(complexity("while ($a): if($a){} if($a){} endwhile;")).isEqualTo(5);

    assertThat(complexity("if ($a) { while ($a) {} }")).isEqualTo(3);

    assertThat(components("while ($a) {}")).containsExactly(cc("while", 1));
  }

  @Test
  void doWhileLoop() {
    assertThat(complexity("do {} while ($a);")).isEqualTo(1);
    assertThat(complexity("do { if ($a) {} } while ($a);")).isEqualTo(3);
    assertThat(complexity("if ($a) { do {} while ($a); }")).isEqualTo(3);

    assertThat(components("do {} while ($a);")).containsExactly(cc("do", 1));
  }

  @Test
  void foreachLoop() {
    assertThat(complexity("foreach ($a as $b) {}")).isEqualTo(1);
    assertThat(complexity("foreach ($a as $b => $c) {}")).isEqualTo(1);
    assertThat(complexity("foreach ($a as $b): endforeach;")).isEqualTo(1);

    assertThat(complexity("foreach ($a as $b) { if ($a) {} }")).isEqualTo(3);
    assertThat(complexity("foreach ($a as $b): if($a){} if($a){} endforeach;")).isEqualTo(5);

    assertThat(complexity("if ($a) { foreach ($a as $b) {} }")).isEqualTo(3);

    assertThat(components("foreach ($a as $b) {}")).containsExactly(cc("foreach", 1));
  }

  @Test
  void forLoop() {
    assertThat(complexity("for (;;) {}")).isEqualTo(1);
    assertThat(complexity("for (;;): endfor;")).isEqualTo(1);

    assertThat(complexity("for (;;) { if ($a) {} }")).isEqualTo(3);
    assertThat(complexity("for (;;): if($a){} if($a){} endfor;")).isEqualTo(5);

    assertThat(complexity("if ($a) { for (;;) {} }")).isEqualTo(3);

    assertThat(components("for (;;) {}")).containsExactly(cc("for", 1));
  }

  @Test
  void tryCatch() {
    assertThat(complexity("try {} finally {}")).isZero();
    assertThat(complexity("try {} catch (Exc $e) {} ")).isEqualTo(1);
    assertThat(complexity("try {} catch (Exc $e) {} catch (Exc $e) {}")).isEqualTo(2);

    assertThat(complexity("try { if($a) {} }")).isEqualTo(1);
    assertThat(complexity("try {} finally { if($a) {} }")).isEqualTo(1);
    assertThat(complexity("try {} catch (Exc $e) { if($a) {} }")).isEqualTo(3);

    assertThat(complexity("if ($a) { try {} catch (Exc $e) {} }")).isEqualTo(3);

    assertThat(components("try {} catch (Exc $e) {} ")).containsExactly(cc("catch", 1));
  }

  @Test
  void conditionalExpression() {
    assertThat(complexity("$a = $b ? 1 : 2; ")).isEqualTo(1);
    assertThat(complexity("$a = $b ? 1 : ($b ? 1 : 2); ")).isEqualTo(3);
    assertThat(complexity("$a = $b ? ($b ? 1 : 2) : 2; ")).isEqualTo(3);

    assertThat(components("$a = $b ? 1 : 2;")).containsExactly(cc("?", 1));
  }

  @Test
  void jumps() {
    assertThat(complexity(" return 42;")).isZero();
    assertThat(complexity(" return;")).isZero();
    assertThat(complexity(" break; ")).isZero();
    assertThat(complexity(" continue; ")).isZero();

    assertThat(complexity(" continue 42; ")).isEqualTo(1);
    assertThat(complexity(" break 42; ")).isEqualTo(1);
    assertThat(complexity(" goto a; ")).isEqualTo(1);

    assertThat(complexity(" if ($a) { continue 42; } ")).isEqualTo(2);
    assertThat(complexity(" if ($a) { break 42; } ")).isEqualTo(2);
    assertThat(complexity(" if ($a) { goto a; } ")).isEqualTo(2);

    assertThat(components(" continue 42; ")).containsExactly(cc("continue", 1));
    assertThat(components(" break 42; ")).containsExactly(cc("break", 1));
    assertThat(components(" goto a; ")).containsExactly(cc("goto", 1));
  }

  @Test
  void logicalOperators() {
    assertThat(complexity(" 1 && 2;")).isEqualTo(1);
    assertThat(complexity(" 1 && 2 && 3 && 4 && 5;")).isEqualTo(1);

    assertThat(complexity(" 1 || 2;")).isEqualTo(1);
    assertThat(complexity(" 1 || 2 || 3 || 4 || 5;")).isEqualTo(1);

    assertThat(complexity(" (1 || 2) || 3;")).isEqualTo(1);
    assertThat(complexity(" 1 && (2 && 3);")).isEqualTo(1);

    assertThat(complexity(" 1 && 2 || 3;")).isEqualTo(2);
    assertThat(complexity(" 1 && 2 || 3 || 4;")).isEqualTo(2);
    assertThat(complexity(" 1 || 2 || 3 && 4;")).isEqualTo(2);
    assertThat(complexity(" 1 && 2 || 3 && 4;")).isEqualTo(3);
    assertThat(complexity(" 1 || 2 && 3 || 4;")).isEqualTo(3);

    assertThat(complexity(" 1 || 2 && (3 || 4);")).isEqualTo(3);
    assertThat(complexity(" 1 || 2 && foo(3 && 4);")).isEqualTo(3);

    assertThat(components(" 1 && 2 || 3 || 4; ")).containsExactly(cc("&&", 1), cc("||", 1));
  }

  @Test
  void nestedFunctions() {
    assertThat(components(" function nestedFunction() { if ($a) {} } ")).containsExactly(cc("if", 2));
    assertThat(components(" foo(function() { if ($a) {} });")).containsExactly(cc("if", 2));
    assertThat(components(" foo(function() { return $a && $b; });")).containsExactly(cc("&&", 1));
    assertThat(components(" foo(fn() => $a && $b);")).containsExactly(cc("&&", 1));
  }

  @Test
  void recursion() {
    // recursion is not supported yet, should be 1
    assertThat(complexity(" f(); ")).isZero();
  }

  @Test
  void noComplexity() {
    assertThat(complexity(" foo(); ")).isZero();
  }

  @Test
  void fileComplexityIsSumOfFunctions() {
    File file = new File("src/test/resources/metrics/file_cognitive_complexity.php");
    ActionParser<Tree> p = PHPParserBuilder.createParser(PHPLexicalGrammar.COMPILATION_UNIT);
    CompilationUnitTree cut = (CompilationUnitTree) p.parse(file);
    int complexity = CognitiveComplexityVisitor.complexity(cut);
    assertThat(complexity).isEqualTo(
      1 // foo
        + 3 // bar (incl. qix)
        + 2 // gul (incl. function expression)
        + 1 // dom
        + 1 // $func function expression
        + 1 // $func arrow function expression
        + 4 // rest of the script
        + 2 // pipe operators
    );
  }

  private int complexity(String functionBody) {
    FunctionTree tree = parse("function f() { " + functionBody + " }", PHPLexicalGrammar.FUNCTION_DECLARATION);
    CognitiveComplexity complexity = CognitiveComplexityVisitor.complexity(tree);
    return complexity.getValue();
  }

  private List<TestComplexityComponent> components(String functionBody) {
    FunctionTree tree = parse("function f() { " + functionBody + " }", PHPLexicalGrammar.FUNCTION_DECLARATION);
    CognitiveComplexity complexity = CognitiveComplexityVisitor.complexity(tree);
    return complexity.getComplexityComponents().stream().map(TestComplexityComponent::create).collect(Collectors.toList());
  }

  private static class TestComplexityComponent {

    String tokenValue;
    int addedComplexity;

    public TestComplexityComponent(String tokenValue, int addedComplexity) {
      this.tokenValue = tokenValue;
      this.addedComplexity = addedComplexity;
    }

    static TestComplexityComponent create(ComplexityComponent complexityComponent) {
      return new TestComplexityComponent(complexityComponent.tree().toString(), complexityComponent.addedComplexity());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TestComplexityComponent that = (TestComplexityComponent) o;

      if (addedComplexity != that.addedComplexity) {
        return false;
      }
      return tokenValue.equals(that.tokenValue);

    }

    @Override
    public int hashCode() {
      int result = tokenValue.hashCode();
      result = 31 * result + addedComplexity;
      return result;
    }
  }

  static TestComplexityComponent cc(String tokenValue, int addedComplexity) {
    return new TestComplexityComponent(tokenValue, addedComplexity);
  }

}
