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
package org.sonar.php.metrics;

import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CommentLineVisitorTest extends ParsingTestUtils {

  @Test
  void commentLines() {
    CommentLineVisitor comment = new CommentLineVisitor(parse("metrics/comments.php"));

    assertThat(comment.commentLineNumber()).isEqualTo(3);
    assertThat(comment.commentLines()).contains(3, 7, 11);
    assertThat(comment.noSonarLines()).containsOnly(14, 15);
  }

}
