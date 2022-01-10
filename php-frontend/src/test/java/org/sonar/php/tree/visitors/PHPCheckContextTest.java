/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.php.tree.visitors;

import java.io.File;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.sonar.php.FileTestUtils;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.regex.PhpRegexCheck;
import org.sonar.php.regex.RegexParserTestUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonarsource.analyzer.commons.regex.ast.CurlyBraceQuantifier;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.RepetitionTree;

import static org.assertj.core.api.Assertions.assertThat;

public class PHPCheckContextTest {

  private static final File PHP_FILE = new File("src/test/resources/visitors/test.php");
  private static final PhpFile PHP_INPUT_FILE = FileTestUtils.getFile(PHP_FILE);
  private PHPCheckContext context;

  @Before
  public void setUp() throws Exception {
    CompilationUnitTree compilationUnitTree = ParsingTestUtils.parse(PHP_FILE);
    context = new PHPCheckContext(PHP_INPUT_FILE, compilationUnitTree, PHP_FILE);
  }

  @Test
  public void test_regex_newIssue() {
    PhpRegexCheck regexCheck = new TestPhpRegexCheck();
    String regex = "'/x{42}|y{23}/'";
    RegexTree regexTree = RegexParserTestUtils.assertSuccessfulParse(regex);
    DisjunctionTree disjunctionTree = (DisjunctionTree) regexTree;
    RepetitionTree y23 = (RepetitionTree) disjunctionTree.getAlternatives().get(1);
    CurlyBraceQuantifier rep23 = (CurlyBraceQuantifier) y23.getQuantifier();

    PreciseIssue issue = context.newIssue(regexCheck, rep23, "regexMsg");

    assertThat(issue.check()).isSameAs(regexCheck);
    IssueLocation issueLocation = issue.primaryLocation();
    assertThat(issueLocation.message()).isEqualTo("regexMsg");
    assertThat(issueLocation.startLine()).isEqualTo(3);
    assertThat(issueLocation.startLineOffset()).isEqualTo(9);
    assertThat(issueLocation.endLine()).isEqualTo(3);
    assertThat(issueLocation.endLineOffset()).isEqualTo(13);
  }

  static class TestPhpRegexCheck extends PHPVisitorCheck implements PhpRegexCheck {
  }
}
