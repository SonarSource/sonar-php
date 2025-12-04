/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class ExpandableStringCharactersTreeTest extends PHPTreeModelTest {

  @Test
  void test() throws Exception {
    ExpandableStringCharactersTreeImpl tree = parse("characters with spaces", Kind.EXPANDABLE_STRING_CHARACTERS);

    assertThat(tree.is(Kind.EXPANDABLE_STRING_CHARACTERS)).isTrue();
    assertThat(tree.value()).isEqualTo("characters with spaces");
    assertThat(tree.token().text()).isEqualTo(tree.value());
  }

}
