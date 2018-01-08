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
package org.sonar.php.tree.impl.declaration;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassPropertyDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  public void variable_declaration() throws Exception {
    ClassPropertyDeclarationTree tree = parse("public final $a, $b, $c;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(2);
    assertThat(tree.declarations()).hasSize(3);
    assertThat(tree.eosToken().text()).isEqualTo(";");

    assertThat(tree.hasModifiers("public", "final")).isTrue();
    assertThat(tree.hasModifiers("public")).isTrue();
    assertThat(tree.hasModifiers("public", "static")).isFalse();
    assertThat(tree.hasModifiers("static")).isFalse();
  }

  @Test
  public void constant_declaration() throws Exception {
    ClassPropertyDeclarationTree tree = parse("const A, B;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(1);
    assertThat(tree.declarations()).hasSize(2);
    assertThat(tree.eosToken().text()).isEqualTo(";");

    assertThat(tree.hasModifiers("const")).isTrue();
  }

  @Test
  public void private_constant_declaration() throws Exception {
    ClassPropertyDeclarationTree tree = parse("private const A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(2);
    assertThat(tree.modifierTokens().get(0).text()).isEqualTo("private");
    assertThat(tree.modifierTokens().get(1).text()).isEqualTo("const");
  }

}
