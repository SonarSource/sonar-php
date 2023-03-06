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
package org.sonar.php.tree.impl.declaration;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeTreeTest extends PHPTreeModelTest {
  @Test
  public void simple_attribute() throws Exception {
    AttributeTree tree = parse("A", PHPLexicalGrammar.ATTRIBUTE);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE)).isTrue();
    assertThat(tree.name()).hasToString("A");
  }

  @Test
  public void with_arguments_and_fqn() throws Exception {
    AttributeTree tree = parse("\\A\\B\\C($x, y: $y)", PHPLexicalGrammar.ATTRIBUTE);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE)).isTrue();
    assertThat(tree.name()).hasToString("\\A\\B\\C");

    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().get(0).name()).isNull();
    assertThat(tree.arguments().get(1).name()).hasToString("y");
  }
}
