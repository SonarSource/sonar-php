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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;

/**
 * Utility class for detecting WordPress import/loading patterns in PHP files.
 * Detects:
 * - require/require_once/include/include_once statements
 * - All WordPress bootstrap files (wp-load.php, wp-config.php, wp-settings.php, etc.)
 * - Path variations (absolute, relative, concatenated, variable-based)
 */
public class WordPressImportDetector {

  // Utility class to comply w rule S1118
  private WordPressImportDetector() {
  }

  // WordPress bootstrap files to detect
  private static final Set<String> WORDPRESS_FILES = new HashSet<>(Arrays.asList(
    "wp-load.php",
    "wp-config.php",
    "wp-settings.php",
    "wp-blog-header.php",
    "wp-includes/wp-load.php",
    "wp-includes/wp-config.php",
    "wp-includes/functions.php",
    "wp-includes/load.php",
    "wp-includes/plugin.php",
    "wp-admin/admin.php",
    "wp-admin/includes/admin.php"));

  // PHP include/require functions to check
  private static final Set<String> INCLUDE_FUNCTIONS = new HashSet<>(Arrays.asList(
    "require",
    "require_once",
    "include",
    "include_once"));

  // Checks if a function call is a WordPress import statement
  public static boolean isWordPressImport(FunctionCallTree tree) {
    String functionName = CheckUtils.functionName(tree);

    if (functionName == null || !isIncludeFunction(functionName)) {
      return false;
    }

    Optional<String> filePath = extractFilePath(tree);
    return filePath.isPresent() && isWordPressFile(filePath.get());
  }

  // Extracts the WordPress file type from a function call
  public static String getWordPressFileType(FunctionCallTree tree) {
    Optional<String> filePath = extractFilePath(tree);
    if (filePath.isEmpty()) {
      return null;
    }
    return getWordPressFileType(filePath.get());
  }

  // Gets the WordPress file type from a path
  private static String getWordPressFileType(String path) {

    String lowerPath = path.toLowerCase(Locale.ROOT);

    for (String wpFile : WORDPRESS_FILES) {
      if (lowerPath.endsWith("/" + wpFile) || lowerPath.endsWith("\\" + wpFile) || lowerPath.contains(wpFile)) {
        return wpFile;
      }
    }

    return null;
  }

  // Checks if the function is an include/require function
  private static boolean isIncludeFunction(String functionName) {
    return INCLUDE_FUNCTIONS.contains(functionName.toLowerCase(Locale.ROOT));
  }


  // Extracts the file path from a require/include function call
  private static Optional<String> extractFilePath(FunctionCallTree tree) {
    return CheckUtils.argument(tree, "", 0)
      .map(CallArgumentTree::value)
      .map(WordPressImportDetector::extractPathFromExpression);
  }

  // Extracts path from various expression types
  private static String extractPathFromExpression(ExpressionTree expression) {
    // Handle parentheses: require_once( 'file.php' )
    if (expression.is(Tree.Kind.PARENTHESISED_EXPRESSION)) {
      expression = ((ParenthesisedExpressionTree) expression).expression();
    }

    // Handle concatenation: ABSPATH . 'wp-load.php'
    if (expression.is(Tree.Kind.CONCATENATION)) {
      BinaryExpressionTree concat = (BinaryExpressionTree) expression;
      ExpressionTree right = concat.rightOperand();

      // Check right operand (usually the filename)
      if (right.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
        return CheckUtils.trimQuotes((LiteralTree) right);
      }

      // Also check left operand for full paths
      ExpressionTree left = concat.leftOperand();
      if (left.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
        String leftPath = CheckUtils.trimQuotes((LiteralTree) left);
        String rightPath = extractPathFromExpression(right);
        if (rightPath != null) {
          return leftPath + rightPath;
        }
      }
    }

    // Handle direct string literals: 'wp-load.php' or "wp-load.php"
    if (expression.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return CheckUtils.trimQuotes((LiteralTree) expression);
    }

    return null;
  }

  // Checks if a file path matches a WordPress bootstrap file
  private static boolean isWordPressFile(String path) {
    if (path.isEmpty()) {
      return false;
    }

    String lowerPath = path.toLowerCase(Locale.ROOT);

    // Check exact matches
    for (String wpFile : WORDPRESS_FILES) {
      if (lowerPath.equals(wpFile) || lowerPath.endsWith("/" + wpFile) || lowerPath.endsWith("\\" + wpFile)) {
        return true;
      }
    }

    for (String wpFile : WORDPRESS_FILES) {
      if (lowerPath.contains(wpFile)) {
        return true;
      }
    }

    // Check for WordPress directory patterns
    if (lowerPath.contains("wp-includes/") ||
      lowerPath.contains("wp-admin/") ||
      lowerPath.contains("wp-content/")) {
      // Could be a WordPress file, check more specifically
      return lowerPath.contains("wp-load.php") ||
        lowerPath.contains("wp-config.php") ||
        lowerPath.contains("wp-settings.php") ||
        lowerPath.contains("wp-blog-header.php");
    }

    return false;
  }
}
