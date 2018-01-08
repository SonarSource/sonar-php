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
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = FieldNameCheck.KEY)
public class FieldNameCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S116";

  private static final String MESSAGE = "Rename this field \"%s\" to match the regular expression %s.";

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
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.CLASS_PROPERTY_DECLARATION);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassPropertyDeclarationTree property = (ClassPropertyDeclarationTree) tree;
    for (VariableDeclarationTree variableDeclarationTree : property.declarations()) {
      String propertyName = variableDeclarationTree.identifier().text();
      if (!pattern.matcher(StringUtils.remove(propertyName, "$")).matches()) {
        context().newIssue(this, variableDeclarationTree.identifier(), String.format(MESSAGE, propertyName, format));
      }
    }
  }

}
