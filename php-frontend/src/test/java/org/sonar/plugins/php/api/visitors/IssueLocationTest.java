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
package org.sonar.plugins.php.api.visitors;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class IssueLocationTest {

  private static final Tree TOKEN1 = createToken(5, 10, "token1");
  private static final Tree TOKEN2 = createToken(12, 0, "token2");

  @Test
  void test() {
    IssueLocation issueLocation = new IssueLocation(TOKEN1, "Test message");

    assertThat(issueLocation.message()).isEqualTo("Test message");
    assertThat(issueLocation.startLine()).isEqualTo(5);
    assertThat(issueLocation.startLineOffset()).isEqualTo(10);
    assertThat(issueLocation.endLine()).isEqualTo(5);
    assertThat(issueLocation.endLineOffset()).isEqualTo(16);
    assertThat(issueLocation.filePath()).isNull();
  }

  @Test
  void withoutMessage() {
    IssueLocation issueLocation = new IssueLocation(TOKEN1, null);

    assertThat(issueLocation.message()).isNull();
    assertThat(issueLocation.startLine()).isEqualTo(5);
  }

  @Test
  void testTwoTreesConstructor() {
    IssueLocation issueLocation = new IssueLocation(TOKEN1, TOKEN2, "Test message");

    assertThat(issueLocation.message()).isEqualTo("Test message");
    assertThat(issueLocation.startLine()).isEqualTo(5);
    assertThat(issueLocation.startLineOffset()).isEqualTo(10);
    assertThat(issueLocation.endLine()).isEqualTo(12);
    assertThat(issueLocation.endLineOffset()).isEqualTo(6);
  }

  @Test
  void locationInFile() {
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
    return new InternalSyntaxToken(line, column, tokenValue, Collections.emptyList(), 0, false);
  }

}
