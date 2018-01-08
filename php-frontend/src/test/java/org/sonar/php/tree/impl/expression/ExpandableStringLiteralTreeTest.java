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
package org.sonar.php.tree.impl.expression;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpandableStringLiteralTreeTest extends PHPTreeModelTest {

  @Test
  public void simple_variable() throws Exception {
    ExpandableStringLiteralTree tree = parse("\"simple var: $a\"", Kind.EXPANDABLE_STRING_LITERAL);

    assertExpandableStringLiteral(tree, 1, 1);

    assertFirstString(tree, "simple var: ");
    assertFirstExpression(tree, "$a", Kind.VARIABLE_IDENTIFIER);
  }

  @Test
  public void semi_complex_variable() throws Exception {
    ExpandableStringLiteralTree tree = parse("\"semi-complex var: ${$a}\"", Kind.EXPANDABLE_STRING_LITERAL);

    assertExpandableStringLiteral(tree, 1, 1);

    assertFirstString(tree, "semi-complex var: ");
    assertFirstExpression(tree, "${$a}", Kind.COMPOUND_VARIABLE_NAME);
  }

  @Test
  public void complex_variable() throws Exception {
    ExpandableStringLiteralTree tree = parse("\"complex var: {$a}\"", Kind.EXPANDABLE_STRING_LITERAL);

    assertExpandableStringLiteral(tree, 1, 1);

    assertFirstString(tree, "complex var: ");
    assertFirstExpression(tree, "{$a}", Kind.COMPUTED_VARIABLE_NAME);
  }

  @Test
  public void multiple_variables_and_strings() throws Exception {
    ExpandableStringLiteralTree tree = parse("\"1st var: $a - 2nd composed vars: $b$c\"", Kind.EXPANDABLE_STRING_LITERAL);

    assertExpandableStringLiteral(tree, 2, 3);

    assertFirstString(tree, "1st var: ");
    assertFirstExpression(tree, "$a", Kind.VARIABLE_IDENTIFIER);
  }

  @Test
  public void test_pseudo_comment() throws Exception {
    parse("\"/**/{$a}\"", Kind.EXPANDABLE_STRING_LITERAL);
  }

  private void assertExpandableStringLiteral(ExpandableStringLiteralTree tree, int stringsSize, int expressionsSize) {
    assertThat(tree.is(Kind.EXPANDABLE_STRING_LITERAL)).isTrue();

    assertThat(tree.openDoubleQuoteToken().text()).isEqualTo("\"");
    assertThat(tree.strings()).hasSize(stringsSize);
    assertThat(tree.expressions()).hasSize(expressionsSize);
    assertThat(tree.closeDoubleQuoteToken().text()).isEqualTo("\"");
  }

  private void assertFirstExpression(ExpandableStringLiteralTree tree, String s, Kind kind) {
    ExpressionTree expr = tree.expressions().get(0);

    assertThat(expr.is(kind)).isTrue();
    assertThat(expressionToString(expr)).isEqualTo(s);
  }

  private void assertFirstString(ExpandableStringLiteralTree tree, String s) {
    ExpandableStringCharactersTree string = tree.strings().get(0);

    assertThat(string.is(Kind.EXPANDABLE_STRING_CHARACTERS)).isTrue();
    assertThat(string.value()).isEqualTo(s);
  }

}
