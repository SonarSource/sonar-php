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
package org.sonar.php.symbols;

import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.php.tree.impl.expression.FunctionCallTreeImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;

import static org.sonar.php.symbols.FunctionSymbolAssert.assertThat;
import static org.sonar.php.symbols.SymbolTestUtils.parse;
import static org.sonar.php.tree.TreeUtils.descendants;

class MethodCallResolutionTest {

  @Test
  void resolveThis() {
    assertThat(callSymbol("<?php class A { function f(){} function g(){ $this->f(); } }")).isKnown("A::f");
    assertThat(callSymbol("<?php class A { function f(){} function g(){ $this->x(); } }")).isUnknown();
    assertThat(callSymbol("<?php class A { function f(){} function g(){ $this->$f(); } }")).isUnknown();
    assertThat(callSymbol("<?php class A { function f(){} function g(){ $notThis->f(); } }")).isUnknown();
  }

  @Test
  void resolveThisWithInheritance() {
    assertThat(callSymbol("<?php ",
      "class A { function f(){} }",
      "class B extends A { function g(){ $this->f(); } }")).isKnown("A::f");
    assertThat(callSymbol("<?php ",
      "class A { function f(){} }",
      "class B extends A { function f(){ $this->f(); } }")).isKnown("B::f");
  }

  @Test
  void resolveWithSuperclassCycle() {
    assertThat(callSymbol("<?php ",
      "class A extends B { }",
      "class B extends A { function g(){ $this->f(); } }")).isUnknown();
  }

  @Test
  void resolveSelfAndStatic() {
    assertThat(callSymbol("<?php class A { function f(){} function g(){ self::f(); } }")).isKnown("A::f");
    assertThat(callSymbol("<?php class A { function f(){} function g(){ self::x(); } }")).isUnknown();
    assertThat(callSymbol("<?php class A { function f(){} function g(){ $self::f(); } }")).isUnknown();
    assertThat(callSymbol("<?php class A { function f(){} function g(){ self::$f(); } }")).isUnknown();
    assertThat(callSymbol("<?php class A { function f(){} function g(){ static::f(); } }")).isKnown("A::f");
    assertThat(callSymbol("<?php class A { function f(){} function g(){ static::x(); } }")).isUnknown();
    assertThat(callSymbol("<?php class A { function f(){} function g(){ $static::f(); } }")).isUnknown();
    assertThat(callSymbol("<?php class A { function f(){} function g(){ static::$f(); } }")).isUnknown();
  }

  private FunctionSymbol callSymbol(String... lines) {
    CompilationUnitTree root = parse(lines);
    List<FunctionCallTreeImpl> descendants = descendants(root, FunctionCallTreeImpl.class).collect(Collectors.toList());
    Assertions.assertThat(descendants).hasSize(1);
    return descendants.get(0).symbol();
  }
}
