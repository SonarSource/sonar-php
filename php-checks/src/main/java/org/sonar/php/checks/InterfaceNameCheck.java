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
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = InterfaceNameCheck.KEY)
public class InterfaceNameCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S114";

  private static final String MESSAGE = "Rename this interface name to match the regular expression %s.";

  public static final String DEFAULT_FORMAT = "^[A-Z][a-zA-Z0-9]*$";

  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the interface names against.",
    defaultValue = DEFAULT_FORMAT)
  public String format = DEFAULT_FORMAT;

  @Override
  public void init() {
    pattern = Pattern.compile(format);
  }

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.INTERFACE_DECLARATION);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassDeclarationTree declaration = (ClassDeclarationTree) tree;
    String name = declaration.name().text();
    if (!pattern.matcher(name).matches()) {
      context().newIssue(this, declaration.name(), String.format(MESSAGE, format));
    }
  }

}
