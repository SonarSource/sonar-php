/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassPropertyDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  public void variable_declaration() throws Exception {
    ClassPropertyDeclarationTree tree = parse("public final $a, $b, $c;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(2);
    assertThat(tree.typeAnnotation()).isNull();
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

  @Test
  public void type_annotation() throws Exception {
    ClassPropertyDeclarationTree tree = parse("public int $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.typeAnnotation().typeName().is(Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(tree.typeAnnotation().questionMarkToken()).isNull();
    assertThat(builtinType(tree)).isEqualTo("int");

    assertThat(((PHPTree) tree).childrenIterator()).containsExactly(
      tree.modifierTokens().get(0),
      tree.typeAnnotation(),
      tree.declarations().get(0),
      tree.eosToken()
    );
  }

  @Test
  public void type_annotation_classname() throws Exception {
    ClassPropertyDeclarationTree tree = parse("public MyClass $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.typeAnnotation().typeName().is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(((NamespaceNameTree) tree.typeAnnotation().typeName()).fullName()).isEqualTo("MyClass");
  }

  @Test
  public void type_annotation_self_parent() throws Exception {
    ClassPropertyDeclarationTree tree = parse("public self $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("self");

    tree = parse("public parent $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(builtinType(tree)).isEqualTo("parent");
  }

  @Test
  public void static_type_annotation() throws Exception {
    ClassPropertyDeclarationTree tree = parse("public static iterable $staticProp;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).extracting(SyntaxToken::text).containsExactly("public", "static");
    assertThat(builtinType(tree)).isEqualTo("iterable");
  }

  @Test
  public void type_annotation_default_value() throws Exception {
    ClassPropertyDeclarationTree tree = parse("private string $str = \"foo\";", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("string");
    assertThat(((LiteralTree) tree.declarations().get(0).initValue()).value()).isEqualTo("\"foo\"");
  }

  @Test
  public void type_annotation_var() throws Exception {
    ClassPropertyDeclarationTree tree = parse("var bool $flag;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("bool");
    assertThat(tree.declarations().get(0).identifier().text()).isEqualTo("$flag");
  }

  @Test
  public void type_annotation_nullable() throws Exception {
    ClassPropertyDeclarationTree tree = parse("public ?int $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    TypeTree type = tree.typeAnnotation();
    assertThat(type.questionMarkToken().text()).isEqualTo("?");
    assertThat(builtinType(tree)).isEqualTo("int");
  }

  private static String builtinType(ClassPropertyDeclarationTree tree) {
    return ((BuiltInTypeTree) tree.typeAnnotation().typeName()).token().text();
  }
}
