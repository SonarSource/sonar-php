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
package org.sonar.php.tree.impl.statement;

import java.util.List;
import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.EchoTagStatementTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.InlineHTMLTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

import static org.fest.assertions.Assertions.assertThat;

public class EchoTagStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void start_without_echo_tag() {
    CompilationUnitTree tree = parse("<?php $a ?>", PHPLexicalGrammar.COMPILATION_UNIT);
    ScriptTree script = tree.script();
    List<StatementTree> statements = script.statements();
    assertThat(statements).hasSize(1);
    StatementTree statement = statements.get(0);
    assertThat(statement.getKind()).isEqualTo(Kind.EXPRESSION_STATEMENT);
    assertThat(statement).isInstanceOf(ExpressionStatementTree.class);
    assertThat(((ExpressionStatementTree)statement).expression().getKind()).isEqualTo(Kind.VARIABLE_IDENTIFIER);
  }

  @Test
  public void start_with_echo_tag() {
    CompilationUnitTree tree = parse("<?= $a ?>", PHPLexicalGrammar.COMPILATION_UNIT);
    ScriptTree script = tree.script();
    List<StatementTree> statements = script.statements();
    assertThat(statements).hasSize(1);
    StatementTree statement = statements.get(0);
    assertThat(statement.getKind()).isEqualTo(Kind.ECHO_TAG_STATEMENT);
    assertThat(statement).isInstanceOf(EchoTagStatementTree.class);
    SeparatedList<ExpressionTree> expressions = ((EchoTagStatementTree) statement).expressions();
    assertThat(expressions).hasSize(1);
    assertThat(expressions.get(0).getKind()).isEqualTo(Kind.VARIABLE_IDENTIFIER);
  }

  @Test
  public void start_with_echo_tag_with_several_arguments() {
    CompilationUnitTree tree = parse("<?= $a, $b ?>", PHPLexicalGrammar.COMPILATION_UNIT);
    ScriptTree script = tree.script();
    List<StatementTree> statements = script.statements();
    assertThat(statements).hasSize(1);
    StatementTree statement = statements.get(0);
    assertThat(statement.getKind()).isEqualTo(Kind.ECHO_TAG_STATEMENT);
    assertThat(statement).isInstanceOf(EchoTagStatementTree.class);
    SeparatedList<ExpressionTree> expressions = ((EchoTagStatementTree) statement).expressions();
    assertThat(expressions).hasSize(2);
    assertThat(expressions.get(0).getKind()).isEqualTo(Kind.VARIABLE_IDENTIFIER);
    assertThat(expressions.get(1).getKind()).isEqualTo(Kind.VARIABLE_IDENTIFIER);
  }

  @Test
  public void several_echo_tags() {
    CompilationUnitTree tree = parse("<?php foo(); ?> 1 <?= $a ?> 2 <?= $b; ?> 3 <?= $c; bar(); ?> 4", PHPLexicalGrammar.COMPILATION_UNIT);
    ScriptTree script = tree.script();
    List<StatementTree> statements = script.statements();
    assertThat(statements).hasSize(8);

    assertThat(statements.get(0).getKind()).isEqualTo(Kind.EXPRESSION_STATEMENT);
    assertThat(((ExpressionStatementTree)statements.get(0)).expression().getKind()).isEqualTo(Kind.FUNCTION_CALL);
    assertThat(((ExpressionStatementTree)statements.get(0)).eosToken().text()).isEqualTo(";");

    assertThat(statements.get(1).getKind()).isEqualTo(Kind.INLINE_HTML);
    assertThat(((InlineHTMLTree)statements.get(1)).inlineHTMLToken().text()).isEqualTo("?> 1 <?=");

    assertThat(statements.get(2).getKind()).isEqualTo(Kind.ECHO_TAG_STATEMENT);
    assertThat(((EchoTagStatementTree)statements.get(2)).expressions()).hasSize(1);
    assertThat(((EchoTagStatementTree)statements.get(2)).eosToken().text()).isEqualTo("?> 2 <?=");

    assertThat(statements.get(3).getKind()).isEqualTo(Kind.ECHO_TAG_STATEMENT);
    assertThat(((EchoTagStatementTree)statements.get(3)).expressions()).hasSize(1);
    assertThat(((EchoTagStatementTree)statements.get(3)).eosToken().text()).isEqualTo(";");

    assertThat(statements.get(4).getKind()).isEqualTo(Kind.INLINE_HTML);
    assertThat(((InlineHTMLTree)statements.get(4)).inlineHTMLToken().text()).isEqualTo("?> 3 <?=");

    assertThat(statements.get(5).getKind()).isEqualTo(Kind.ECHO_TAG_STATEMENT);
    assertThat(((EchoTagStatementTree)statements.get(5)).expressions()).hasSize(1);
    assertThat(((EchoTagStatementTree)statements.get(5)).eosToken().text()).isEqualTo(";");

    assertThat(statements.get(6).getKind()).isEqualTo(Kind.EXPRESSION_STATEMENT);
    assertThat(((ExpressionStatementTree)statements.get(6)).expression().getKind()).isEqualTo(Kind.FUNCTION_CALL);
    assertThat(((ExpressionStatementTree)statements.get(6)).eosToken().text()).isEqualTo(";");

    assertThat(statements.get(7).getKind()).isEqualTo(Kind.INLINE_HTML);
    assertThat(((InlineHTMLTree)statements.get(7)).inlineHTMLToken().text()).isEqualTo("?> 4");
  }

  @Test
  public void echo_tags_in_a_block() {
    CompilationUnitTree tree = parse("<?php if (true) { ?> 1 <?= $a ?> 2 <?php }", PHPLexicalGrammar.COMPILATION_UNIT);
    ScriptTree script = tree.script();
    List<StatementTree> statements = script.statements();
    assertThat(statements).hasSize(1);
    assertThat(statements.get(0).getKind()).isEqualTo(Kind.IF_STATEMENT);
    IfStatementTree ifStatement = (IfStatementTree)statements.get(0);
    assertThat(ifStatement.statements()).hasSize(1);
    assertThat(ifStatement.statements().get(0).getKind()).isEqualTo(Kind.BLOCK);
    BlockTree block = (BlockTree)ifStatement.statements().get(0);
    List<StatementTree> blockStatements = block.statements();
    assertThat(blockStatements).hasSize(2);

    assertThat(blockStatements.get(0).getKind()).isEqualTo(Kind.INLINE_HTML);
    assertThat(((InlineHTMLTree) blockStatements.get(0)).inlineHTMLToken().text()).isEqualTo("?> 1 <?=");

    assertThat(blockStatements.get(1).getKind()).isEqualTo(Kind.ECHO_TAG_STATEMENT);
    assertThat(((EchoTagStatementTree) blockStatements.get(1)).expressions()).hasSize(1);
    assertThat(((EchoTagStatementTree) blockStatements.get(1)).eosToken().text()).isEqualTo("?> 2 <?php");
  }

  @Test
  public void empty_echo_tag() {
    CompilationUnitTree tree = parse("<?=", PHPLexicalGrammar.COMPILATION_UNIT);
    ScriptTree script = tree.script();
    List<StatementTree> statements = script.statements();
    assertThat(statements).hasSize(0);
  }

}
