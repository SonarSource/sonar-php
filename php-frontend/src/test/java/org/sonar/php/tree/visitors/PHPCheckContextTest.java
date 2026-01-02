/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.tree.visitors;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.sonar.php.FileTestUtils;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.regex.PhpRegexCheck;
import org.sonar.php.regex.RegexParserTestUtils;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpInputFileContext;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonarsource.analyzer.commons.regex.ast.CurlyBraceQuantifier;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.RepetitionTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PHPCheckContextTest {

  private static final File WORKDIR = new File("src/test/resources/visitors");
  private static final File PHP_FILE = new File("src/test/resources/visitors/test.php");
  private static final PhpFile PHP_INPUT_FILE = FileTestUtils.getFile(PHP_FILE);
  private static final CompilationUnitTree TREE = ParsingTestUtils.parse(PHP_FILE);

  private static final CacheContext CACHE_CONTEXT = mock(CacheContext.class);

  @Test
  void testRegexNewIssue() {
    PHPCheckContext context = new PHPCheckContext(PHP_INPUT_FILE, TREE, PHP_FILE);
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

  @Test
  void shouldReturnCacheContextWhenConstructedWith_PhpInputFileContext() {
    PhpInputFileContext inputFileContext = new PhpInputFileContext(PHP_INPUT_FILE, WORKDIR, CACHE_CONTEXT);
    CheckContext checkContext = new PHPCheckContext(inputFileContext, TREE, SymbolTableImpl.create(TREE));
    assertThat(checkContext.cacheContext()).isEqualTo(CACHE_CONTEXT);
  }

  static class TestPhpRegexCheck extends PHPVisitorCheck implements PhpRegexCheck {
  }
}
