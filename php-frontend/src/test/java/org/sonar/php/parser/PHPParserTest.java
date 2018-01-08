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
package org.sonar.php.parser;

import com.sonar.sslr.api.typed.ActionParser;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;

public class PHPParserTest {

  @Test
  public void expression() throws Exception {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.EXPRESSION);
    BinaryExpressionTree tree = (BinaryExpressionTree) parser.parse("$a + $b + $c");
    Assertions.assertThat(tree.getParent()).isNull();
    Assertions.assertThat(tree.leftOperand().getParent()).isSameAs(tree);
    Assertions.assertThat(tree.rightOperand().getParent()).isSameAs(tree);

    BinaryExpressionTree subExpression = (BinaryExpressionTree) tree.leftOperand();
    Assertions.assertThat(subExpression.leftOperand().getParent()).isSameAs(subExpression);
    Assertions.assertThat(subExpression.rightOperand().getParent()).isSameAs(subExpression);
  }

  @Test
  public void for_each() throws Exception {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.FOREACH_STATEMENT);
    ForEachStatementTree tree = (ForEachStatementTree) parser.parse("foreach ($arr as &$value) { }");
    Assertions.assertThat(tree.expression().getParent()).isSameAs(tree);
  }
}
