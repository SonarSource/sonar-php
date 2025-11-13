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
package org.sonar.php.tree.visitors.frameworkDetectors;

import java.net.URISyntaxException;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.tree.visitors.WordPressImportDetector;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

class WordPressImportDetectorTest {

  WordPressImportDetectorTest() throws URISyntaxException {
  }

  @Test
  void testWpLoad() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-load.php';"))).isTrue();
  }

  @Test
  void testWpConfig() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-config.php';"))).isTrue();
  }

  @Test
  void testWpSettings() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-settings.php';"))).isTrue();
  }

  @Test
  void testWpBlogHeader() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-blog-header.php';"))).isTrue();
  }

  @Test
  void testWithParentheses() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once( ABSPATH . 'wp-load.php' );"))).isTrue();
  }

  @Test
  void testWithConcatenation() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once( 'wp-load.php' );"))).isTrue();
  }

  @Test
  void testWithConcatenationAndParentheses() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once ABSPATH . 'wp-settings.php';"))).isTrue();
  }

  @Test
  void testInclude() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("include 'wp-load.php';"))).isTrue();
  }

  @Test
  void testIncludeOnce() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("include_once 'wp-load.php';"))).isTrue();
  }

  @Test
  void testRequire() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require 'wp-load.php';"))).isTrue();
  }

  @Test
  void testDoubleQuotes() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once \"wp-load.php\";"))).isTrue();
  }

  @Test
  void testWpIncludesPath() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-includes/wp-load.php';"))).isTrue();
  }

  @Test
  void testWpIncludesFunctions() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-includes/functions.php';"))).isTrue();
  }

  @Test
  void testWpAdminPath() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-admin/admin.php';"))).isTrue();
  }

  @Test
  void testAbsolutePath() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once '/var/www/html/wp-load.php';"))).isTrue();
  }

  @Test
  void testRelativePath() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once '../wp-load.php';"))).isTrue();
  }

  @Test
  void testDirConcatenation() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once __DIR__ . '/wp-load.php';"))).isTrue();
  }

  @Test
  void testFullPathConcatenation() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once '/path/to/' . 'wp-load.php';"))).isTrue();
  }

  @Test
  void testNonWordPressFile() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'config.php';"))).isFalse();
  }

  @Test
  void testOtherFunction() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("echo 'test';"))).isFalse();
  }

  @Test
  void testEmptyFilePath() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once '';"))).isFalse();
  }

  @Test
  void testWpIncludesLoad() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-includes/load.php';"))).isTrue();
  }

  @Test
  void testWpIncludesPlugin() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-includes/plugin.php';"))).isTrue();
  }

  @Test
  void testWpAdminIncludesAdmin() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'wp-admin/includes/admin.php'; "))).isTrue();
  }

  @Test
  void testCaseInsensitive() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once 'WP-LOAD.PHP';"))).isTrue();
  }

  @Test
  void testNestedConcatenationWithLeftOperand() {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement("require_once '/wordpress/' . ('wp-load' . ('.php'));"))).isTrue();
  }

  FunctionCallTree getImportStatement(String code) {
    CompilationUnitTree tree = ParsingTestUtils.parseSource("<?php " + code);
    ScriptTree treeScript = Objects.requireNonNull(tree.script());
    ExpressionStatementTree expressionStatementTree = (ExpressionStatementTree) treeScript.statements().get(0);
    return (FunctionCallTree) expressionStatementTree.expression();
  }
}
