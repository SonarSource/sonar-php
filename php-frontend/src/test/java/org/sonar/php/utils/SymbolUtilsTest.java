/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.utils;

import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.tree.TreeUtils.firstDescendant;


public class SymbolUtilsTest{

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  @Test
  public void verify_is_new_expression() {
    FunctionCallTree functionCallTree = get("<?php new Foo();", FunctionCallTree.class);
    assertThat(SymbolUtils.isNewExpressionCall(functionCallTree)).isTrue();
  }

  @Test
  public void verify_resolvable_inner_self_call() {
    MemberAccessTree memberAccessTree = get("<?php class Foo{function foo(){self::foo();}}", MemberAccessTree.class);
    assertThat(SymbolUtils.isResolvableInnerMemberAccess(memberAccessTree)).isTrue();
  }

  @Test
  public void verify_resolvable_inner_static_call() {
    MemberAccessTree memberAccessTree = get("<?php class Foo{function foo(){static::foo();}}", MemberAccessTree.class);
    assertThat(SymbolUtils.isResolvableInnerMemberAccess(memberAccessTree)).isTrue();
  }

  @Test
  public void verify_unresolvable_inner_call() {
    MemberAccessTree memberAccessTree = get("<?php class Foo{function foo(){self::$foo();}}", MemberAccessTree.class);
    assertThat(SymbolUtils.isResolvableInnerMemberAccess(memberAccessTree)).isFalse();
  }

  @Test
  public void verify_unresolvable_inner_self_call() {
    MemberAccessTree memberAccessTree = get("<?php class Foo{function foo(){Bar::foo();}}", MemberAccessTree.class);
    assertThat(SymbolUtils.isResolvableInnerMemberAccess(memberAccessTree)).isFalse();
  }

  @Test
  public void verify_unresolvable_inner_static_call() {
    MemberAccessTree memberAccessTree = get("<?php class Foo{function foo(){$foo::foo();}}", MemberAccessTree.class);
    assertThat(SymbolUtils.isResolvableInnerMemberAccess(memberAccessTree)).isFalse();
  }

  private <T extends Tree> T get(String content, Class<T> clazz) {
    return firstDescendant(getAst(content), clazz).get();
  }

  private Tree getAst(String content) {
    Tree ast = parser.parse(content);
    return ast;
  }
}
