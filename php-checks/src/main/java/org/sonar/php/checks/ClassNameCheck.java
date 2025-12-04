/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = ClassNameCheck.KEY)
public class ClassNameCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S101";
  private static final String MESSAGE = "Rename class \"%s\" to match the regular expression %s.";

  public static final String DEFAULT = "^[A-Z][a-zA-Z0-9]*$";
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  String format = DEFAULT;

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.CLASS_DECLARATION);
  }

  @Override
  public void init() {
    pattern = Pattern.compile(format);
  }

  @Override
  public void visitNode(Tree tree) {
    NameIdentifierTree nameTree = ((ClassDeclarationTree) tree).name();
    String className = nameTree.text();

    if (!pattern.matcher(className).matches()) {
      String message = String.format(MESSAGE, className, this.format);
      context().newIssue(this, nameTree, message);
    }
  }

}
