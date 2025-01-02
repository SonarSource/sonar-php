/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.impl.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeTreeTest extends PHPTreeModelTest {
  @Test
  void simpleAttribute() {
    AttributeTree tree = parse("A", PHPLexicalGrammar.ATTRIBUTE);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE)).isTrue();
    assertThat(tree.name()).hasToString("A");
  }

  @Test
  void withArgumentsAndFqn() {
    AttributeTree tree = parse("\\A\\B\\C($x, y: $y)", PHPLexicalGrammar.ATTRIBUTE);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE)).isTrue();
    assertThat(tree.name()).hasToString("\\A\\B\\C");

    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().get(0).name()).isNull();
    assertThat(tree.arguments().get(1).name()).hasToString("y");
  }
}
