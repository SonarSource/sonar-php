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
package org.sonar.php.parser.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class MemberExpressionTest {

  @Test
  void test() {
    // callable_variable -> simple_variable
    assertThat(PHPLexicalGrammar.MEMBER_EXPRESSION)
      .matches("${3 + 2}")
      .matches("$a")
      .matches("a")
      .matches("$$a")

      // callable_variable -> dereferencable '[' optional_expr ']' -> variable '[' optional_expr ']'
      .matches("$a[]")
      .matches("$a[$b+2]")
      .matches("$a($b)[]")
      .matches("$a[0][]")
      // ??? callable_variable -> dereferencable '[' optional_expr ']' -> '(' expr ')' '[' optional_expr ']'
      // ??? callable_variable -> dereferencable '[' optional_expr ']' -> dereferencable_scalar '[' optional_expr ']'

      // callable_variable -> constant '[' optional_expr ']' -> name '[' optional_expr ']'
      .matches("a[3]")
      // callable_variable -> constant '[' optional_expr ']' -> class_name :: T_STRING '[' optional_expr ']'
      .matches("static::a[3]")

      // callable_variable -> dereferencable '{' expr '}' -> variable '{' expr '}'
      .matches("$a{3+2}")

      // callable_variable -> dereferencable T_OBJECT_OPERATOR member_name argument_list
      .matches("$a->$b($c)")
      // callable_variable -> function_call
      .matches("myfunction($a)")
      .matches("$a()")

      // static_member
      .matches("static::a")
      .matches("class1::a")
      .matches("class1::$a")
      .matches("class1::if")
      .matches("namespace1\\class1::a")
      // static_member -> dereferencable :: simple_variable
      .matches("$a::$b")

      // dereferencable T_OBJECT_OPERATOR member_name
      .matches("$a->b")
      .matches("$a->$b")
      .matches("$a->if")
      .matches("$a->b->$c")

      .notMatches("(int) $a")

      // new expression without parenthesis introduced in PHP 8.4
      .matches("new Foo()->bar")
      .matches("new Foo()->bar()")
      .matches("new Foo()::BAR")
      .matches("new Foo()::bar()")
      .matches("new Foo()::$bar")
      .matches("new Foo()[0]")
      .matches("new Foo(){0}")
      .matches("new Foo()()")

      .matches("new class {}->bar")
      .matches("new class {}->bar()")
      .matches("new class {}::BAR")
      .matches("new class {}::bar()")
      .matches("new class {}::$bar")
      .matches("new class {}[0]")
      .matches("new class {}{0}")
      .matches("new class {}()")

      // OLD TESTS

      .matches("$a[]")
      .matches("$a()")

      .matches("$a->b")
      .matches("$a->b()")

      .matches("$a::b()")
      .matches("Foo::$a")

      .matches("$a->$b[$c]{'d'}")

      .matches("$$a")
      .matches("${'a'}")
      .matches("$a{'a'}")
      .matches("$a[$b]")

      .matches("f()->$a")
      .matches("$a->{'b'}")

      .matches("A::class");
  }
}
