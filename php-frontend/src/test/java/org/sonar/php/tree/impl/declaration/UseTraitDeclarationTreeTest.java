/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

class UseTraitDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void withoutAdaptations() {
    UseTraitDeclarationTree tree = parse("use A,B,C;", Kind.USE_TRAIT_DECLARATION);
    assertThat(tree.is(Kind.USE_TRAIT_DECLARATION)).isTrue();
    assertThat(tree.traits()).hasSize(3);
    assertThat(tree.openCurlyBraceToken()).isNull();
    assertThat(tree.adaptations()).isEmpty();
    assertThat(tree.closeCurlyBraceToken()).isNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  void withAdaptations() {
    UseTraitDeclarationTree tree = parse("use A,B,C { m1 as m2; }", Kind.USE_TRAIT_DECLARATION);
    assertThat(tree.is(Kind.USE_TRAIT_DECLARATION)).isTrue();
    assertThat(tree.traits()).hasSize(3);
    assertThat(tree.openCurlyBraceToken().text()).isEqualTo("{");
    assertThat(tree.adaptations()).hasSize(1);
    assertThat(tree.closeCurlyBraceToken().text()).isEqualTo("}");
    assertThat(tree.eosToken()).isNull();
  }
}
