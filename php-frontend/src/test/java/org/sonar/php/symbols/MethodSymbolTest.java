/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.symbols;

import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.php.tree.impl.expression.AnonymousClassTreeImpl;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.symbols.SymbolTestUtils.parse;
import static org.sonar.php.tree.TreeUtils.firstDescendant;

class MethodSymbolTest {

  @Test
  void simpleMethod() {
    Tree ast = parse("<?php class A { function foo($p1) {} }");
    ClassSymbol a = Symbols.get(firstDescendant(ast, ClassDeclarationTreeImpl.class).get());
    MethodSymbol foo = firstDescendant(ast, MethodDeclarationTreeImpl.class).get().symbol();
    assertThat(a.declaredMethods().get(0)).isSameAs(foo);
    assertThat(foo.owner()).isSameAs(a);
    assertThat(foo.returnType().isPresent()).isFalse();
    assertThat(foo.returnType().isVoid()).isFalse();
  }

  @Test
  void methodInAnonymousClass() {
    Tree ast = parse("<?php $x = new class { function foo($p1) {} };");
    ClassSymbol anonymous = Symbols.get(firstDescendant(ast, AnonymousClassTreeImpl.class).get());
    MethodSymbol foo = firstDescendant(ast, MethodDeclarationTreeImpl.class).get().symbol();
    assertThat(anonymous.declaredMethods().get(0)).isSameAs(foo);
    assertThat(foo.owner()).isSameAs(anonymous);
    assertThat(foo.isTestMethod().isTrue()).isFalse();
  }

  @Test
  void isOverridingBySuperClasses() {
    Map<String, ClassSymbol> classes = parseMultipleClasses("<?php",
      "class A { function foo() {} }",
      "class B extends X { function foo() {} }",
      "class A1 extends A { function foo() {} function bar() {} }",
      "class A2 extends A { }",
      "class A21 extends A2 { function foo() {} }",
      "class C { function __construct() {} }",
      "class C1 extends C { function __construct() {} }");

    assertThat(classes.get("A").getDeclaredMethod("foo").isOverriding()).isEqualTo(Trilean.FALSE);
    assertThat(classes.get("B").getDeclaredMethod("foo").isOverriding()).isEqualTo(Trilean.UNKNOWN);
    assertThat(classes.get("A1").getDeclaredMethod("foo").isOverriding()).isEqualTo(Trilean.TRUE);
    assertThat(classes.get("A1").getDeclaredMethod("bar").isOverriding()).isEqualTo(Trilean.FALSE);
    assertThat(classes.get("A21").getDeclaredMethod("foo").isOverriding()).isEqualTo(Trilean.TRUE);
    assertThat(classes.get("C1").getDeclaredMethod("__construct").isOverriding()).isEqualTo(Trilean.FALSE);
  }

  @Test
  void isOverridingByInterfaces() {
    Map<String, ClassSymbol> classes = parseMultipleClasses("<?php",
      "interface I1 extends I2, I3, I4 {public function method1($a);}",
      "interface I2 {public function method2($a);}",
      "interface I3 {}",
      "abstract class A1 implements I1 {}",
      "abstract class A2 {}",
      "class C1 extends A1 implements I5 {",
      "public function method1($a) {}", // is implemented from I1 by extending A1
      "public function method2($a) {}", // is implemented from I2 through I1 by extending A1
      "public function method3($a) {}}", // is unknown because I4 is unknown
      "class C2 extends A3 {public function method4($a) {}}", // is unknown because A2 is unknown
      "class C3 implements I3 {public function method5($a) {}}", // is not implemented because not declared in I3
      "class C4 extends A2 {public function method6($a) {}}"); // is not implemented because A2 does not implement an interface

    assertThat(classes.get("C1").getDeclaredMethod("method1").isOverriding()).isEqualTo(Trilean.TRUE);
    assertThat(classes.get("C1").getDeclaredMethod("method2").isOverriding()).isEqualTo(Trilean.TRUE);
    assertThat(classes.get("C1").getDeclaredMethod("method3").isOverriding()).isEqualTo(Trilean.UNKNOWN);
    assertThat(classes.get("C2").getDeclaredMethod("method4").isOverriding()).isEqualTo(Trilean.UNKNOWN);
    assertThat(classes.get("C3").getDeclaredMethod("method5").isOverriding()).isEqualTo(Trilean.FALSE);
    assertThat(classes.get("C4").getDeclaredMethod("method6").isOverriding()).isEqualTo(Trilean.FALSE);
  }

  @Test
  void catchDeadLoopInIsDeclaredInInterface() {
    Map<String, ClassSymbol> classes = parseMultipleClasses("<?php",
      "interface I1 extends I2 {}",
      "interface I2 extends I1 {}",
      "class C1 implements I1 {public function method1(){}}",
      "class C2 extends C3 {public function method2(){}}",
      "class C3 extends C2 {}");

    assertThat(classes.get("C1").getDeclaredMethod("method1").isOverriding()).isEqualTo(Trilean.FALSE);
    assertThat(classes.get("C2").getDeclaredMethod("method2").isOverriding()).isEqualTo(Trilean.FALSE);
  }

  @Test
  void privateMethodDoesNotOverride() {
    Map<String, ClassSymbol> classes = parseMultipleClasses("<?php",
      "class C1 {public function method1(){}}",
      "class C2 extends C1 {private function method1(){}}");

    assertThat(classes.get("C2").getDeclaredMethod("method1").isOverriding()).isEqualTo(Trilean.FALSE);
  }

  @Test
  void privateMethodCanNotBeOverridden() {
    Map<String, ClassSymbol> classes = parseMultipleClasses("<?php",
      "class C1 {private function method1(){}}",
      "class C2 extends C1 {public function method1(){}}");

    assertThat(classes.get("C2").getDeclaredMethod("method1").isOverriding()).isEqualTo(Trilean.FALSE);
  }

  @Test
  void shouldDeserializeParameters() {
    Map<String, ClassSymbol> classes = parseMultipleClasses("<?php",
      "class C1 {private function method1($arg){}}",
      "class C2 {public function method1($arg2){}}",
      "class C3 {public function method1(...$arg3){}}",
      "class C4 {public function method1(int $arg){}}",
      "class C5 {public function method1(int $arg = 7){}}");

    Parameter arg1 = classes.get("C1").getDeclaredMethod("method1").parameters().get(0);
    Parameter arg2 = classes.get("C2").getDeclaredMethod("method1").parameters().get(0);
    Parameter arg3 = classes.get("C3").getDeclaredMethod("method1").parameters().get(0);
    Parameter arg4 = classes.get("C4").getDeclaredMethod("method1").parameters().get(0);
    Parameter arg5 = classes.get("C5").getDeclaredMethod("method1").parameters().get(0);
    Parameter arg6 = null;

    assertThat(arg1)
      .isNotNull()
      .isEqualTo(arg1)
      .isNotEqualTo(arg2)
      .isNotEqualTo(arg3)
      .isNotEqualTo(arg4)
      .isNotEqualTo(arg5)
      .isNotEqualTo(arg6)
      .isNotEqualTo((Object) classes.get("C1"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "<?php class A {public function testFoo(){}}",
    "<?php class A {#[PHPUnit\\Framework\\Attributes\\Test] public function foo(){}}",
    "<?php use PHPUnit\\Framework\\Attributes\\Test; class A {#[Test] public function foo(){}}",
    "<?php use PHPUnit\\Framework; class A {#[Framework\\Attributes\\Test] public function foo(){}}",
    "<?php class A {/** * @test */ public function foo(){}}",
  })
  void shouldIdentifyTestMethodInClass(String code) {
    Tree ast = parse(code);
    ClassSymbol classSymbol = Symbols.get(firstDescendant(ast, ClassDeclarationTreeImpl.class).get());
    MethodSymbol methodSymbol = firstDescendant(ast, MethodDeclarationTreeImpl.class).get().symbol();

    assertThat(classSymbol.declaredMethods().get(0)).isSameAs(methodSymbol);
    assertThat(methodSymbol.isTestMethod().isTrue()).isTrue();
  }

  private Map<String, ClassSymbol> parseMultipleClasses(String... lines) {
    Tree ast = parse(lines);
    return TreeUtils.descendants(ast, ClassDeclarationTreeImpl.class)
      .collect(Collectors.toMap(c -> c.name().text(), ClassDeclarationTreeImpl::symbol));
  }
}
