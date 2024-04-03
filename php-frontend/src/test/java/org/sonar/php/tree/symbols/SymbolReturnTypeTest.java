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
    assertThat(returnType.isDefined()).isFalse();
    assertThat(returnType.isVoid()).isFalse();
  }

  @Test
  void testFromNotDefined() {
    SymbolReturnType returnType = SymbolReturnType.from(parseReturnTypeClause("function a(){}"));
    assertThat(returnType.isDefined()).isFalse();
    assertThat(returnType.isVoid()).isFalse();
  }

  @Test
  void testFromIntType() {
    SymbolReturnType returnType = SymbolReturnType.from(parseReturnTypeClause("function a():int{}"));
    assertThat(returnType.isDefined()).isTrue();
    assertThat(returnType.isVoid()).isFalse();
  }

  @Test
  void testFromVoidType() {
    SymbolReturnType returnType = SymbolReturnType.from(parseReturnTypeClause("function a():void{}"));
    assertThat(returnType.isDefined()).isTrue();
    assertThat(returnType.isVoid()).isTrue();
  }

  @Test
  void testFromComplexType() {
    SymbolReturnType returnType = SymbolReturnType.from(parseReturnTypeClause("function a():int|float{}"));
    assertThat(returnType.isDefined()).isTrue();
    assertThat(returnType.isVoid()).isFalse();
  }

  ReturnTypeClauseTree parseReturnTypeClause(String code) {
    CompilationUnitTree file = parseSource("<?php\n" + code);
    FunctionDeclarationTree func = TreeUtils.firstDescendant(file, FunctionDeclarationTree.class).get();
    return func.returnTypeClause();
  }
}
