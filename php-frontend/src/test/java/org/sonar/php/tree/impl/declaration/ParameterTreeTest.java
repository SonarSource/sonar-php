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
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;

public class ParameterTreeTest extends PHPTreeModelTest {

  @Test
  public void only_identifier() throws Exception {
    ParameterTree tree = parse("$param1", PHPLexicalGrammar.PARAMETER);
    assertThat(tree.is(Kind.PARAMETER)).isTrue();
    assertThat(tree.type()).isNull();
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.ellipsisToken()).isNull();
    assertThat(tree.variableIdentifier().variableExpression().text()).isEqualTo("$param1");
    assertThat(tree.equalToken()).isNull();
    assertThat(tree.initValue()).isNull();
  }

  @Test
  public void full() throws Exception {
    ParameterTree tree = parse("Class1&...$param1=$value1", PHPLexicalGrammar.PARAMETER);
    assertThat(tree.type().typeName().is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.referenceToken().text()).isEqualTo("&");
    assertThat(tree.ellipsisToken().text()).isEqualTo("...");
    assertThat(tree.variableIdentifier().variableExpression().text()).isEqualTo("$param1");
    assertThat(tree.equalToken().text()).isEqualTo("=");
    assertThat(tree.initValue().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
  }

}
