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
package org.sonar.php.tree.impl.expression;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

public class YieldExpressionTreeTest extends PHPTreeModelTest {

  @Test
  public void yield_value() throws Exception {
    YieldExpressionTree tree = parse("yield $a", Kind.YIELD_EXPRESSION);

    assertThat(tree.is(Kind.YIELD_EXPRESSION)).isTrue();

    assertThat(tree.yieldToken().text()).isEqualTo("yield");
    assertThat(tree.fromToken()).isNull();
    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(expressionToString(tree.value())).isEqualTo("$a");
  }

  @Test
  public void yield_key_value() throws Exception {
    YieldExpressionTree tree = parse("yield $a=>$b", Kind.YIELD_EXPRESSION);

    assertThat(tree.is(Kind.YIELD_EXPRESSION)).isTrue();

    assertThat(tree.yieldToken().text()).isEqualTo("yield");
    assertThat(tree.fromToken()).isNull();
    assertThat(expressionToString(tree.key())).isEqualTo("$a");
    assertThat(expressionToString(tree.doubleArrowToken())).isEqualTo("=>");
    assertThat(expressionToString(tree.value())).isEqualTo("$b");
  }

  @Test
  public void yield_null() throws Exception {
    YieldExpressionTree tree = parse("yield", Kind.YIELD_EXPRESSION);

    assertThat(tree.is(Kind.YIELD_EXPRESSION)).isTrue();
    assertThat(tree.yieldToken().text()).isEqualTo("yield");
    assertThat(tree.fromToken()).isNull();
    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(tree.value()).isNull();
  }

  @Test
  public void yield_from() throws Exception {
    YieldExpressionTree tree = parse("yield from foo()", Kind.YIELD_EXPRESSION);

    assertThat(tree.is(Kind.YIELD_EXPRESSION)).isTrue();

    assertThat(tree.yieldToken().text()).isEqualTo("yield");
    assertThat(tree.fromToken()).isNotNull();
    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(expressionToString(tree.value())).isEqualTo("foo()");
  }
}
