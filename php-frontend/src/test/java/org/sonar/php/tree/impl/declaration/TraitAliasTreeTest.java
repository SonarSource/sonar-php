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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.TraitAliasTree;

import static org.assertj.core.api.Assertions.assertThat;

class TraitAliasTreeTest extends PHPTreeModelTest {

  @Test
  void withAlias() {
    TraitAliasTree tree = alias("method1 as method2;");
    assertThat(tree.methodReference().method().text()).isEqualTo("method1");
    assertThat(tree.modifierToken()).isNull();
    assertThat(tree.alias().text()).isEqualTo("method2");
  }

  @Test
  void withModifier() {
    TraitAliasTree tree = alias("method1 as public;");
    assertThat(tree.methodReference().method().text()).isEqualTo("method1");
    assertThat(tree.modifierToken().text()).isEqualTo("public");
    assertThat(tree.alias()).isNull();
  }

  @Test
  void withAliasAndModifier() {
    TraitAliasTree tree = alias("method1 as public method2;");
    assertThat(tree.methodReference().method().text()).isEqualTo("method1");
    assertThat(tree.modifierToken().text()).isEqualTo("public");
    assertThat(tree.alias().text()).isEqualTo("method2");
  }

  private TraitAliasTree alias(String toParse) {
    TraitAliasTree tree = parse(toParse, PHPLexicalGrammar.TRAIT_ALIAS);
    assertThat(tree.is(Kind.TRAIT_ALIAS)).isTrue();
    assertThat(tree.asToken().text()).isEqualTo("as");
    assertThat(tree.eosToken().text()).isEqualTo(";");
    return tree;
  }

}
