/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = FunctionNameCheck.KEY)
public class FunctionNameCheck extends PHPVisitorCheck {

  public static final String KEY = "S100";

  private static final String MESSAGE = "Rename function \"%s\" to match the regular expression %s.";

  private static final ImmutableList<String> MAGIC_METHODS = ImmutableList.of(
    "__construct", "__destruct", "__call", "__callStatic", "__callStatic", "__get",
    "__set", "__isset", "__unset", "__sleep", "__wakeup", "__toString", "__invoke",
    "__set_state", "__clone", "__clone", "__debugInfo");
  public static final String DEFAULT = "^[a-z][a-zA-Z0-9]*$";
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
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!CheckUtils.isOverriding(tree)) {
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
    if (!pattern.matcher(functionName).matches() && !MAGIC_METHODS.contains(functionName)) {
      context().newIssue(this, name, String.format(MESSAGE, functionName, format));
    }
  }

}
