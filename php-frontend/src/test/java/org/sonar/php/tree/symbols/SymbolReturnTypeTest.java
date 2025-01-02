/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.tree.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;

import static org.fest.assertions.Assertions.assertThat;

class SymbolReturnTypeTest extends ParsingTestUtils {

  @Test
  void testNotDefined() {
    SymbolReturnType returnType = SymbolReturnType.notDefined();
    assertThat(returnType.isPresent()).isFalse();
    assertThat(returnType.isVoid()).isFalse();
  }

  @Test
  void testFromNotDefined() {
    SymbolReturnType returnType = SymbolReturnType.from(parseReturnTypeClause("function a(){}"));
    assertThat(returnType.isPresent()).isFalse();
    assertThat(returnType.isVoid()).isFalse();
  }

  @Test
  void testFromIntType() {
    SymbolReturnType returnType = SymbolReturnType.from(parseReturnTypeClause("function a():int{}"));
    assertThat(returnType.isPresent()).isTrue();
    assertThat(returnType.isVoid()).isFalse();
  }

  @Test
  void testFromVoidType() {
    SymbolReturnType returnType = SymbolReturnType.from(parseReturnTypeClause("function a():void{}"));
    assertThat(returnType.isPresent()).isTrue();
    assertThat(returnType.isVoid()).isTrue();
  }

  @Test
  void testFromComplexType() {
    SymbolReturnType returnType = SymbolReturnType.from(parseReturnTypeClause("function a():int|float{}"));
    assertThat(returnType.isPresent()).isTrue();
    assertThat(returnType.isVoid()).isFalse();
  }

  ReturnTypeClauseTree parseReturnTypeClause(String code) {
    CompilationUnitTree file = parseSource("<?php\n" + code);
    FunctionDeclarationTree func = TreeUtils.firstDescendant(file, FunctionDeclarationTree.class).get();
    return func.returnTypeClause();
  }
}
