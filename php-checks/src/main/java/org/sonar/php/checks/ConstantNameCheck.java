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
package org.sonar.php.checks;

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ConstantNameCheck.KEY)
public class ConstantNameCheck extends PHPVisitorCheck {

  public static final String KEY = "S115";

  private static final String MESSAGE = "Rename this constant \"%s\" to match the regular expression %s.";

  public static final String DEFAULT = "^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$";
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
  public void visitFunctionCall(FunctionCallTree functionCall) {
    if ("define".equals(CheckUtils.getLowerCaseFunctionName(functionCall))) {
      CheckUtils.argumentValue(functionCall, "constant_name", 0)
        .filter(constantName -> constantName.is(Kind.REGULAR_STRING_LITERAL))
        .map(LiteralTree.class::cast)
        .ifPresent(constantName -> checkConstantName(constantName, CheckUtils.trimQuotes(constantName)));
    }
    super.visitFunctionCall(functionCall);
  }

  @Override
  public void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree) {
    if (tree.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)) {
      checkDeclarations(tree.declarations());
    }
    super.visitClassPropertyDeclaration(tree);
  }

  @Override
  public void visitConstDeclaration(ConstantDeclarationTree tree) {
    checkDeclarations(tree.declarations());
    super.visitConstDeclaration(tree);
  }

  private void checkDeclarations(SeparatedList<VariableDeclarationTree> declarations) {
    for (VariableDeclarationTree declaration : declarations) {
      String constantName = declaration.identifier().text();
      checkConstantName(declaration.identifier(), constantName);
    }
  }

  private void checkConstantName(Tree tree, String constName) {
    String constNameWithoutNamespace = constName;
    if (constName.contains("\\")) {
      constNameWithoutNamespace = constName.substring(constName.lastIndexOf("\\") + 1);
    }
    if (!pattern.matcher(constNameWithoutNamespace).matches()) {
      context().newIssue(this, tree, String.format(MESSAGE, constName, format));
    }
  }

}
