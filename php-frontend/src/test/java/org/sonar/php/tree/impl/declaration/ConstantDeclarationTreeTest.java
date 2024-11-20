/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

class ConstantDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void test() throws Exception {
    ConstantDeclarationTree tree = parse("const A = 1, B = 2;", PHPLexicalGrammar.CONSTANT_DECLARATION);
    assertThat(tree.is(Kind.CONSTANT_DECLARATION)).isTrue();
    assertThat(tree.constToken().text()).isEqualTo("const");
    assertThat(tree.declarations()).hasSize(2);
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
