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
package org.sonar.php.checks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = RequireIncludeInstructionsUsageCheck.KEY)
public class RequireIncludeInstructionsUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S4833";
  private static final String MESSAGE = "Replace \"%s\" with namespace import mechanism through the \"use\" keyword.";

  private static final Set<String> EXCLUDED_FILES = Set.of("autoload.php", "ScriptHandler.php");
  // Mirrors Php.DEFAULT_FILE_SUFFIXES from sonar-php-plugin (inlined to avoid cross-module dependency)
  private static final Set<String> PHP_FILE_SUFFIXES = Set.of("php", "php3", "php4", "php5", "phtml", "inc");
  private static final Set<String> WRONG_FUNCTIONS = Set.of(
    "require",
    "include",
    "require_once",
    "include_once");

  @Nullable
  private Set<String> filesWithNamespacedSymbols = null;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    if (filesWithNamespacedSymbols == null) {
      filesWithNamespacedSymbols = buildFilesWithNamespacedSymbols();
    }
    if (!isExcludedFile()) {
      super.visitCompilationUnit(tree);
    }
  }

  private Set<String> buildFilesWithNamespacedSymbols() {
    SymbolTable symbolTable = context().symbolTable();
    if (!(symbolTable instanceof SymbolTableImpl symbolTableImpl)) {
      return Set.of();
    }
    return symbolTableImpl.projectSymbolData().classSymbolsByQualifiedName().values().stream()
      .map(data -> data.location().filePath())
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    String callee = tree.callee().toString();
    if (!isFrameworkWithBuiltInRequires()
      && WRONG_FUNCTIONS.contains(callee.toLowerCase(Locale.ENGLISH))
      && !isAutoloadImport(tree)
      && !isReturnValueUsed(tree)
      && !isNonPhpFileInclude(tree)
      && includedFileHasNamespacedSymbols(tree)) {
      String message = String.format(MESSAGE, callee);
      context().newIssue(this, tree.callee(), message);
    }

  }

  private boolean isExcludedFile() {
    String filename = context().getPhpFile().filename();
    return EXCLUDED_FILES.contains(filename);
  }

  private static boolean isAutoloadImport(FunctionCallTree tree) {
    String call = tree.toString();
    return (call.startsWith("include") || call.startsWith("require")) && call.endsWith("autoload.php'");
  }

  private boolean isFrameworkWithBuiltInRequires() {
    SymbolTable.Framework framework = context().getFramework();
    return framework == SymbolTable.Framework.LARAVEL
      || framework == SymbolTable.Framework.WORDPRESS;
  }

  private static boolean isReturnValueUsed(FunctionCallTree tree) {
    Tree parent = tree.getParent();
    return parent != null && !parent.is(Tree.Kind.EXPRESSION_STATEMENT);
  }

  private static boolean isNonPhpFileInclude(FunctionCallTree tree) {
    if (tree.callArguments().isEmpty()) {
      return false;
    }
    ExpressionTree arg = tree.callArguments().get(0).value();
    if (!arg.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return false;
    }
    String path = CheckUtils.trimQuotes((LiteralTree) arg).toLowerCase(Locale.ENGLISH);
    int dotIndex = path.lastIndexOf('.');
    if (dotIndex == -1) {
      return false;
    }
    String extension = path.substring(dotIndex + 1);
    return !PHP_FILE_SUFFIXES.contains(extension);
  }

  /**
   * Returns true if the included file is known to contain at least one namespaced class, meaning
   * the include/require could be replaced by a {@code use} import.
   * Returns false when the path is dynamic (unresolvable) or when the file is not known to contain
   * any namespaced class — in which case flagging would be a false positive.
   */
  private boolean includedFileHasNamespacedSymbols(FunctionCallTree tree) {
    if (tree.callArguments().isEmpty()) {
      return false;
    }
    ExpressionTree arg = CheckUtils.skipParenthesis(tree.callArguments().get(0).value());
    if (!arg.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return false;
    }
    String includedPath = CheckUtils.trimQuotes((LiteralTree) arg);
    Path currentFilePath = Paths.get(context().getPhpFile().uri());
    Path resolvedPath = currentFilePath.getParent().resolve(includedPath).normalize();
    return filesWithNamespacedSymbols.contains(resolvedPath.toString());
  }
}
