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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.tree.visitors.WordPressImportDetector;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

class WordPressImportDetectorTest {

  WordPressImportDetectorTest() throws URISyntaxException {
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "require_once 'wp-load.php';",
    "require_once 'wp-config.php';",
    "require_once 'wp-settings.php';",
    "require_once 'wp-blog-header.php';",
    "require_once( ABSPATH . 'wp-load.php' );",
    "require_once( 'wp-load.php' );",
    "require_once ABSPATH . 'wp-settings.php';",
    "include 'wp-load.php';",
    "include_once 'wp-load.php';",
    "require 'wp-load.php';",
    "require_once \"wp-load.php\";",
    "require_once 'wp-includes/wp-load.php';",
    "require_once 'wp-includes/functions.php';",
    "require_once 'wp-admin/admin.php';",
    "require_once '/var/www/html/wp-load.php';",
    "require_once '../wp-load.php';",
    "require_once __DIR__ . '/wp-load.php';",
    "require_once '/path/to/' . 'wp-load.php';",
    "require_once 'wp-includes/load.php';",
    "require_once 'wp-includes/plugin.php';",
    "require_once 'wp-admin/includes/admin.php'; ",
    "require_once 'WP-LOAD.PHP';",
    "require_once '/wordpress/' . ('wp-load' . ('.php'));"
  })
  void testWordPressImports(String importStatement) {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement(importStatement))).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "require_once 'config.php';",
    "echo 'test';",
    "require_once '';"
  })
  void testNonWordPressImports(String importStatement) {
    Assertions.assertThat(WordPressImportDetector.isWordPressImport(getImportStatement(importStatement))).isFalse();
  }

  FunctionCallTree getImportStatement(String code) {
    CompilationUnitTree tree = ParsingTestUtils.parseSource("<?php " + code);
    ScriptTree treeScript = Objects.requireNonNull(tree.script());
    ExpressionStatementTree expressionStatementTree = (ExpressionStatementTree) treeScript.statements().get(0);
    return (FunctionCallTree) expressionStatementTree.expression();
  }
}
