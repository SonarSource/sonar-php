/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;

import static org.assertj.core.api.Assertions.assertThat;

class UnionTypeTreeTest extends PHPTreeModelTest {
  @Test
  void simpleContent() {
    UnionTypeTree tree = parse("int|string", PHPLexicalGrammar.UNION_TYPE);

    assertThat(tree.is(Tree.Kind.UNION_TYPE)).isTrue();
    assertThat(tree.isSimple()).isFalse();
    assertThat(tree.types()).hasSize(2);
    assertThat(tree.types().get(0).typeName().is(Tree.Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(tree.types().get(1).typeName().is(Tree.Kind.BUILT_IN_TYPE)).isTrue();
  }

}
