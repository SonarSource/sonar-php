/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;

import static org.assertj.core.api.Assertions.assertThat;

class MemberAccessTreeTest extends PHPTreeModelTest {

  @Test
  void objectMemberAccess() {
    MemberAccessTree tree = parse("$obj->member", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.OBJECT_MEMBER_ACCESS)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("$obj");
    assertThat(tree.accessToken().text()).isEqualTo("->");
    assertThat(expressionToString(tree.member())).isEqualTo("member");
    assertThat(tree.isStatic()).isFalse();
    assertThat(tree.isNullSafeObjectAccess()).isFalse();
  }

  @Test
  void nullSafeMemberAccess() {
    MemberAccessTree tree = parse("$obj?->member", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.OBJECT_MEMBER_ACCESS)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("$obj");
    assertThat(tree.accessToken().text()).isEqualTo("?->");
    assertThat(expressionToString(tree.member())).isEqualTo("member");
    assertThat(tree.isStatic()).isFalse();
    assertThat(tree.isNullSafeObjectAccess()).isTrue();
  }

  @Test
  void classMemberAccess() {
    MemberAccessTree tree = parse("ClassName::MEMBER", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.CLASS_MEMBER_ACCESS)).isTrue();
    assertThat(expressionToString(tree.object())).isEqualTo("ClassName");
    assertThat(tree.accessToken().text()).isEqualTo("::");
    assertThat(expressionToString(tree.member())).isEqualTo("MEMBER");
    assertThat(tree.isStatic()).isTrue();
    assertThat(tree.isNullSafeObjectAccess()).isFalse();
  }

  @Test
  void objectMemberClassNameAccess() {
    MemberAccessTree tree = parse("$obj::class", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.CLASS_MEMBER_ACCESS)).isTrue();
  }
}
