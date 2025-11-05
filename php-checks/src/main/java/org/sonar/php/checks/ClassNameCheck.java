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

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ClassNameCheck.KEY)
public class ClassNameCheck extends PHPVisitorCheck {

  public static final String KEY = "S101";
  private static final String MESSAGE = "Rename class \"%s\" to match the regular expression %s.";

  public static final String DEFAULT = "^[A-Z][a-zA-Z0-9]*$";
  public static final String YII = "^[a-z0-9_]+$";
  public static final Pattern YII_PATTERN = Pattern.compile(YII);
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  String format = DEFAULT;

  @Override
  public void init() {
    pattern = Pattern.compile(format);
  }

  @Override
  public void visitScript(ScriptTree tree) {
    if (frameworkYiiUsed()) {
      format = YII;
      pattern = YII_PATTERN;
    }

    super.visitScript(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    NameIdentifierTree nameTree = tree.name();
    String className = nameTree.text();

    if (!pattern.matcher(className).matches()) {
      String message = String.format(MESSAGE, className, format);
      context().newIssue(this, nameTree, message);
    }
  }

  private boolean frameworkYiiUsed() {
    return context().getFramework() == SymbolTable.Framework.YII;
  }

}
