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
/*
 * Sonarfy it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 Qube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modi* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.symbols;

import org.junit.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;

import static org.assertj.core.api.Assertions.assertThat;

public class UsagesTest extends ParsingTestUtils {

  private final SymbolTableImpl SYMBOL_TABLE = SymbolTableImpl.create(parse("symbols/usages.php"));

  private final Symbol globalSymbolA = getGlobalScopeSymbol("$a");
  private final Symbol globalSymbolB = getGlobalScopeSymbol("$b");
  private final Symbol globalSymbolFooBar = getGlobalScopeSymbol("$fooBar");
  private final Symbol globalSymbolSomeTrait = getGlobalScopeSymbol("SomeTrait");
  private final Symbol globalSymbolSomeClass = getGlobalScopeSymbol("SomeClass");
  private final Symbol globalSymbolSomeInterface = getGlobalScopeSymbol("SomeInterface");

  @Test
  public void test() throws Exception {
    for (Scope scope : SYMBOL_TABLE.getScopes()) {

      Tree tree = scope.tree();
      if (tree.is(Tree.Kind.FUNCTION_EXPRESSION)) {
        test_use_clause(scope);

      } else if (scope.isGlobal()) {
        test_global_scope(scope);

      } else if (tree.is(Tree.Kind.FUNCTION_DECLARATION)) {
        String functionName = ((FunctionDeclarationTree) tree).name().text();
        if ("g".equals(functionName)) {
          test_global_statement(scope);

        } else if ("h".equals(functionName)) {
          test_parameter(scope);

        } else if ("j".equals(functionName)) {
          test_local_variable(scope);
        }

      } else if (tree.is(Tree.Kind.CLASS_DECLARATION)) {
        String className = ((ClassDeclarationTree)tree).name().text();
        if ("A".equals(className)) {
          test_class(scope);
        }
      } else if (tree.is(Tree.Kind.METHOD_DECLARATION)) {
        String methodName = ((MethodDeclarationTree)tree).name().text();
        if ("method".equals(methodName)) {
          test_method(scope);
        } else if ("__construct".equals(methodName)) {
          test_anonymous_class_method(scope);
        }
      } else if (tree.is(Tree.Kind.ANONYMOUS_CLASS)) {
        if (((AnonymousClassTree)tree).arguments().size() == 2) {
          test_anonymous_class(scope);
        }
      }
    }

  }

  private void test_anonymous_class(Scope scope) {
    assertThat(globalSymbolFooBar.usages()).hasSize(1);
    assertThat(globalSymbolSomeTrait.usages()).hasSize(1);
    assertThat(globalSymbolSomeClass.usages()).hasSize(1);
    assertThat(globalSymbolSomeInterface.usages()).hasSize(1);
    assertThat(scope.getSymbol("$qux").usages()).hasSize(1);
    assertThat(scope.getSymbol("$num").usages()).hasSize(2);
  }

  private void test_anonymous_class_method(Scope scope) {
    assertThat(scope.getSymbol("$string").usages()).hasSize(1);
    assertThat(scope.getSymbol("$num").usages()).hasSize(1);
  }

  private void test_class(Scope scope) {
    assertThat(scope.getSymbol("$field").usages()).hasSize(1);
    assertThat(scope.getSymbol("method").usages()).hasSize(1);
  }

  private void test_method(Scope scope) {
    assertThat(scope.getSymbol("$A").usages()).hasSize(1);
  }

  private void test_local_variable(Scope scope) {
    Symbol localSymbol = scope.getSymbol("$a");
    assertThat(localSymbol).isNotEqualTo(globalSymbolA);
    assertThat(localSymbol.usages()).hasSize(1);
  }

  private void test_parameter(Scope scope) {
    Symbol parameterSymbol = scope.getSymbol("$a");
    assertThat(parameterSymbol).isNotEqualTo(globalSymbolA);
    assertThat(parameterSymbol.usages()).hasSize(1);
  }

  private void test_global_scope(Scope scope) {
    assertThat(globalSymbolA.usages()).hasSize(4);
    assertThat(globalSymbolB.usages()).hasSize(1);

    Symbol arraySymbol = scope.getSymbol("$array");
    assertThat(arraySymbol.usages()).hasSize(1);

    assertThat(scope.getSymbol("$f").usages()).hasSize(1);
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

  private void test_global_statement(Scope scope) {
    assertThat(scope.getSymbol("$a")).isEqualTo(globalSymbolA);
  }

  private void test_use_clause(Scope scope) {
    Symbol useClauseSymbolA = scope.getSymbol("$a");
    Symbol useClauseSymbolB = scope.getSymbol("$b");
    assertThat(useClauseSymbolA).isNotEqualTo(globalSymbolA);
    assertThat(useClauseSymbolB).isNotEqualTo(globalSymbolB);
    assertThat(useClauseSymbolA.usages()).hasSize(1);
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
