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
package org.sonar.php.tree.impl.declaration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;

public class ParameterListTreeTest extends PHPTreeModelTest {

  @Test
  public void empty() throws Exception {
    ParameterListTree tree = parameterList("()");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.parameters()).isEmpty();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

  @Test
  public void not_empty() throws Exception {
    assertThat(parameterList("($p1)").parameters()).hasSize(1);
    assertThat(parameterList("($p1, $p2)").parameters()).hasSize(2);
  }

  private ParameterListTree parameterList(String toParse) throws Exception {
    ParameterListTree tree = parse(toParse, PHPLexicalGrammar.PARAMETER_LIST);
    assertThat(tree.is(Kind.PARAMETER_LIST)).isTrue();
    return tree;
  }
  
}
