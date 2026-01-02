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

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
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

  public static final String DEFAULT = "^[a-z][a-zA-Z0-9]*$";
  public static final String FORMAT_DRUPAL_AND_WORDPRESS = "^[a-z][a-z0-9_]*$";
  private static final Pattern PATTERN_DRUPAL_AND_WORDPRESS = Pattern.compile(FORMAT_DRUPAL_AND_WORDPRESS);

  private Pattern pattern = null;
  boolean wasDefaultOverridden = false;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  public String format = DEFAULT;

  @Override
  public void init() {
    pattern = Pattern.compile(format);
    wasDefaultOverridden = !format.equals(DEFAULT);
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

    if (!getPattern().matcher(functionName).matches() && !CheckUtils.MAGIC_METHODS.contains(functionName)) {
      context().newIssue(this, name, String.format(MESSAGE, functionName, getFormat()));
    }
  }

  private Pattern getPattern() {
    if (isFrameworkDrupalOrWordpress() && !wasDefaultOverridden) {
      return PATTERN_DRUPAL_AND_WORDPRESS;
    }
    return pattern;
  }

  private String getFormat() {
    if (isFrameworkDrupalOrWordpress() && !wasDefaultOverridden) {
      return FORMAT_DRUPAL_AND_WORDPRESS;
    }
    return format;
  }

  private boolean isFrameworkDrupalOrWordpress() {
    return context().isFramework(SymbolTable.Framework.DRUPAL) || context().isFramework((SymbolTable.Framework.WORDPRESS));
  }
}
