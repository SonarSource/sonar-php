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
package org.sonar.php.tree.impl.statement;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

public class CatchBlockTreeTest extends PHPTreeModelTest {

  @Test
  public void test() throws Exception {
    CatchBlockTree tree = parse("catch (ExceptionType $e) {}", PHPLexicalGrammar.CATCH_BLOCK);
    assertThat(tree.is(Kind.CATCH_BLOCK)).isTrue();
    assertThat(tree.catchToken().text()).isEqualTo("catch");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.exceptionTypes()).hasSize(1);
    assertThat(tree.exceptionTypes().get(0).fullName()).isEqualTo("ExceptionType");
    assertThat(tree.variable().variableExpression().text()).isEqualTo("$e");
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(expressionToString(tree.block())).isEqualTo("{}");
  }

  @Test
  public void multi_catch() throws Exception {
    CatchBlockTree tree = parse("catch (FirstException | SecondException | ThirdException $e) {}", PHPLexicalGrammar.CATCH_BLOCK);
    assertThat(tree.is(Kind.CATCH_BLOCK)).isTrue();
    assertThat(tree.catchToken().text()).isEqualTo("catch");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.exceptionTypes()).hasSize(3);
    assertThat(tree.exceptionTypes().get(0).fullName()).isEqualTo("FirstException");
    assertThat(tree.exceptionTypes().get(1).fullName()).isEqualTo("SecondException");
    assertThat(tree.exceptionTypes().get(2).fullName()).isEqualTo("ThirdException");
    assertThat(tree.variable().variableExpression().text()).isEqualTo("$e");
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(expressionToString(tree.block())).isEqualTo("{}");
  }

  @Test
  public void catch_without_variable() throws Exception {
    CatchBlockTree tree = parse("catch (ExceptionType) {}", PHPLexicalGrammar.CATCH_BLOCK);
    assertThat(tree.exceptionTypes().get(0).fullName()).isEqualTo("ExceptionType");
    assertNull(tree.variable());
  }

}
