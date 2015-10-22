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
package org.sonar.php.tree.visitors;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.sonar.php.tree.impl.expression.NameIdentifierTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;

import java.util.Collections;

public class PHPIssueTest {

  @Test
  public void test_no_line() throws Exception {
    PHPIssue issue = new PHPIssue("key", "message");

    Assertions.assertThat(issue.ruleKey()).isEqualTo("key");
    Assertions.assertThat(issue.message()).isEqualTo("message");
    Assertions.assertThat(issue.line()).isEqualTo(0);
    Assertions.assertThat(issue.cost()).isNull();
  }

  @Test
  public void test_with_line() throws Exception {
    final int line = 7;
    PHPIssue issue = new PHPIssue("key", "message").line(line);

    Assertions.assertThat(issue.ruleKey()).isEqualTo("key");
    Assertions.assertThat(issue.message()).isEqualTo("message");
    Assertions.assertThat(issue.line()).isEqualTo(line);
    Assertions.assertThat(issue.cost()).isNull();
  }

  @Test
  public void test_with_line_and_cost() throws Exception {
    final int cost = 7;
    PHPIssue issue = new PHPIssue("key", "message").cost(cost);

    Assertions.assertThat(issue.ruleKey()).isEqualTo("key");
    Assertions.assertThat(issue.message()).isEqualTo("message");
    Assertions.assertThat(issue.line()).isEqualTo(0);
    Assertions.assertThat(issue.cost()).isEqualTo(cost);
  }

  @Test
  public void test_setting_line_from_tree() throws Exception {
    final int line = 3;
    NameIdentifierTreeImpl tree = new NameIdentifierTreeImpl(new InternalSyntaxToken(line, 1, "tree", Collections.EMPTY_LIST, 0, false, null));
    PHPIssue issue = new PHPIssue("key", "message").tree(tree);

    Assertions.assertThat(issue.ruleKey()).isEqualTo("key");
    Assertions.assertThat(issue.message()).isEqualTo("message");
    Assertions.assertThat(issue.line()).isEqualTo(line);
    Assertions.assertThat(issue.cost()).isNull();
  }

}
