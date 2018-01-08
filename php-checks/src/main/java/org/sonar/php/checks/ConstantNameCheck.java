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

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
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
    ExpressionTree callee = functionCall.callee();
    if (callee.is(Kind.NAMESPACE_NAME) && "define".equals(((NamespaceNameTree) callee).fullName())) {
      SeparatedList<ExpressionTree> arguments = functionCall.arguments();
      if (!arguments.isEmpty()) {
        ExpressionTree firstArgument = arguments.get(0);
        if (firstArgument.is(Kind.REGULAR_STRING_LITERAL)) {
          String constantName = ((LiteralTree) firstArgument).value();
          checkConstantName(firstArgument, constantName.substring(1, constantName.length() - 1));
        }
      }
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
    if (!pattern.matcher(constName).matches()) {
      context().newIssue(this, tree, String.format(MESSAGE, constName, format));
    }
  }

}
