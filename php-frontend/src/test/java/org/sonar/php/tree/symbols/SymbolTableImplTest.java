/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import com.google.common.collect.Iterables;
import java.util.List;
import org.assertj.core.api.ListAssert;
import org.junit.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.symbols.MemberSymbol;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.symbols.TypeSymbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class SymbolTableImplTest extends ParsingTestUtils {

  private CompilationUnitTree cut = parse("symbols/symbolTable.php");
  private SymbolTableImpl SYMBOL_MODEL = SymbolTableImpl.create(cut);

  @Test
  public void case_sensitivity() {
    CompilationUnitTree cut = parse("symbols/symbolCase.php");
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);

    Symbol myFunc = getUniqueSymbol("myFunc", symbolTable);
    assertThat(myFunc.kind()).isEqualTo(Kind.FUNCTION);
    assertThat(myFunc.usages()).extracting("value").containsExactlyInAnyOrder("MyFunc", "MYFUNC");

    Symbol constLow = getUniqueSymbol("myconst", symbolTable);
    Symbol constUp = getUniqueSymbol("MYCONST", symbolTable);

    assertThat(constLow).isNotEqualTo(constUp);
    assertThat(constUp.usages()).hasSize(0); // FIXME should be 1
    assertThat(constLow.usages()).hasSize(0);

    Symbol variableLow = getUniqueSymbol("$myvar", symbolTable);
    Symbol variableUp = getUniqueSymbol("$MYVAR", symbolTable);

    assertThat(variableLow).isNotEqualTo(variableUp);
    assertThat(variableLow.usages()).hasSize(1);
    assertThat(variableUp.usages()).hasSize(0);

    assertThat(symbolTable.getSymbols("$MyVar")).isEmpty();
  }

  @Test
  public void symbols_filtering() {
    assertThat(SYMBOL_MODEL.getSymbols()).hasSize(18);

    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.FUNCTION)).hasSize(2);
    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.CLASS)).hasSize(1);
    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.FIELD)).hasSize(3);
    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.PARAMETER)).hasSize(1);
    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.VARIABLE)).hasSize(11);

    assertThat(SYMBOL_MODEL.getSymbols("$a")).hasSize(2);
    // Case sensitive for variables
    assertThat(SYMBOL_MODEL.getSymbols("$A")).hasSize(0);

    assertThat(SYMBOL_MODEL.getSymbols("f")).hasSize(2);
    // Case in-sensitive for functions
    assertThat(SYMBOL_MODEL.getSymbols("F")).hasSize(2);
  }

  @Test
  public void test_class_fields() {
    Symbol field = SYMBOL_MODEL.getSymbols("$field1").get(0);
    Symbol constantField = SYMBOL_MODEL.getSymbols("CONSTANT_FIELD").get(0);

    assertThat(field.hasModifier("public")).isTrue();
    assertThat(field.is(Symbol.Kind.FIELD)).isTrue();

    assertThat(constantField.hasModifier("const")).isTrue();
    assertThat(constantField.is(Symbol.Kind.FIELD)).isTrue();
  }

  @Test
  public void test_global_constant() {
    Symbol constant = SYMBOL_MODEL.getSymbols("CONSTANT").get(0);

    assertThat(constant.hasModifier("const")).isTrue();
    assertThat(constant.is(Symbol.Kind.VARIABLE)).isTrue();
  }

  @Test
  public void list_variable() {
    assertThat(SYMBOL_MODEL.getSymbols("$l1")).hasSize(1);
    assertThat(SYMBOL_MODEL.getSymbols("$l2")).hasSize(1);
  }

  @Test
  public void foreach_variable() {
    assertThat(SYMBOL_MODEL.getSymbols("$key")).hasSize(1);
    assertThat(SYMBOL_MODEL.getSymbols("$val")).hasSize(1);
  }

  @Test
  public void static_variable() {
    List<Symbol> symbols = SYMBOL_MODEL.getSymbols("$static");
    assertThat(symbols).hasSize(1);
    Symbol symbol = symbols.get(0);
    assertThat(symbol.hasModifier("static")).isTrue();
  }

  @Test
  public void global_variable() {
    List<Symbol> symbols = SYMBOL_MODEL.getSymbols("$global");
    assertThat(symbols).hasSize(2);

    SymbolImpl globalGlobal = (SymbolImpl) symbols.get(0);
    assertThat(globalGlobal.scope().tree().is(Tree.Kind.COMPILATION_UNIT)).isTrue();
    assertThat(((PHPTree) globalGlobal.declaration()).getLine()).isEqualTo(4);
    assertThat(globalGlobal.usages().stream().map(st -> st.line())).containsExactly(15);
    assertThat(globalGlobal.hasModifier("global")).isTrue();

    SymbolImpl localGlobal = (SymbolImpl) symbols.get(1);
    assertThat(localGlobal.scope().tree().is(Tree.Kind.FUNCTION_DECLARATION)).isTrue();
    assertThat(localGlobal.modifiers()).isEmpty();
    assertThat(localGlobal.usages()).isEmpty();
    assertThat(((PHPTree) localGlobal.declaration()).getLine()).isEqualTo(13);
    // not able to retrieve the symbol '$global' from the scope itself, as it is ambiguous : there is the local and global one
    assertThat(localGlobal.scope().getSymbol("$global", Symbol.Kind.VARIABLE)).isNull();
  }

  @Test
  public void retrieve_symbol_by_tree() {
    ExpressionTree dollarAUsage = ((AssignmentExpressionTree) ((ExpressionStatementTree)
      ((FunctionDeclarationTree) cut.script().statements().get(5)).body().statements().get(3)).expression()).variable();
    Symbol symbol = SYMBOL_MODEL.getSymbol(dollarAUsage);
    assertThat(symbol).isNotNull();
    assertThat(symbol.name()).isEqualTo("$a");
  }

  @Test
  public void built_in_variables() {
    CompilationUnitTree cut = parse("symbols/symbolBuiltins.php");
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);

    assertThat(symbolTable.getSymbols("$myvar")).hasSize(3);
    assertThat(symbolTable.getSymbols("$GLOBALS")).isEmpty();
    assertThat(symbolTable.getSymbols("$_SERVER")).isEmpty();
    assertThat(symbolTable.getSymbols("$_GET")).isEmpty();
    assertThat(symbolTable.getSymbols("$_POST")).isEmpty();
    assertThat(symbolTable.getSymbols("$_FILES")).isEmpty();
    assertThat(symbolTable.getSymbols("$_SESSION")).isEmpty();
    assertThat(symbolTable.getSymbols("$_ENV")).isEmpty();
    assertThat(symbolTable.getSymbols("$PHP_ERRORMSG")).isEmpty();
    assertThat(symbolTable.getSymbols("$HTTP_RAW_POST_DATA")).isEmpty();
    assertThat(symbolTable.getSymbols("$HTTP_RESPONSE_HEADER")).isEmpty();
    assertThat(symbolTable.getSymbols("$ARGC")).isEmpty();
    assertThat(symbolTable.getSymbols("$argc")).hasSize(2);
    assertThat(symbolTable.getSymbols("$ARGV")).hasSize(1);
    assertThat(symbolTable.getSymbols("$argv")).hasSize(1);
    assertThat(symbolTable.getSymbols("$_COOKIE")).isEmpty();
    assertThat(symbolTable.getSymbols("$_REQUEST")).isEmpty();
    assertThat(symbolTable.getSymbols("$this")).isEmpty();

    Symbol paramArgvLowerCase = getUniqueSymbol("$argv", symbolTable);
    Symbol argvUpperCase = getUniqueSymbol("$ARGV", symbolTable);

    assertThat(paramArgvLowerCase).isNotEqualTo(argvUpperCase);
    assertThat(paramArgvLowerCase.usages()).hasSize(1);
    assertThat(argvUpperCase.usages()).hasSize(0);
    assertThat(paramArgvLowerCase.kind()).isEqualTo(Symbol.Kind.PARAMETER);
  }

  @Test
  public void qualified_name_for_classes() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php class A  {} namespace N1 { class A {} } ");
    assertClassSymbols(symbolTable, "a", "n1\\a");

    symbolTable = symbolTableFor("<?php namespace N1; class A  {} class B {} ");
    assertClassSymbols(symbolTable, "n1\\a", "n1\\b");

    symbolTable = symbolTableFor("<?php namespace N1; class A  {} class B {} namespace N2; class C {}");
    assertClassSymbols(symbolTable, "n1\\a", "n1\\b", "n2\\c");
  }

  @Test
  public void qn_class_symbol_usages() {
    CompilationUnitTree cut = parseSource("<?php namespace N1 {\n" +
      " class A {}\n" +
      " $a = new A();\n" +
      "}\n" +
      "$a = new \\N1\\A();");
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);
    assertClassSymbols(symbolTable, "n1\\a");
    assertSymbolUsages(symbolTable, "n1\\a", 3, 5);
  }


  @Test
  public void use_statements() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php \n" +
      "namespace N1 { class A {} }\n" +
      "use N1\\A as Alias;\n" +
      "$a = new Alias();");
    assertSymbolUsages(symbolTable, "n1\\a", 4);

    symbolTable = symbolTableFor("<?php \n" +
      "namespace N1 { class A {} }\n" +
      "use N1\\A;\n" +
      "$a = new A();");
    assertSymbolUsages(symbolTable, "n1\\a", 4);
  }

  @Test
  public void use_statements_aliased_name() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php \n" +
      "namespace N1\\N2 { class A {} }\n" +
      "use N1\\N2;\n" +
      "$a = new N2\\A();");
    assertSymbolUsages(symbolTable, "N1\\N2\\A", 4);
  }

  @Test
  public void global_and_alias_usage() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php \n" +
      "class A {} \n" +
      "namespace N { class A {} }\n" +
      "use N\\A;\n" +
      "$a = new A();\n" +
      "$a = new \\A();");
    assertClassSymbols(symbolTable, "a", "n\\a");
    assertSymbolUsages(symbolTable, "n\\a", 5);
    assertSymbolUsages(symbolTable, "a", 6);
  }

  @Test
  public void use_statements_group() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php namespace A\\B; class C {} class D {} use A\\B\\{C, D as E};\n" +
      "new C();\n" +
      "new D();\n" +
      "new E();");
    assertSymbolUsages(symbolTable, "A\\B\\C", 2);
    assertSymbolUsages(symbolTable, "A\\B\\D", 3, 4);
  }

  @Test
  public void usage_before_declaration() {
    CompilationUnitTree cut = parseSource("<?php namespace N {\n" +
      "$a = new N1\\A();\n" +
      "}\n" +
      "\n" +
      "namespace N\\N1 {\n" +
      "class A {}\n" +
      "}");
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);
    assertClassSymbols(symbolTable, "n\\n1\\a");
    assertSymbolUsages(symbolTable, "n\\n1\\a", 2);
  }

  @Test
  public void function_usage_before_declaration() {
    CompilationUnitTree cut = parseSource("<?php namespace N {\n" +
      "f();\n" +
      "function f() {}" +
      "}");
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);
    assertFunctionSymbols(symbolTable, "n\\f");
    assertSymbolUsages(symbolTable, "n\\f", 2);
  }

  @Test
  public void nested_function() {
    // Note that actual runtime behavior is that function g doesn't exist until f() is invoked, so this particular example
    // will actually lead to function not defined error on line 2 at runtime, however this is good enough for purpose of analysis
    CompilationUnitTree cut = parseSource("<?php namespace N {\n" +
      "g();\n" +
      "f();\n" +
      "function f() {" +
      "  function g() {}" +
      "}" +
      "}");
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);
    assertFunctionSymbols(symbolTable, "n\\f", "n\\g");
    assertSymbolUsages(symbolTable, "n\\f", 3);
    assertSymbolUsages(symbolTable, "n\\g", 2);
  }

  @Test
  public void nested_function_inside_method() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php class A { private function nesting() { function nested() {} } }");
    Symbol symbol = symbolTable.getSymbol("nested");
    assertThat(symbol).isNotNull();
  }

  @Test
  public void undeclared_class_usage() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php $dbh = new PDO('odbc:sample', 'db2inst1', 'ibmdb2');");
    Symbol symbol = symbolTable.getSymbol("pdo");
    assertThat(symbol).isInstanceOf(UndeclaredSymbol.class);
    SyntaxToken usage = Iterables.getOnlyElement(symbol.usages());
    assertThat(usage.line()).isEqualTo(1);
    assertThat(usage.column()).isEqualTo(17);
  }

  @Test
  public void undeclared_class_usage_with_fully_qualified_name() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php $dbh = new \\PDO('odbc:sample', 'db2inst1', 'ibmdb2');");
    Symbol symbol = symbolTable.getSymbol("pdo");
    assertThat(symbol).isInstanceOf(UndeclaredSymbol.class);
    SyntaxToken usage = Iterables.getOnlyElement(symbol.usages());
    assertThat(usage.line()).isEqualTo(1);
    assertThat(usage.column()).isEqualTo(18);
  }

  @Test
  public void undeclared_class_usage_in_namespace() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace A { $a = new A('odbc:sample', 'db2inst1', 'ibmdb2'); }");
    Symbol symbol = symbolTable.getSymbol("A\\A");
    assertThat(symbol).isInstanceOf(UndeclaredSymbol.class);
    SyntaxToken usage = Iterables.getOnlyElement(symbol.usages());
    assertThat(usage.line()).isEqualTo(1);
    assertThat(usage.column()).isEqualTo(30);
  }

  @Test
  public void undeclared_function_usage() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace A { $a = f(); $b = f(); } f();");
    Symbol symbol = symbolTable.getSymbol("f");
    assertThat(symbol).isNotNull();
    assertThat(symbol).isInstanceOf(UndeclaredSymbol.class);
    assertSymbolUsages(symbolTable, "f", 1, 1, 1);
  }

  @Test
  public void test_type_symbol() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace N { class A {} class B extends A {} } ");
    Symbol classA = symbolTable.getSymbol("n\\a");
    Symbol classB = symbolTable.getSymbol("n\\b");
    assertThat(classA.name()).isEqualTo("a");
    assertThat(classA).isInstanceOf(TypeSymbol.class);
    assertThat(classB).isInstanceOf(TypeSymbol.class);
    assertThat(((TypeSymbol) classB).superClass()).isEqualTo(classA);
    assertThat(((TypeSymbol) classA).superClass()).isNull();
  }

  @Test
  public void test_undeclared_superclass() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace N { class B extends A {} } ");
    Symbol classA = symbolTable.getSymbol("n\\a");
    Symbol classB = symbolTable.getSymbol("n\\b");
    assertThat(classA).isInstanceOf(UndeclaredSymbol.class);
    assertThat(classB).isInstanceOf(TypeSymbol.class);
    assertThat(((TypeSymbol) classB).superClass()).isEqualTo(classA);
  }

  @Test
  public void test_superclass_with_qualified_name() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace N { use M; class B extends M\\A implements \\M\\C {} } namespace M { class A {} interface C {} }");
    TypeSymbol classA = (TypeSymbol) symbolTable.getSymbol("m\\a");
    TypeSymbol classB = (TypeSymbol) symbolTable.getSymbol("n\\b");
    assertThat(classB.superClass()).isEqualTo(classA);
    assertThat(classB.interfaces()).extracting(i -> i.qualifiedName().toString()).containsExactly("m\\c");
  }

  @Test
  public void test_class_symbol_with_interfaces() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace N { class B implements I1, I2 {} interface I1 {} } ");
    TypeSymbol classB = (TypeSymbol) symbolTable.getSymbol("n\\b");
    Symbol iface1 = symbolTable.getSymbol("n\\i1");
    Symbol iface2 = symbolTable.getSymbol("n\\i2");
    assertThat(classB.interfaces()).containsExactly(iface1, iface2);
    assertThat(iface1).isInstanceOf(TypeSymbol.class);
    assertThat(iface2).isInstanceOf(UndeclaredSymbol.class);
  }


  @Test
  public void test_anonymous_class() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace N { $x = new class { function foo() {} }; } ");
    assertThat(symbolTable.getSymbols()).hasSize(2);
    Symbol fooSymbol = Iterables.getOnlyElement(symbolTable.getSymbols("foo"));
    // TODO qualified name for methods of anonymous class are wrong, because we don't create correct symbols
    assertThat(fooSymbol.qualifiedName().toString()).isEqualTo("n\\foo");
  }

  @Test
  public void test_class_symbol_members() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace N { class A { const A; function foo() {} }  } class B { function bar() {} }");
    TypeSymbol classA = (TypeSymbol) symbolTable.getSymbol("n\\a");
    assertThat(classA.kind()).isEqualTo(Kind.CLASS);
    assertThat(classA.members()).extracting(m -> m.qualifiedName().toString())
      .containsExactly("n\\a::a", "n\\a::foo");
    assertThat(classA.members()).extracting(MemberSymbol::owner).allMatch(classA::equals);

    TypeSymbol classB = (TypeSymbol) symbolTable.getSymbol("b");
    assertThat(classB.members()).extracting(m -> m.qualifiedName().toString())
      .containsExactly("b::bar");
    assertThat(classB.members()).extracting(MemberSymbol::owner).allMatch(classB::equals);
  }

  @Test
  public void test_class_symbol_members_case_insensitive() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php  namespace N { class A { const A; function Foo() {} }  } ");
    TypeSymbol classA = (TypeSymbol) symbolTable.getSymbol("n\\a");
    assertThat(classA.members()).extracting(m -> m.qualifiedName().simpleName()).containsExactly("a", "foo");
  }

  @Test
  public void static_invocation() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php use Cake\\Utility\\Security as CakeSecurity; CakeSecurity::encrypt($data, $key); ");
    Symbol symbol = symbolTable.getSymbol("cake\\utility\\security");
    assertThat(symbol).isNotNull();
    assertThat(symbol.kind()).isEqualTo(Kind.CLASS);
    assertThat(symbol).isExactlyInstanceOf(UndeclaredSymbol.class);
  }

  @Test
  public void new_expression_wo_brackets() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php new A; ");
    Symbol a = symbolTable.getSymbol("a");
    assertThat(a).isNotNull();
    assertThat(a.usages()).hasSize(1);
  }

  @Test
  public void anonymous_class() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php new class() extends A implements I1, I2 {};");
    Symbol symbol = symbolTable.getSymbol("a");
    assertThat(symbol).isNotNull();
    assertThat(symbol.kind()).isEqualTo(Kind.CLASS);
    assertThat(symbol).isExactlyInstanceOf(UndeclaredSymbol.class);
    assertThat(symbolTable.getSymbol("i1")).isNotNull();
    assertThat(symbolTable.getSymbol("i2")).isNotNull();
  }

  @Test
  public void traits() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php namespace N { class A { use trait1, trait2; } }");
    assertThat(symbolTable.getSymbol("n\\trait1")).isNotNull();
    assertThat(symbolTable.getSymbol("n\\trait2")).isNotNull();
  }

  @Test
  public void use_in_trait() {
    SymbolTableImpl symbolTable = symbolTableFor("<?php namespace N { trait A { use trait1, trait2; } }");
    assertThat(symbolTable.getSymbol("n\\a")).isNotNull();
    assertThat(symbolTable.getSymbol("n\\trait1")).isNotNull();
    assertThat(symbolTable.getSymbol("n\\trait2")).isNotNull();
  }

  private static ListAssert<String> assertClassSymbols(SymbolTableImpl symbolTable, String... fullyQualifiedNames) {
    return assertThat(symbolTable.getSymbols(Kind.CLASS)).extracting(s -> s.qualifiedName().toString())
      .containsExactly(fullyQualifiedNames);
  }

  private static ListAssert<String> assertFunctionSymbols(SymbolTableImpl symbolTable, String... fullyQualifiedNames) {
    return assertThat(symbolTable.getSymbols(Kind.FUNCTION)).extracting(s -> s.qualifiedName().toString())
      .containsExactly(fullyQualifiedNames);
  }

  private void assertSymbolUsages(SymbolTableImpl symbolTable, String qualifiedName, Integer... lines) {
    Symbol symbol = symbolTable.getSymbol(qualifiedName);
    assertThat(symbol.usages()).hasSize(lines.length);
    assertThat(symbol.usages()).extracting(SyntaxToken::line).containsExactly(lines);
  }


  private SymbolTableImpl symbolTableFor(String source) {
    CompilationUnitTree cut = parseSource(source);
    return SymbolTableImpl.create(cut);
  }


  private static Symbol getUniqueSymbol(String name, SymbolTableImpl table) {
    List<Symbol> symbols = table.getSymbols(name);
    assertThat(symbols).hasSize(1);

    return symbols.get(0);
  }
}
