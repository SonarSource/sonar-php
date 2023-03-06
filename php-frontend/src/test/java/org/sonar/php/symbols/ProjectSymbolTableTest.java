/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.symbols;

import com.sonar.sslr.api.typed.ActionParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.expression.AnonymousClassTreeImpl;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.tree.TreeUtils.firstDescendant;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public class ProjectSymbolTableTest {

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  @Test
  public void superclass_in_different_file() {
    PhpFile file1 = file("file1.php", "<?php namespace ns1; class A {}");
    PhpFile file2 = file("file2.php", "<?php namespace ns1; class C extends B {}");
    PhpFile file3 = file("file3.php", "<?php namespace ns1; class B extends A {}");
    Tree ast = getAst(file2, buildProjectSymbolData(file1, file2, file3));
    Optional<ClassDeclarationTree> classDeclarationTree = firstDescendant(ast, ClassDeclarationTree.class);
    ClassSymbol c = Symbols.get(classDeclarationTree.get());
    assertThat(c.qualifiedName()).hasToString("ns1\\c");
    assertThat(c.location()).isEqualTo(new LocationInFileImpl(filePath("file2.php"), 1, 27, 1, 28));
    ClassSymbol b = c.superClass().get();
    assertThat(b.qualifiedName()).hasToString("ns1\\b");
    assertThat(b.location()).isEqualTo(new LocationInFileImpl(filePath("file3.php"), 1, 27, 1, 28));
    ClassSymbol a = b.superClass().get();
    assertThat(a.qualifiedName()).hasToString("ns1\\a");
    assertThat(a.superClass()).isEmpty();
  }

  @Test
  public void implemented_interfaces() {
    PhpFile file2 = file("file2.php", "<?php namespace ns1; class B extends A implements C {}");
    PhpFile file3 = file("file3.php", "<?php namespace ns1; interface C extends D {}");
    Tree ast = getAst(file2, buildProjectSymbolData(file2, file3));

    ClassDeclarationTree bDeclaration = firstDescendant(ast, ClassDeclarationTree.class).get();

    ClassSymbol b = Symbols.get(bDeclaration);
    assertThat(b.qualifiedName()).hasToString("ns1\\b");

    ClassSymbol a = b.superClass().get();
    assertThat(a.qualifiedName()).hasToString("ns1\\a");
    assertThat(a.isUnknownSymbol()).isTrue();
    assertThat(a.implementedInterfaces()).isEmpty();

    List<ClassSymbol> bInterfaces = b.implementedInterfaces();
    assertThat(bInterfaces).hasSize(1);
    ClassSymbol c = bInterfaces.get(0);
    assertThat(c.implementedInterfaces()).extracting(ClassSymbol::qualifiedName).containsExactly(qualifiedName("ns1\\d"));
  }

  @Test
  public void catch_clause() {
    PhpFile file1 = file("file1.php", "<?php namespace ns1; class A {}");
    PhpFile file2 = file("file2.php", "<?php namespace ns1; try {} catch (A $a) {}");
    Tree ast = getAst(file2, buildProjectSymbolData(file1, file2));

    CatchBlockTree catchBlockTree = firstDescendant(ast, CatchBlockTree.class).get();
    ClassSymbol a = Symbols.getClass(catchBlockTree.exceptionTypes().get(0));
    assertThat(a.location()).isEqualTo(new LocationInFileImpl(filePath("file1.php"), 1, 27, 1, 28));
  }

  @Test
  public void new_expression() {
    PhpFile file1 = file("file1.php", "<?php namespace ns1; class A {}");
    PhpFile file2 = file("file2.php", "<?php namespace ns1;\n new A();\n new A;");
    Tree ast = getAst(file2, buildProjectSymbolData(file1, file2));

    List<NewExpressionTree> newExpressions = TreeUtils.descendants(ast)
      .filter(NewExpressionTree.class::isInstance)
      .map(NewExpressionTree.class::cast)
      .collect(Collectors.toList());
    assertThat(newExpressions).extracting(e -> ((PHPTree) e).getLine()).containsExactly(2, 3);
    assertThat(newExpressions).extracting(e -> e.expression().getKind()).containsExactly(Tree.Kind.FUNCTION_CALL, Tree.Kind.NAMESPACE_NAME);
    for (NewExpressionTree newExpression : newExpressions) {
      NamespaceNameTree namespaceName = firstDescendant(newExpression, NamespaceNameTree.class).get();
      assertThat(Symbols.getClass(namespaceName).location()).isEqualTo(new LocationInFileImpl(filePath("file1.php"), 1, 27, 1, 28));
    }
  }

  @Test
  public void non_class_namespace_name() {
    PhpFile file1 = file("file1.php", "<?php namespace ns1; class A {}");
    Tree ast = parser.parse(file1.contents());
    NamespaceNameTree namespaceNameTree = firstDescendant(ast, NamespaceNameTree.class).get();
    assertThat(Symbols.getClass(namespaceNameTree).isUnknownSymbol()).isTrue();
  }

  @Test
  public void function_symbol_added_to_call() {
    PhpFile file1 = file("file1.php", "<?php function a(int $x, string... $y, $z = 'default') {}");
    PhpFile file2 = file("file2.php", "<?php a();");
    Tree ast = getAst(file2, buildProjectSymbolData(file1, file2));

    Optional<FunctionCallTree> functionCall = firstDescendant(ast, FunctionCallTree.class);
    FunctionSymbol symbol = Symbols.get(functionCall.get());

    assertThat(symbol.location()).isEqualTo(new LocationInFileImpl(filePath("file1.php"), 1, 15, 1, 16));
    assertThat(symbol.parameters()).hasSize(3);
    assertThat(symbol.parameters().get(0).name()).isEqualTo("$x");
    assertThat(symbol.parameters().get(0).type()).isEqualTo("int");
    assertThat(symbol.parameters().get(0).hasEllipsisOperator()).isFalse();
    assertThat(symbol.parameters().get(1).hasDefault()).isFalse();
    assertThat(symbol.parameters().get(1).hasEllipsisOperator()).isTrue();
    assertThat(symbol.parameters().get(2).hasDefault()).isTrue();
  }

  @Test
  public void function_has_return() {
    PhpFile file1 = file("file1.php", "<?php function a() { return $y; }");
    PhpFile file2 = file("file2.php", "<?php a();");
    Tree ast = getAst(file2, buildProjectSymbolData(file1, file2));

    Optional<FunctionCallTree> functionCall = firstDescendant(ast, FunctionCallTree.class);
    FunctionSymbol symbol = Symbols.get(functionCall.get());

    assertThat(symbol.hasReturn()).isTrue();
  }

  @Test
  public void function_has_return_function_expression_and_inner() {
    PhpFile file1 = file("file1.php", "<?php function a() { function foo() {return $y;} $x = function() {return $y;}; }");
    PhpFile file2 = file("file2.php", "<?php a();");
    Tree ast = getAst(file2, buildProjectSymbolData(file1, file2));

    Optional<FunctionCallTree> functionCall = firstDescendant(ast, FunctionCallTree.class);
    FunctionSymbol symbol = Symbols.get(functionCall.get());

    assertThat(symbol.hasReturn()).isFalse();
  }

  @Test
  public void function_has_func_get_args() {
    PhpFile file1 = file("file1.php", "<?php function a() { $args = func_get_args(); }");
    PhpFile file2 = file("file2.php", "<?php a();");
    ProjectSymbolData projectSymbolData = buildProjectSymbolData(file1, file2);
    Tree ast = parser.parse(file2.contents());
    SymbolTableImpl.create((CompilationUnitTree) ast, projectSymbolData, file2);

    Optional<FunctionCallTree> functionCall = firstDescendant(ast, FunctionCallTree.class);
    FunctionSymbol symbol = Symbols.get(functionCall.get());

    assertThat(symbol.hasFuncGetArgs()).isTrue();
  }

  @Test
  public void function_has_func_get_args_function_expression_and_inner() {
    PhpFile file1 = file("file1.php", "<?php function a() { function foo() { $args = func_get_args();} $x = function() {$args = func_get_args();}; }");
    PhpFile file2 = file("file2.php", "<?php a();");
    ProjectSymbolData projectSymbolData = buildProjectSymbolData(file1, file2);
    Tree ast = parser.parse(file2.contents());
    SymbolTableImpl.create((CompilationUnitTree) ast, projectSymbolData, file2);

    Optional<FunctionCallTree> functionCall = firstDescendant(ast, FunctionCallTree.class);
    FunctionSymbol symbol = Symbols.get(functionCall.get());

    assertThat(symbol.hasFuncGetArgs()).isFalse();
  }

  @Test
  public void unknown_function_symbol() {
    PhpFile file1 = file("file1.php", "<?php a();");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));

    Optional<FunctionCallTree> functionCall = firstDescendant(ast, FunctionCallTree.class);
    FunctionSymbol symbol = Symbols.get(functionCall.get());

    assertThat(symbol.isUnknownSymbol()).isTrue();
    assertThat(symbol.location()).isInstanceOf(UnknownLocationInFile.class);
    assertThat(symbol.parameters()).isEmpty();
    assertThat(symbol.qualifiedName()).isEqualTo(qualifiedName("a"));
  }

  @Test
  public void duplicate_function_declaration() {
    PhpFile file1 = file("file1.php", "<?php f();");
    PhpFile file2 = file("file2.php", "<?php function f($p1) {}");
    PhpFile file3 = file("file3.php", "<?php function f($p2) {}");
    Tree ast = getAst(file1, buildProjectSymbolData(file1, file2, file3));
    Optional<FunctionCallTree> functionCall = firstDescendant(ast, FunctionCallTree.class);
    FunctionSymbol symbol = Symbols.get(functionCall.get());
    assertThat(symbol.isUnknownSymbol()).isTrue();
  }

  @Test
  public void get_class_methods() {
    PhpFile file1 = file("file1.php", "<?php namespace SomeNamespace; class A {public function foo(){}}");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));

    Optional<ClassDeclarationTree> classDeclaration = firstDescendant(ast, ClassDeclarationTree.class);
    ClassSymbol classSymbol = Symbols.get(classDeclaration.get());

    assertThat(classSymbol.declaredMethods()).hasSize(1);

    MethodSymbol methodSymbol = classSymbol.getDeclaredMethod("foo");
    assertThat(methodSymbol.isUnknownSymbol()).isFalse();
    assertThat(methodSymbol.parameters()).isEmpty();
    assertThat(methodSymbol.hasReturn()).isFalse();
    assertThat(methodSymbol.visibility()).isEqualTo(Visibility.PUBLIC);
    assertThat(methodSymbol.location()).isEqualTo(new LocationInFileImpl(filePath("file1.php"), 1, 56, 1, 59));
  }

  @Test
  public void get_class_methods_anonymous() {
    PhpFile file1 = file("file1.php", "<?php class A {public function x() {$o = new class() {public function anon() {}};}}");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));

    Optional<ClassDeclarationTree> classDeclaration = firstDescendant(ast, ClassDeclarationTree.class);
    ClassSymbol classSymbol = Symbols.get(classDeclaration.get());
    assertThat(classSymbol.declaredMethods()).hasSize(1);
    assertThat(classSymbol.getDeclaredMethod("anon").isUnknownSymbol()).isTrue();
  }

  @Test
  public void anonymous_class() {
    PhpFile file1 = file("file1.php", "<?php $x = new class extends A implements B { public function foo() {} };");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));
    ClassSymbol anonymous = Symbols.get(firstDescendant(ast, AnonymousClassTreeImpl.class).get());
    assertThat(anonymous.isUnknownSymbol()).isFalse();
    assertThat(anonymous.qualifiedName()).hasToString("<anonymous_class>");
    assertThat(anonymous.superClass().get().qualifiedName()).isEqualTo(qualifiedName("a"));
    assertThat(anonymous.implementedInterfaces()).extracting(ClassSymbol::qualifiedName).containsOnly(qualifiedName("b"));
    assertThat(anonymous.declaredMethods()).extracting(MethodSymbol::name).containsOnly("foo");
  }

  @Test
  public void get_function_symbol_from_call() {
    PhpFile file1 = file("file1.php", "<?php function foo() {} foo();");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));
    FunctionSymbol symbol = Symbols.get(firstDescendant(ast, FunctionCallTree.class).get());
    assertThat(symbol).isNotInstanceOf(MethodSymbol.class);
    assertThat(symbol.isUnknownSymbol()).isFalse();
  }

  @Test
  public void do_not_get_function_symbol_from_unresolvable_call() {
    PhpFile file1 = file("file1.php", "<?php function foo() {} $foo();");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));
    assertThat(Symbols.get(firstDescendant(ast, FunctionCallTree.class).get()).isUnknownSymbol()).isTrue();
  }

  @Test
  public void get_method_symbol_from_call() {
    PhpFile file1 = file("file1.php", "<?php class FOO{public static function foo() {}} FOO::foo();");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));
    FunctionSymbol symbol = Symbols.get(firstDescendant(ast, FunctionCallTree.class).get());
    assertThat(symbol).isInstanceOf(MethodSymbol.class);
    assertThat(symbol.isUnknownSymbol()).isFalse();
  }

  @Test
  public void get_method_symbol_from_extended_call() {
    PhpFile file1 = file("file1.php", "<?php class FOO{public static function foo() {}} class BAR extends FOO{} BAR::foo();");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));
    FunctionSymbol symbol = Symbols.get(firstDescendant(ast, FunctionCallTree.class).get());
    assertThat(symbol).isInstanceOf(MethodSymbol.class);
    assertThat(symbol.isUnknownSymbol()).isFalse();
  }

  @Test
  public void get_method_symbol_from_unknown_extended_call() {
    PhpFile file1 = file("file1.php", "<?php class BAR extends FOO{} BAR::foo();");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));
    FunctionSymbol symbol = Symbols.get(firstDescendant(ast, FunctionCallTree.class).get());
    assertThat(symbol).isInstanceOf(MethodSymbol.class);
    assertThat(symbol.isUnknownSymbol()).isTrue();
  }

  @Test
  public void do_not_get_method_symbol_from_unresolvable_call() {
    PhpFile file1 = file("file1.php", "<?php class FOO{public static function foo() {}} FOO::$foo();");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));
    assertThat(Symbols.get(firstDescendant(ast, FunctionCallTree.class).get()).isUnknownSymbol()).isTrue();
  }

  @Test
  public void do_not_get_function_symbol_from_new_expression_call() {
    PhpFile file1 = file("file1.php", "<?php class FOO{} new FOO();");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));
    assertThat(Symbols.get(firstDescendant(ast, FunctionCallTree.class).get()).isUnknownSymbol()).isTrue();
  }

  @Test
  public void get_method_with_yield_return() {
    PhpFile file1 = file("file1.php", "<?php class A {public function foo(){yield 1;}}");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));

    Optional<MethodDeclarationTree> methodDeclaration = firstDescendant(ast, MethodDeclarationTree.class);
    MethodSymbol methodSymbol = Symbols.get(methodDeclaration.get());
    assertThat(methodSymbol.hasReturn()).isTrue();
  }

  @Test
  public void get_abstract_method() {
    PhpFile file1 = file("file1.php", "<?php abstract class A {abstract public function foo(){}}");
    Tree ast = getAst(file1, buildProjectSymbolData(file1));

    Optional<MethodDeclarationTree> methodDeclaration = firstDescendant(ast, MethodDeclarationTree.class);
    MethodSymbol methodSymbol = Symbols.get(methodDeclaration.get());
    assertThat(methodSymbol.isAbstract().isTrue()).isTrue();
  }

  private ProjectSymbolData buildProjectSymbolData(PhpFile... files) {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    for (PhpFile file : files) {
      Tree ast = parser.parse(file.contents());
      SymbolTableImpl symbolTable = SymbolTableImpl.create((CompilationUnitTree) ast, new ProjectSymbolData(), file);
      symbolTable.classSymbolDatas().forEach(projectSymbolData::add);
      symbolTable.functionSymbolDatas().forEach(projectSymbolData::add);
    }
    return projectSymbolData;
  }

  private Tree getAst(PhpFile file, ProjectSymbolData projectSymbolData) {
    Tree ast = parser.parse(file.contents());
    SymbolTableImpl.create((CompilationUnitTree) ast, projectSymbolData, file);

    return ast;
  }

  private String filePath(String fileName) {
    return path(fileName).toFile().getAbsolutePath();
  }

  private Path path(String fileName) {
    return Paths.get(fileName);
  }

  private PhpFile file(String name, String content) {
    return new TestFile(content, name);
  }
}
