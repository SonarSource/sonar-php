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
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import static org.sonar.php.utils.Assertions.assertThat;

class InternalFunctionTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.INTERNAL_FUNCTION)
      .matches("isset ($a, $b)")
      .matches("isset ($a, $b,)")
      .notMatches("unset ($a, $b)")
      .notMatches("isset (,)")
      .matches("empty ($a)")
      .matches("include $a")
      .matches("include_once $a")
      .matches("require $a")
      .matches("require_once $a")
      .matches("clone $a")
      .matches("clone($a)")
      .matches("clone (Foo::bar($x), ['a' => 1,\n 'b' => 2,\n])")
      .matches("clone($a, ['a' => 1],)")
      .notMatches("clone($a,)")
      .matches("print $a");
  }

  @Test
  void cloneWithArguments() {
    FunctionCallTree withArguments = parse("clone($a, ['a' => 1])");
    org.assertj.core.api.Assertions.assertThat(withArguments.callArguments()).hasSize(2);
    org.assertj.core.api.Assertions.assertThat(withArguments.openParenthesisToken()).isNotNull();

    // plain clone($a) must keep its shape (parenthesized single argument, no call parentheses), relied on by ConstructWithParenthesesCheck
    FunctionCallTree plain = parse("clone($a)");
    org.assertj.core.api.Assertions.assertThat(plain.callArguments()).hasSize(1);
    org.assertj.core.api.Assertions.assertThat(plain.openParenthesisToken()).isNull();
  }

  private static FunctionCallTree parse(String code) {
    return (FunctionCallTree) PHPParserBuilder.createParser(PHPLexicalGrammar.INTERNAL_FUNCTION).parse(code);
  }
}
