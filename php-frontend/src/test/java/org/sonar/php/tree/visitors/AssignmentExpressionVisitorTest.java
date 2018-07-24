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
package org.sonar.php.tree.visitors;

import java.util.Optional;
import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

import static org.assertj.core.api.Assertions.assertThat;

public class AssignmentExpressionVisitorTest {

  @Test
  public void getAssignmentValue() throws Exception {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.Of("$a").from("<?php function foo() { $a = 1; }");

    assertThat(uniqueAssignedValue).isPresent();
    ExpressionTree value = uniqueAssignedValue.get();
    assertThat(value).isInstanceOf(LiteralTree.class);
    assertThat(((LiteralTree) value).value()).isEqualTo("1");
  }

  @Test
  public void getAssignmentValue_global() throws Exception {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.Of("$a").from("<?php $a = 1;");

    assertThat(uniqueAssignedValue).isPresent();
    ExpressionTree value = uniqueAssignedValue.get();
    assertThat(value).isInstanceOf(LiteralTree.class);
    assertThat(((LiteralTree) value).value()).isEqualTo("1");
  }

  @Test
  public void getAssignmentValue_multiple() throws Exception {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.Of("$a").from("<?php $a = 1;\n$a = 2;");

    assertThat(uniqueAssignedValue).isNotPresent();
  }

  private static class UniqueAssignedValue extends PHPTreeModelTest {
    private String name;

    UniqueAssignedValue(String name) {
      this.name = name;
    }

    static UniqueAssignedValue Of(String name) {
      return new UniqueAssignedValue(name);
    }

    Optional<ExpressionTree> from(String code) throws Exception {
      CompilationUnitTree tree = parse(code, PHPLexicalGrammar.COMPILATION_UNIT);
      SymbolTable symbolTable = SymbolTableImpl.create(tree);

      AssignmentExpressionVisitor assignmentExpressionVisitor = new AssignmentExpressionVisitor(symbolTable);
      tree.accept(assignmentExpressionVisitor);
      IdentifierTree var = ((SymbolTableImpl) symbolTable).getSymbols(name).get(0).declaration();
      Symbol symbol = symbolTable.getSymbol(var);
      return assignmentExpressionVisitor.getUniqueAssignedValue(symbol);
    }
  }

}
