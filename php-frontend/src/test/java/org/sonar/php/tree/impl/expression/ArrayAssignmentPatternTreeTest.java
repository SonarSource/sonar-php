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

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternTree;

import static org.assertj.core.api.Assertions.assertThat;

class ArrayAssignmentPatternTreeTest extends PHPTreeModelTest {

  @Test
  void simple() {
    ArrayAssignmentPatternTree tree = parse("[$a, , $b]", Kind.ARRAY_ASSIGNMENT_PATTERN);

    assertThat(tree.is(Kind.ARRAY_ASSIGNMENT_PATTERN)).isTrue();

    assertThat(tree.openBracketToken().text()).isEqualTo("[");
    assertThat(tree.elements()).hasSize(3);
    assertThat(tree.separators()).hasSize(2);
    Optional<ArrayAssignmentPatternElementTree> firstElement = tree.elements().get(0);
    assertThat(firstElement).isPresent();
    assertThat(expressionToString(firstElement.get())).isEqualTo("$a");
    assertThat(tree.elements().get(1)).isEmpty();
    Optional<ArrayAssignmentPatternElementTree> thirdElement = tree.elements().get(2);
    assertThat(thirdElement).isPresent();
    assertThat(expressionToString(thirdElement.get())).isEqualTo("$b");
    assertThat(tree.closeBracketToken().text()).isEqualTo("]");
  }

}
