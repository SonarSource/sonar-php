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
package org.sonar.php.checks;

import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = FunctionNameCheck.KEY)
public class FunctionNameCheck extends PHPVisitorCheck {

  public static final String KEY = "S100";

  private static final String MESSAGE = "Rename function \"%s\" to match the regular expression %s.";

  private static final Set<String> MAGIC_METHODS = Set.of(
    "__construct", "__destruct", "__call", "__callStatic", "__get",
    "__set", "__isset", "__unset", "__sleep", "__wakeup", "__toString", "__invoke",
    "__set_state", "__clone", "__debugInfo");
  public static final String DEFAULT = "^[a-z][a-zA-Z0-9]*$";
  public static final String DEFAULT_DRUPAL = "^[a-z][a-z0-9_]*$";
  private static final Pattern PATTERN_DRUPAL = Pattern.compile(DEFAULT_DRUPAL);
  private static final String DEFAULT_WORDPRESS = "^[a-z][a-z0-9_]*$";
  private static final Pattern PATTERN_WORDPRESS = Pattern.compile(DEFAULT_WORDPRESS);
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  public String format = DEFAULT;

  @Override
  public void init() {
    pattern = Pattern.compile(format);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (Symbols.get(tree).isOverriding().isFalse()) {
      check(tree.name());
    }
    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    check(tree.name());
    super.visitFunctionDeclaration(tree);
  }

  private void check(NameIdentifierTree name) {
    String functionName = name.text();
    if (!getPattern().matcher(functionName).matches() && !MAGIC_METHODS.contains(functionName)) {
      context().newIssue(this, name, String.format(MESSAGE, functionName, getFormat()));
    }
  }

  private Pattern getPattern() {
    if (context().isFramework(SymbolTable.Framework.DRUPAL)) {
      return PATTERN_DRUPAL;
    } else if (context().isFramework(SymbolTable.Framework.WORDPRESS)) {
      return PATTERN_WORDPRESS;
    }
    return pattern;
  }

  private String getFormat() {
    if (context().isFramework(SymbolTable.Framework.DRUPAL)) {
      return DEFAULT_DRUPAL;
    } else if (context().isFramework(SymbolTable.Framework.WORDPRESS)) {
      return DEFAULT_WORDPRESS;
    }
    return DEFAULT;
  }
}
