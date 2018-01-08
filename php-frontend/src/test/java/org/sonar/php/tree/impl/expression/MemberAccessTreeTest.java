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
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberAccessTreeTest extends PHPTreeModelTest {

  @Test
  public void object_member_access() throws Exception {
    MemberAccessTree tree = parse("$obj->member", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.OBJECT_MEMBER_ACCESS)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("$obj");
    assertThat(tree.accessToken().text()).isEqualTo("->");
    assertThat(expressionToString(tree.member())).isEqualTo("member");
    assertThat(tree.isStatic()).isFalse();
  }

  @Test
  public void class_member_access() throws Exception {
    MemberAccessTree tree = parse("ClassName::MEMBER", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.CLASS_MEMBER_ACCESS)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("ClassName");
    assertThat(tree.accessToken().text()).isEqualTo("::");
    assertThat(expressionToString(tree.member())).isEqualTo("MEMBER");
    assertThat(tree.isStatic()).isTrue();
  }

}
