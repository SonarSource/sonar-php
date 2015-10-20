/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.metrics;

import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class CommentLineVisitorTest extends MetricTest {

  @Test
  public void comment_lines() throws Exception {
    CommentLineVisitor comment = new CommentLineVisitor(parse("comments.php"));

    assertThat(comment.commentLineNumber()).isEqualTo(3);
    assertThat(comment.commentLines()).contains(3);
    assertThat(comment.commentLines()).contains(7);
    assertThat(comment.commentLines()).contains(11);
    assertThat(comment.commentLines().contains(2)).isFalse();
    assertThat(comment.noSonarLines()).containsOnly(14);
  }

  @Ignore // FIXME SONARPHP-575
  @Test
  public void comment_line_ignoring_header() throws Exception {
    CommentLineVisitor comment = new CommentLineVisitor(parse("comments.php"));

    assertThat(comment.commentLineNumber()).isEqualTo(2);
    assertThat(comment.commentLines().contains(3)).isFalse();
    assertThat(comment.commentLines()).contains(7);
    assertThat(comment.commentLines()).contains(11);
    assertThat(comment.noSonarLines()).containsOnly(14);
  }

}
