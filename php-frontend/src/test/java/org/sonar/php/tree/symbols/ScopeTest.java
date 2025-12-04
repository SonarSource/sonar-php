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
package org.sonar.php.tree.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeTest extends ParsingTestUtils {

  private final SymbolTableImpl SYMBOL_TABLE = SymbolTableImpl.create(parse("symbols/scopes.php"));

  @Test
  void globalScope() {
    Scope globalScope = getScopeFor(Kind.COMPILATION_UNIT);

    assertThat(globalScope.isGlobal()).isTrue();
    assertThat(globalScope.getSymbol("$a")).isNotNull();
    assertThat(globalScope.getSymbol("$b")).isNotNull();
    assertThat(globalScope.getSymbol("f")).isNotNull();

    assertThat(globalScope.getSymbol("$c")).isNull();
  }

  @Test
  void functionScope() {
    Scope functionScope = getScopeFor(Kind.FUNCTION_DECLARATION);

    assertThat(functionScope.isGlobal()).isFalse();
    assertThat(functionScope.getSymbols(Symbol.Kind.PARAMETER)).hasSize(2);
    assertThat(functionScope.getSymbol("$p1")).isNotNull();
    assertThat(functionScope.getSymbol("$p2")).isNotNull();
    assertThat(functionScope.getSymbol("$c")).isNotNull();
    assertThat(functionScope.getSymbol("$d")).isNotNull();

    // other scopes not accessible
    assertThat(functionScope.getSymbol("$a")).isNull();
    assertThat(functionScope.getSymbol("$e")).isNull();
    assertThat(functionScope.getSymbol("$varVar")).isNull();
  }

  @Test
  void functionExpressionScope() {
    Scope functionExprScope = getScopeFor(Kind.FUNCTION_EXPRESSION);

    assertThat(functionExprScope.getSymbol("$e")).isNotNull();
  }

  @Test
  void arrowFunctionExpressionScope() {
    Scope scope = getScopeFor(Kind.ARROW_FUNCTION_EXPRESSION);

    assertThat(scope.getSymbols(Symbol.Kind.PARAMETER)).extracting(Symbol::name).containsExactlyInAnyOrder("$a", "$d");
    assertThat(scope.getSymbols(Symbol.Kind.VARIABLE)).isEmpty();
    Symbol symbolC = scope.getSymbol("$c");
    Symbol symbolD = scope.getSymbol("$d");
    assertThat(symbolC).isNotNull();
    assertThat(symbolC.kind()).isEqualTo(Symbol.Kind.VARIABLE);
    assertThat(symbolD).isNotNull();
    assertThat(scope.getSymbol("$d", Symbol.Kind.PARAMETER)).isEqualTo(symbolD);
    assertThat(scope.getSymbol("$d", Symbol.Kind.VARIABLE)).isNull();

    Scope functionScope = getScopeFor(Kind.FUNCTION_DECLARATION);
    assertThat(symbolC).isEqualTo(functionScope.getSymbol("$c"));
    assertThat(symbolD).isNotEqualTo(functionScope.getSymbol("$d"));
  }

  @Test
  void globalStatement() {
    Scope functionScope = getScopeFor(Kind.FUNCTION_DECLARATION);

    assertThat(functionScope.getSymbol("$b")).isNotNull();
    assertThat(SYMBOL_TABLE.getSymbols("$b")).hasSize(2);
    assertThat(functionScope.getSymbol("$externalVariable")).isNotNull();
    assertThat(SYMBOL_TABLE.getSymbols("$externalVariable")).hasSize(1);

    Scope globalScope = getScopeFor(Kind.COMPILATION_UNIT);
    assertThat(globalScope.getSymbol("$b")).isEqualTo(functionScope.getSymbol("$b"));
    assertThat(globalScope.getSymbol("$externalVariable")).isNull();
  }

  @Test
  void lexicalVariable() {
    Scope functionScope = getScopeFor(Kind.FUNCTION_EXPRESSION);

    assertThat(functionScope.getSymbol("$c")).isNotNull();
    assertThat(SYMBOL_TABLE.getSymbols("$c")).hasSize(2);
    assertThat(functionScope.getSymbol("$b")).isNotNull();
    assertThat(SYMBOL_TABLE.getSymbols("$b")).hasSize(2);

    Scope parentScope = getScopeFor(Kind.FUNCTION_DECLARATION);
    assertThat(parentScope.getSymbol("$c")).isNotEqualTo(functionScope.getSymbol("$c"));
    assertThat(parentScope.getSymbol("$b")).isNotEqualTo(functionScope.getSymbol("$b"));
  }

  @Test
  void classScope() {
    Scope classScope = getScopeFor(Kind.CLASS_DECLARATION);

    assertThat(classScope.getSymbol("$field1")).isNotNull();
    assertThat(classScope.getSymbol("method")).isNotNull();
  }

  @Test
  void anonymousClassScope() {
    Scope classScope = getScopeFor(Kind.ANONYMOUS_CLASS);

    assertThat(classScope.getSymbol("$field1")).isNotNull();
    assertThat(classScope.getSymbol("method")).isNotNull();
  }

  @Test
  void methodScope() {
    Scope methodScope = getScopeFor(Kind.METHOD_DECLARATION);

    assertThat(methodScope.getSymbol("$g")).isNotNull();
  }

  @Test
  void sameNameInSameScope() {
    SymbolTableImpl symbolTable = SymbolTableImpl.create(parse("symbols/scope_same_name.php"));
    Scope scope = getScopeFor(Kind.COMPILATION_UNIT, symbolTable);

    assertThat(scope.getSymbol("FOO", Symbol.Kind.FUNCTION)).isNotNull();
    assertThat(scope.getSymbol("FOO", Symbol.Kind.CLASS)).isNotNull();
  }

  private Scope getScopeFor(Tree.Kind kind, SymbolTable symbolTable) {
    for (Scope scope : symbolTable.getScopes()) {
      if (scope.tree().is(kind)) {
        return scope;
      }
    }
    throw new IllegalStateException();
  }

  private Scope getScopeFor(Tree.Kind kind) {
    return getScopeFor(kind, SYMBOL_TABLE);
  }

}
