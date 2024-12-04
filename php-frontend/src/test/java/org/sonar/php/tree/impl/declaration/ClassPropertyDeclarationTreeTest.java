/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

      // all possible asymmetric visibility
      .matches("public(set) string $a;")
      .matches("protected(set) string $a;")
      .matches("private(set) string $a;")

      // any order is possible for modifiers, including asymmetric visibility
      .matches("readonly public protected(set) final string $prop;")
      .matches("public readonly protected(set) final string $prop;")
      .matches("readonly protected(set) public final string $prop;")
      .matches("final readonly public protected(set) string $prop;")
      .matches("readonly public final protected(set) string $prop;")

      // not valid php, but we still parse these
      .matches("final $a;")
      .matches("public private $a;")
      .matches("private public(set) string $a;")

      .matches("public string $a { get; }")
      .matches("public string $a { get { return $this-> a + 1; } }")
      .matches("public string $a { final set($value) => $value - 1; }")

      .notMatches("public final A;")
      .notMatches("$a;")
      .notMatches("public( set ) string $a;");
  }

  @Test
  void shouldSupportConstDeclaration() {
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
  void shouldSupportVariableDeclaration() {
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
  void shouldSupportReadonlyVariableDeclaration() {
    ClassPropertyDeclarationTree tree = parse("public readonly $prop;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(2);
    assertThat(tree.hasModifiers("public", "readonly")).isTrue();
    assertThat(tree.hasModifiers("readonly")).isTrue();
  }

  @Test
  void shouldSupportConstantDeclaration() {
    ClassPropertyDeclarationTree tree = parse("const A, B;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(1);
    assertThat(tree.declarations()).hasSize(2);
    assertThat(tree.eosToken().text()).isEqualTo(";");

    assertThat(tree.hasModifiers("const")).isTrue();
  }

  @Test
  void shouldSupportConstantDeclarationWithDeclaredType() {
    ClassPropertyDeclarationTree tree = parse("const string A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(1);
    assertThat(tree.declarations()).hasSize(1);
    assertThat(tree.eosToken().text()).isEqualTo(";");

    assertThat(tree.hasModifiers("const")).isTrue();
    assertThat(builtinType(tree)).isEqualTo("string");
  }

  @Test
  void shouldSupportPrivateConstantDeclaration() {
    ClassPropertyDeclarationTree tree = parse("private const A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).hasSize(2);
    assertThat(tree.modifierTokens().get(0).text()).isEqualTo("private");
    assertThat(tree.modifierTokens().get(1).text()).isEqualTo("const");
  }

  @Test
  void shouldSupportFinalConstantDeclaration() {
    ClassPropertyDeclarationTree tree = parse("final const A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(modifier(tree)).containsExactly("final", "const");
  }

  @Test
  void shouldSupportProtectedFinalConstantDeclaration() {
    ClassPropertyDeclarationTree tree = parse("protected final const A;", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(modifier(tree)).containsExactly("protected", "final", "const");
  }

  @Test
  void shouldSupportTypeAnnotation() {
    ClassPropertyDeclarationTree tree = parse("public int $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.typeAnnotation().typeName().is(Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(tree.typeAnnotation().questionMarkToken()).isNull();
    assertThat(builtinType(tree)).isEqualTo("int");

    assertThat(((PHPTree) tree).childrenIterator()).toIterable().containsExactly(
      tree.modifierTokens().get(0),
      tree.typeAnnotation(),
      tree.declarations().get(0),
      tree.eosToken());
  }

  @Test
  void shouldSupportTypeAnnotationClassname() {
    ClassPropertyDeclarationTree tree = parse("public MyClass $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.typeAnnotation().typeName().is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(((NamespaceNameTree) tree.typeAnnotation().typeName()).fullName()).isEqualTo("MyClass");
  }

  @Test
  void shouldSupportTypeAnnotationSelfParent() {
    ClassPropertyDeclarationTree tree = parse("public self $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("self");

    tree = parse("public parent $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(builtinType(tree)).isEqualTo("parent");
  }

  @Test
  void shouldSupportStaticTypeAnnotation() {
    ClassPropertyDeclarationTree tree = parse("public static iterable $staticProp;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).extracting(SyntaxToken::text).containsExactly("public", "static");
    assertThat(builtinType(tree)).isEqualTo("iterable");
  }

  @Test
  void shouldSupportTypeAnnotationDefaultValue() {
    ClassPropertyDeclarationTree tree = parse("private string $str = \"foo\";", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("string");
    assertThat(((LiteralTree) tree.declarations().get(0).initValue()).value()).isEqualTo("\"foo\"");
  }

  @Test
  void shouldSupportTypeAnnotationVar() {
    ClassPropertyDeclarationTree tree = parse("var bool $flag;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(builtinType(tree)).isEqualTo("bool");
    assertThat(tree.declarations().get(0).identifier().text()).isEqualTo("$flag");
  }

  @Test
  void shouldSupportTypeAnnotationNullable() {
    ClassPropertyDeclarationTree tree = parse("public ?int $id;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    TypeTree type = tree.typeAnnotation();
    assertThat(type.questionMarkToken().text()).isEqualTo("?");
    assertThat(builtinType(tree)).isEqualTo("int");
  }

  @Test
  void shouldSupportVariableWithAttributes() {
    ClassPropertyDeclarationTree tree = parse("#[A1(3)] public $x;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);

    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes().get(0).name()).hasToString("A1");
    assertThat(tree.attributeGroups().get(0).attributes().get(0).arguments()).hasSize(1);
  }

  @Test
  void shouldSupportConstantWithAttributes() {
    ClassPropertyDeclarationTree tree = parse("#[A2(2, 3)] public const FOO = 'foo';", PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION);

    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes().get(0).name()).hasToString("A2");
    assertThat(tree.attributeGroups().get(0).attributes().get(0).arguments()).hasSize(2);
  }

  @Test
  void shouldSupportPropertyHook() {
    ClassPropertyDeclarationTree tree = parse("public string $a { get { return $this-> a + 1; } final set($value) => $value - 1; }", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);

    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(((ClassPropertyDeclarationTreeImpl) tree).childrenIterator()).toIterable().hasSize(4);
    assertThat(tree.attributeGroups()).isEmpty();
    assertThat(tree.modifierTokens()).hasSize(1);
    assertThat(tree.declarations()).hasSize(1);
    assertThat(tree.propertyHookList()).isNotNull();
    assertThat(tree.propertyHookList().openCurlyBrace()).isNotNull();
    assertThat(tree.propertyHookList().hooks()).hasSize(2);
    assertThat(tree.propertyHookList().closeCurlyBrace()).isNotNull();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  void shouldSupportAsymmetricVisibilityModifier() {
    ClassPropertyDeclarationTree tree = parse("protected(set) string $staticProp;", PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION);
    assertThat(tree.is(Kind.CLASS_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.modifierTokens()).extracting(SyntaxToken::text).containsExactly("protected(set)");
    assertThat(builtinType(tree)).isEqualTo("string");
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
