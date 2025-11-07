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
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;

import static org.assertj.core.api.Assertions.assertThat;

class ArrayPairTreeTest extends PHPTreeModelTest {

  @Test
  void value() throws Exception {
    ArrayPairTree tree = parse("$val", Kind.ARRAY_PAIR);

    assertThat(tree.is(Kind.ARRAY_PAIR)).isTrue();

    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(expressionToString(tree.value())).isEqualTo("$val");
  }

  @Test
  void keyValue() {
    ArrayPairTree tree = parse("$key => $val", Kind.ARRAY_PAIR);

    assertThat(tree.is(Kind.ARRAY_PAIR)).isTrue();

    assertThat(expressionToString(tree.key())).isEqualTo("$key");
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(expressionToString(tree.value())).isEqualTo("$val");
  }

  @Test
  void spreadOperator() {
    ArrayPairTree tree = parse("...$val", Kind.ARRAY_PAIR);
    assertThat(tree.ellipsisToken().text()).isEqualTo("...");
    assertThat(expressionToString(tree.value())).isEqualTo("$val");
  }

}
