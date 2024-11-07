/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterTreeTest extends PHPTreeModelTest {

  @Test
  void shouldSupportOnlyIdentifier() {
    ParameterTree tree = parse("$param1", PHPLexicalGrammar.PARAMETER);
    assertThat(tree.is(Kind.PARAMETER)).isTrue();
    assertThat(tree.type()).isNull();
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.ellipsisToken()).isNull();
    assertThat(tree.variableIdentifier().variableExpression().text()).isEqualTo("$param1");
    assertThat(tree.equalToken()).isNull();
    assertThat(tree.initValue()).isNull();
    assertThat(tree.readonlyToken()).isNull();
    assertThat(tree.isReadonly()).isFalse();
    assertThat(tree.isPropertyPromotion()).isFalse();
  }

  @Test
  void shouldSupportFullExample() {
    ParameterTree tree = parse("Class1&...$param1=$value1", PHPLexicalGrammar.PARAMETER);
    assertThat(tree.type().typeName().is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.referenceToken().text()).isEqualTo("&");
    assertThat(tree.ellipsisToken().text()).isEqualTo("...");
    assertThat(tree.variableIdentifier().variableExpression().text()).isEqualTo("$param1");
    assertThat(tree.equalToken().text()).isEqualTo("=");
    assertThat(tree.initValue().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
  }

  @Test
  void shouldSupportWithAttributes() {
    ParameterTree tree = parse("#[A1(5)] #[A2(5)] int $a", PHPLexicalGrammar.PARAMETER);
    assertThat(tree.attributeGroups()).hasSize(2);
  }

  @Test
  void shouldSupportInitProperty() {
    ParameterTree tree = parse("private int $a", PHPLexicalGrammar.PARAMETER);
    assertThat(tree.isPropertyPromotion()).isTrue();
    assertThat(tree.visibility().text()).isEqualTo("private");
    assertThat(tree.declaredType()).isNotNull();
    assertThat(tree.readonlyToken()).isNull();
    assertThat(tree.isReadonly()).isFalse();
  }

  @Test
  void shouldSupportInitReadonlyProperty() {
    ParameterTree tree = parse("private readonly int $a", PHPLexicalGrammar.PARAMETER);
    assertThat(tree.isPropertyPromotion()).isTrue();
    assertThat(tree.visibility().text()).isEqualTo("private");
    assertThat(tree.declaredType()).isNotNull();
    assertThat(tree.readonlyToken()).isNotNull();
    assertThat(tree.isReadonly()).isTrue();
  }

  @Test
  void shouldSupportPropertyHookList() {
    ParameterTree tree = parse("int $a { get; set => 123; }", PHPLexicalGrammar.PARAMETER);
    assertThat(tree.is(Kind.PARAMETER)).isTrue();
    assertThat(((ParameterTreeImpl) tree).childrenIterator()).toIterable().hasSize(7);
    assertThat(tree.propertyHookList()).isNotNull();
    assertThat(tree.propertyHookList().openCurlyBrace()).isNotNull();
    assertThat(tree.propertyHookList().hooks()).hasSize(2);
    assertThat(tree.propertyHookList().closeCurlyBrace()).isNotNull();
  }
}
