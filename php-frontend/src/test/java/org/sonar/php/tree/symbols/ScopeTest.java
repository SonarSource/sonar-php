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
package org.sonar.php.tree.symbols;

import org.junit.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ScopeTest extends ParsingTestUtils {

  private final SymbolTableImpl SYMBOL_TABLE = SymbolTableImpl.create(parse("symbols/scopes.php"));

  @Test
  public void global_scope() throws Exception {
    Scope globalScope = getScopeFor(Kind.COMPILATION_UNIT);

    assertThat(globalScope.isGlobal()).isTrue();
    assertNotNull(globalScope.getSymbol("$a"));
    assertNotNull(globalScope.getSymbol("$b"));
    assertNotNull(globalScope.getSymbol("f"));
    assertNotNull(globalScope.getSymbol("A"));

    assertNull(globalScope.getSymbol("$c"));
  }

  @Test
  public void function_scope() throws Exception {
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
  public void function_expression_scope() throws Exception {
    Scope functionExprScope = getScopeFor(Kind.FUNCTION_EXPRESSION);

    assertNotNull(functionExprScope.getSymbol("$e"));
  }

  @Test
  public void global_statement() throws Exception {
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
  public void lexical_variable() throws Exception {
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
  public void class_scope() throws Exception {
    Scope classScope = getScopeFor(Kind.CLASS_DECLARATION);

    assertThat(classScope.getSymbol("$field1")).isNotNull();
    assertThat(classScope.getSymbol("method")).isNotNull();
  }

  @Test
  public void anonymous_class_scope() throws Exception {
    Scope classScope = getScopeFor(Kind.ANONYMOUS_CLASS);

    assertThat(classScope.getSymbol("$field1")).isNotNull();
    assertThat(classScope.getSymbol("method")).isNotNull();
  }

  @Test
  public void method_scope() throws Exception {
    Scope methodScope = getScopeFor(Kind.METHOD_DECLARATION);

    assertThat(methodScope.getSymbol("$g")).isNotNull();
  }

  @Test
  public void same_name_in_same_scope() throws Exception {
    SymbolTableImpl symbolTable = SymbolTableImpl.create(parse("symbols/scope_same_name.php"));
    Scope scope = getScopeFor(Kind.COMPILATION_UNIT, symbolTable);

    assertThat(scope.getSymbol("FOO", Symbol.Kind.FUNCTION)).isNotNull();
    assertThat(scope.getSymbol("FOO", Symbol.Kind.CLASS)).isNotNull();
  }

  private Scope getScopeFor(Tree.Kind kind, SymbolTable symbolTable){
    for (Scope scope: symbolTable.getScopes()){
      if (scope.tree().is(kind)){
        return scope;
      }
    }
    throw new IllegalStateException();
  }

  private Scope getScopeFor(Tree.Kind kind){
    return getScopeFor(kind, SYMBOL_TABLE);
  }

}
