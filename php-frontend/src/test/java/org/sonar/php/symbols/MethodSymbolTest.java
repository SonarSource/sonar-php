/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
import org.junit.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.php.tree.impl.expression.AnonymousClassTreeImpl;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.tree.TreeUtils.firstDescendant;

public class MethodSymbolTest {

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  @Test
  public void simple_method() {
    Tree ast = parse("<?php class A { function foo($p1) {} }");
    ClassSymbol a = Symbols.get(firstDescendant(ast, ClassDeclarationTreeImpl.class).get());
    MethodSymbol foo = firstDescendant(ast, MethodDeclarationTreeImpl.class).get().symbol();
    assertThat(a.declaredMethods().get(0)).isSameAs(foo);
  }

  @Test
  public void method_in_anonymous_class() {
    Tree ast = parse("<?php $x = new class { function foo($p1) {} };");
    ClassSymbol anonymous = Symbols.get(firstDescendant(ast, AnonymousClassTreeImpl.class).get());
    MethodSymbol foo = firstDescendant(ast, MethodDeclarationTreeImpl.class).get().symbol();
    assertThat(anonymous.declaredMethods().get(0)).isSameAs(foo);
  }

  private CompilationUnitTree parse(String... lines) {
    String source = String.join("\n", lines);
    TestFile file = new TestFile(source, "file1.php");
    CompilationUnitTree ast = (CompilationUnitTree) parser.parse(source);
    SymbolTableImpl.create(ast, new ProjectSymbolData(), file);
    return ast;
  }
}
