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
package org.sonar.php.checks.wordpress;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

class WordPressImportDetectorTest {

  private final TestWordPressImportDetectorCheck check = new TestWordPressImportDetectorCheck();

  @Test
  void testWpLoad() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_load.php");
  }

  @Test
  void testWpConfig() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_config.php");
  }

  @Test
  void testWpSettings() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_settings.php");
  }

  @Test
  void testWpBlogHeader() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_blog_header.php");
  }

  @Test
  void testWithParentheses() {
    CheckVerifier.verify(check, "WordPressImportDetector/with_parentheses.php");
  }

  @Test
  void testWithConcatenation() {
    CheckVerifier.verify(check, "WordPressImportDetector/with_concatenation.php");
  }

  @Test
  void testWithConcatenationAndParentheses() {
    CheckVerifier.verify(check, "WordPressImportDetector/with_concatenation_and_parentheses.php");
  }

  @Test
  void testInclude() {
    CheckVerifier.verify(check, "WordPressImportDetector/include.php");
  }

  @Test
  void testIncludeOnce() {
    CheckVerifier.verify(check, "WordPressImportDetector/include_once.php");
  }

  @Test
  void testRequire() {
    CheckVerifier.verify(check, "WordPressImportDetector/require.php");
  }

  @Test
  void testDoubleQuotes() {
    CheckVerifier.verify(check, "WordPressImportDetector/double_quotes.php");
  }

  @Test
  void testWpIncludesPath() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_includes_path.php");
  }

  @Test
  void testWpIncludesFunctions() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_includes_functions.php");
  }

  @Test
  void testWpAdminPath() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_admin_path.php");
  }

  @Test
  void testAbsolutePath() {
    CheckVerifier.verify(check, "WordPressImportDetector/absolute_path.php");
  }

  @Test
  void testRelativePath() {
    CheckVerifier.verify(check, "WordPressImportDetector/relative_path.php");
  }

  @Test
  void testDirConcatenation() {
    CheckVerifier.verify(check, "WordPressImportDetector/dir_concatenation.php");
  }

  @Test
  void testFullPathConcatenation() {
    CheckVerifier.verify(check, "WordPressImportDetector/full_path_concatenation.php");
  }

  @Test
  void testNonWordPressFile() {
    CheckVerifier.verifyNoIssue(check, "WordPressImportDetector/non_wordpress_file.php");
  }

  @Test
  void testOtherFunction() {
    CheckVerifier.verifyNoIssue(check, "WordPressImportDetector/other_function.php");
  }

  @Test
  void testWpIncludesLoad() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_includes_load.php");
  }

  @Test
  void testWpIncludesPlugin() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_includes_plugin.php");
  }

  @Test
  void testWpAdminIncludesAdmin() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_admin_includes_admin.php");
  }

  @Test
  void testCaseInsensitive() {
    CheckVerifier.verify(check, "WordPressImportDetector/case_insensitive.php");
  }

  @Test
  void testEmptyFile() {
    CheckVerifier.verifyNoIssue(check, "WordPressImportDetector/empty_file.php");
  }

  @Test
  void testWpContentDirectoryWithWpLoad() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_content_wp_load.php");
  }

  @Test
  void testWpContentDirectoryWithWpConfig() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_content_wp_config.php");
  }

  @Test
  void testWpContentDirectoryWithWpSettings() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_content_wp_settings.php");
  }

  @Test
  void testWpContentDirectoryWithWpBlogHeader() {
    CheckVerifier.verify(check, "WordPressImportDetector/wp_content_wp_blog_header.php");
  }

  static class TestWordPressImportDetectorCheck extends org.sonar.plugins.php.api.visitors.PHPVisitorCheck {

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (WordPressImportDetector.isWordPressImport(tree)) {
        String fileType = WordPressImportDetector.getWordPressFileType(tree);
        newIssue(tree, "WordPress import detected: " + fileType);
      }
      super.visitFunctionCall(tree);
    }
  }
}
