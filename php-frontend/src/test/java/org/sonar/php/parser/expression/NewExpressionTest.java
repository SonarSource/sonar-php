/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.parser.expression;

import org.junit.Test;
import org.sonar.plugins.php.api.tree.Tree;

import static org.sonar.php.utils.Assertions.assertThat;

public class NewExpressionTest {

  @Test
  public void test() {
    assertThat(Tree.Kind.NEW_EXPRESSION)
      .matches("new Foo")
      .matches("new Foo ()")
      .matches("new Foo ($x, $y)")
      .matches("new class {}")
      .matches("new $foo")
      .matches("new $foo()")
      .matches("new Foo::$bar()")
      .matches("new $foo::$bar()")
      .matches("new $foo[0]()")
      .matches("new $foo::$bar[0]()")
      .matches("new $this->bar()")
      .matches("new $foo->bar()")
      .matches("new $foo->bar[$key]()")
      .matches("new $foo->bar[$key][0]()")
      .matches("new Foo::$bar->foo()")

      .notMatches("new foo::bar()")
      .notMatches("new $a.'bar'()") // valid syntax - however, this is not only a new expression, but also a concat with a functions call
      .notMatches("new Foo::{'bar'}()")
      .notMatches("new static::Foo()")
      .notMatches("new Foo::class()")
    ;
  }

}
