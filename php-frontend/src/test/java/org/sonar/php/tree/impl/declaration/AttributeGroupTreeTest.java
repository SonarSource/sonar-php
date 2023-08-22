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

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeGroupTreeTest extends PHPTreeModelTest {
  @Test
  void simpleGroup() {
    AttributeGroupTree tree = parse("#[A,B($x)]", PHPLexicalGrammar.ATTRIBUTE_GROUP);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE_GROUP)).isTrue();
    assertThat(tree.attributes()).hasSize(2);
    assertThat(tree.startToken()).hasToString("#[");
    assertThat(tree.endToken()).hasToString("]");
  }
}
