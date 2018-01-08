/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.metrics;

import org.junit.Test;
import org.sonar.php.ParsingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;


public class CommentLineVisitorTest extends ParsingTestUtils {

  @Test
  public void comment_lines() throws Exception {
    CommentLineVisitor comment = new CommentLineVisitor(parse("metrics/comments.php"));

    assertThat(comment.commentLineNumber()).isEqualTo(3);
    assertThat(comment.commentLines()).contains(3, 7, 11);
    assertThat(comment.noSonarLines()).containsOnly(14, 15);
  }

}
