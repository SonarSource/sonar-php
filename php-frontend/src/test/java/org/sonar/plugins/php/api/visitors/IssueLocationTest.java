/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.plugins.php.api.visitors;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class IssueLocationTest {

  private static final Tree TOKEN1 = createToken(5, 10, "token1");
  private static final Tree TOKEN2 = createToken(12, 0, "token2");

  @Test
  public void test() throws Exception {
    IssueLocation issueLocation = new IssueLocation(TOKEN1, "Test message");

    assertThat(issueLocation.message()).isEqualTo("Test message");
    assertThat(issueLocation.startLine()).isEqualTo(5);
    assertThat(issueLocation.startLineOffset()).isEqualTo(10);
    assertThat(issueLocation.endLine()).isEqualTo(5);
    assertThat(issueLocation.endLineOffset()).isEqualTo(16);
    assertThat(issueLocation.filePath()).isNull();
  }

  @Test
  public void without_message() throws Exception {
    IssueLocation issueLocation = new IssueLocation(TOKEN1, null);

    assertThat(issueLocation.message()).isNull();
    assertThat(issueLocation.startLine()).isEqualTo(5);
  }

  @Test
  public void test_two_trees_constructor() throws Exception {
    IssueLocation issueLocation = new IssueLocation(TOKEN1, TOKEN2, "Test message");

    assertThat(issueLocation.message()).isEqualTo("Test message");
    assertThat(issueLocation.startLine()).isEqualTo(5);
    assertThat(issueLocation.startLineOffset()).isEqualTo(10);
    assertThat(issueLocation.endLine()).isEqualTo(12);
    assertThat(issueLocation.endLineOffset()).isEqualTo(6);
  }

  @Test
  public void location_in_file() {
    LocationInFileImpl locationInFile = new LocationInFileImpl("dir1/file1.php", 1, 2, 3, 4);
    IssueLocation issueLocation = new IssueLocation(locationInFile, "Test message");
    assertThat(issueLocation.message()).isEqualTo("Test message");
    assertThat(issueLocation.startLine()).isEqualTo(1);
    assertThat(issueLocation.startLineOffset()).isEqualTo(2);
    assertThat(issueLocation.endLine()).isEqualTo(3);
    assertThat(issueLocation.endLineOffset()).isEqualTo(4);
    assertThat(issueLocation.filePath()).isEqualTo("dir1/file1.php");
  }

  private static Tree createToken(int line, int column, String tokenValue) {
    return new InternalSyntaxToken(line, column, tokenValue, ImmutableList.of(), 0, false);
  }

}
