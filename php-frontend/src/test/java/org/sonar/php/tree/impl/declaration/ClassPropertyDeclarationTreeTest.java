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
package org.sonar.php.tree.impl.declaration;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.Assertions;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class ClassPropertyDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void shouldParseVariable() {
    Assertions.assertThat(PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION)
      .matches("public $a;")
      .matches("public $a, $b, $c;")
      .matches("public final $a;")
      .matches("private $a;")
      .matches("protected $a;")
      .matches("public readonly $a;")
      .matches("public string $a;")
      .matches("public MyClass $a;")
      .matches("public self $a;")
      .matches("public static iterable $staticProp;")
      .matches("private string $str = \"foo\";")
      .matches("public ?int $a;")
      .matches("#[A1(3)] public int $a;")
      .matches("static $a;")
      .matches("readonly $a;")
      .matches("var $a;")

      .matches("final $a;")
      .matches("public private $a;")

      .notMatches("public final A;")
      .notMatches("$a;");
  }

  @Test
  void shouldConstDeclaration() {
    Assertions.assertThat(PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION)
      .matches("const A;")
      .matches("const A, B;")
      .matches("const string A;")
      .matches("const A = 'val';")
      .matches("const string A = 'val';")
      .matches("public const A;")
      .matches("#[A1(3)] const A;")

      .matches("public private const A;")

      .notMatches("const $a;")
      .notMatches("A;");
  }

  @Test
  void variableDeclaration() {
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
  void readonlyVariableDeclaration() {
    ClassPropertyDeclarationTree tree = parse("public readonly $prop;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(2);
    assertThat(tree.hasModifiers("public", "readonly")).isTrue();
    assertThat(tree.hasModifiers("readonly")).isTrue();
  }

  @Test
  void constantDeclaration() {
    ClassPropertyDeclarationTree tree = parse("const A, B;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(1);
    assertThat(tree.declarations()).hasSize(2);
    assertThat(tree.eosToken().text()).isEqualTo(";");

    assertThat(tree.hasModifiers("const")).isTrue();
  }

  @Test
  void constantDeclarationWithDeclaredType() {
    ClassPropertyDeclarationTree tree = parse("const string A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(1);
    assertThat(tree.declarations()).hasSize(1);
    assertThat(tree.eosToken().text()).isEqualTo(";");

    assertThat(tree.hasModifiers("const")).isTrue();
    assertThat(builtinType(tree)).isEqualTo("string");
  }

  @Test
  void privateConstantDeclaration() {
    ClassPropertyDeclarationTree tree = parse("private const A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(2);
    assertThat(tree.modifierTokens().get(0).text()).isEqualTo("private");
    assertThat(tree.modifierTokens().get(1).text()).isEqualTo("const");
  }

  @Test
  void finalConstantDeclaration() {
    ClassPropertyDeclarationTree tree = parse("final const A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(modifier(tree)).containsExactly("final", "const");
  }

  @Test
  void protectedFinalConstantDeclaration() {
    ClassPropertyDeclarationTree tree = parse("protected final const A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(modifier(tree)).containsExactly("protected", "final", "const");
  }

  @Test
  void typeAnnotation() {
    ClassPropertyDeclarationTree tree = parse("public int $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.typeAnnotation().typeName().is(Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(tree.typeAnnotation().questionMarkToken()).isNull();
    assertThat(builtinType(tree)).isEqualTo("int");

    assertThat(((PHPTree) tree).childrenIterator()).containsExactly(
      tree.modifierTokens().get(0),
      tree.typeAnnotation(),
      tree.declarations().get(0),
      tree.eosToken());
  }

  @Test
  void typeAnnotationClassname() {
    ClassPropertyDeclarationTree tree = parse("public MyClass $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.typeAnnotation().typeName().is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(((NamespaceNameTree) tree.typeAnnotation().typeName()).fullName()).isEqualTo("MyClass");
  }

  @Test
  void typeAnnotationSelfParent() {
    ClassPropertyDeclarationTree tree = parse("public self $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("self");

    tree = parse("public parent $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(builtinType(tree)).isEqualTo("parent");
  }

  @Test
  void staticTypeAnnotation() {
    ClassPropertyDeclarationTree tree = parse("public static iterable $staticProp;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).extracting(SyntaxToken::text).containsExactly("public", "static");
    assertThat(builtinType(tree)).isEqualTo("iterable");
  }

  @Test
  void typeAnnotationDefaultValue() {
    ClassPropertyDeclarationTree tree = parse("private string $str = \"foo\";", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("string");
    assertThat(((LiteralTree) tree.declarations().get(0).initValue()).value()).isEqualTo("\"foo\"");
  }

  @Test
  void typeAnnotationVar() {
    ClassPropertyDeclarationTree tree = parse("var bool $flag;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("bool");
    assertThat(tree.declarations().get(0).identifier().text()).isEqualTo("$flag");
  }

  @Test
  void typeAnnotationNullable() {
    ClassPropertyDeclarationTree tree = parse("public ?int $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    TypeTree type = tree.typeAnnotation();
    assertThat(type.questionMarkToken().text()).isEqualTo("?");
    assertThat(builtinType(tree)).isEqualTo("int");
  }

  @Test
  void variableWithAttributes() {
    ClassPropertyDeclarationTree tree = parse("#[A1(3)] public $x;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);

    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes().get(0).name()).hasToString("A1");
    assertThat(tree.attributeGroups().get(0).attributes().get(0).arguments()).hasSize(1);
  }

  @Test
  void constantWithAttributes() {
    ClassPropertyDeclarationTree tree = parse("#[A2(2, 3)] public const FOO = 'foo';", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);

    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes().get(0).name()).hasToString("A2");
    assertThat(tree.attributeGroups().get(0).attributes().get(0).arguments()).hasSize(2);
  }

  /**
   * Get list of all modifier token texts
   */
  private static List<String> modifier(ClassPropertyDeclarationTree classPropertyDeclaration) {
    return classPropertyDeclaration.modifierTokens().stream().map(SyntaxToken::text).collect(Collectors.toList());
  }

  private static String builtinType(ClassPropertyDeclarationTree tree) {
    return ((BuiltInTypeTree) tree.typeAnnotation().typeName()).token().text();
  }
}
