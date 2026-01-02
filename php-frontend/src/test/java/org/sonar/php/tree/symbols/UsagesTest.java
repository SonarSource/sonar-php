/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;

import static org.assertj.core.api.Assertions.assertThat;

class UsagesTest extends ParsingTestUtils {

  private final SymbolTableImpl SYMBOL_TABLE = SymbolTableImpl.create(parse("symbols/usages.php"));

  private final Symbol globalSymbolA = getGlobalScopeSymbol("$a");
  private final Symbol globalSymbolB = getGlobalScopeSymbol("$b");
  private final Symbol globalSymbolFooBar = getGlobalScopeSymbol("$fooBar");
  private final Symbol globalSymbolSomeTrait = getGlobalScopeSymbol("SomeTrait");
  private final Symbol globalSymbolSomeClass = getGlobalScopeSymbol("SomeClass");
  private final Symbol globalSymbolSomeInterface = getGlobalScopeSymbol("SomeInterface");

  @Test
  void test() {
    for (Scope scope : SYMBOL_TABLE.getScopes()) {

      Tree tree = scope.tree();
      if (tree.is(Tree.Kind.FUNCTION_EXPRESSION)) {
        testUseClause(scope);
      } else if (tree.is(Tree.Kind.ARROW_FUNCTION_EXPRESSION)) {
        testArrowFunction(scope);
      } else if (scope.isGlobal()) {
        testGlobalScope(scope);

      } else if (tree.is(Tree.Kind.FUNCTION_DECLARATION)) {
        String functionName = ((FunctionDeclarationTree) tree).name().text();
        if ("g".equals(functionName)) {
          testGlobalStatement(scope);

        } else if ("h".equals(functionName)) {
          testParameter(scope);

        } else if ("j".equals(functionName)) {
          testLocalVariable(scope);
        }

      } else if (tree.is(Tree.Kind.CLASS_DECLARATION)) {
        String className = ((ClassDeclarationTree) tree).name().text();
        if ("A".equals(className)) {
          testClass(scope);
        }
      } else if (tree.is(Tree.Kind.METHOD_DECLARATION)) {
        String methodName = ((MethodDeclarationTree) tree).name().text();
        if ("method".equals(methodName)) {
          testMethod(scope);
        } else if ("__construct".equals(methodName)) {
          testAnonymousClassMethod(scope);
        }
      } else if (tree.is(Tree.Kind.ANONYMOUS_CLASS)) {
        if (((AnonymousClassTree) tree).arguments().size() == 2) {
          testAnonymousClass(scope);
        }
      }
    }

  }

  private void testAnonymousClass(Scope scope) {
    assertThat(globalSymbolFooBar.usages()).hasSize(1);
    assertThat(globalSymbolSomeTrait.usages()).hasSize(1);
    assertThat(globalSymbolSomeClass.usages()).hasSize(1);
    assertThat(globalSymbolSomeInterface.usages()).hasSize(1);
    assertThat(scope.getSymbol("$qux").usages()).hasSize(1);
    assertThat(scope.getSymbol("$num").usages()).hasSize(2);
  }

  private void testAnonymousClassMethod(Scope scope) {
    assertThat(scope.getSymbol("$string").usages()).hasSize(1);
    assertThat(scope.getSymbol("$num").usages()).hasSize(1);
  }

  private void testClass(Scope scope) {
    assertThat(scope.getSymbol("$field").usages()).hasSize(1);
    assertThat(scope.getSymbol("method").usages()).hasSize(1);
  }

  private void testMethod(Scope scope) {
    assertThat(scope.getSymbol("$A").usages()).hasSize(1);
  }

  private void testLocalVariable(Scope scope) {
    Symbol localSymbol = scope.getSymbol("$a");
    assertThat(localSymbol).isNotEqualTo(globalSymbolA);
    assertThat(localSymbol.usages()).hasSize(1);
  }

  private void testParameter(Scope scope) {
    Symbol parameterSymbol = scope.getSymbol("$a");
    assertThat(parameterSymbol).isNotEqualTo(globalSymbolA);
    assertThat(parameterSymbol.usages()).hasSize(1);
  }

  private void testGlobalScope(Scope scope) {
    assertThat(globalSymbolA.usages()).hasSize(5);
    assertThat(globalSymbolB.usages()).hasSize(1);

    Symbol arraySymbol = scope.getSymbol("$array");
    assertThat(arraySymbol.usages()).hasSize(1);

    assertThat(scope.getSymbol("$f").usages()).hasSize(2);
    assertThat(scope.getSymbol("h").usages()).hasSize(1);
    assertThat(scope.getSymbol("j").usages()).hasSize(1);

    assertThat(scope.getSymbol("$compoundVar").usages()).hasSize(2);
    assertThat(scope.getSymbol("$heredocVar").usages()).hasSize(2);
    assertThat(scope.getSymbol("$var").usages()).hasSize(3);

    Symbol classSymbol = scope.getSymbol("A");
    assertThat(classSymbol).isNotNull();
    assertThat(classSymbol.is(Kind.CLASS)).isTrue();
    assertThat(classSymbol.usages()).hasSize(3);

  }

  private void testGlobalStatement(Scope scope) {
    assertThat(scope.getSymbol("$a")).isEqualTo(globalSymbolA);
  }

  private void testUseClause(Scope scope) {
    Symbol useClauseSymbolA = scope.getSymbol("$a");
    Symbol useClauseSymbolB = scope.getSymbol("$b");
    assertThat(useClauseSymbolA).isNotEqualTo(globalSymbolA);
    assertThat(useClauseSymbolB).isNotEqualTo(globalSymbolB);
    assertThat(useClauseSymbolA.usages()).hasSize(1);
  }

  private void testArrowFunction(Scope scope) {
    Symbol symbolA = scope.getSymbol("$a");
    Symbol symbolB = scope.getSymbol("$b");
    assertThat(symbolA).isEqualTo(globalSymbolA);
    assertThat(symbolB).isNotEqualTo(globalSymbolB);
    assertThat(symbolB.usages()).hasSize(1);
  }

  Symbol getGlobalScopeSymbol(String name) {
    for (Scope scope : SYMBOL_TABLE.getScopes()) {
      if (scope.isGlobal()) {
        return scope.getSymbol(name);
      }
    }
    throw new IllegalStateException();
  }

}
