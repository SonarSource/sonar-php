/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.ArrayList;
import java.util.List;

@Rule(
  key = UnusedFunctionParametersCheck.KEY,
  name = "Unused function parameters should be removed",
  priority = Priority.MAJOR,
  tags = {Tags.UNUSED, Tags.MISRA})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class UnusedFunctionParametersCheck extends PHPVisitorCheck {

  public static final String KEY = "S1172";
  private static final String MESSAGE = "Remove the unused function parameter%s \"%s\".";

  private boolean mayOverride = false;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (tree.superClass() != null || tree.implementsToken() != null) {
      mayOverride = true;
    }
    super.visitClassDeclaration(tree);
    mayOverride = false;
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkParameters(tree);
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    checkParameters(tree);
    super.visitFunctionExpression(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isExcluded(tree)) {
      checkParameters(tree);
    }
    super.visitMethodDeclaration(tree);
  }

  private void checkParameters(FunctionTree tree) {
    Scope scope = context().symbolTable().getScopeFor(tree);
    if (scope != null) {
      List<String> unused = new ArrayList<>();

      for (Symbol symbol : scope.getSymbols(Symbol.Kind.PARAMETER)) {
        if (symbol.usages().isEmpty()) {
          unused.add(symbol.name());
        }
      }

      if (!unused.isEmpty()) {
        String message = String.format(MESSAGE, unused.size() == 1 ? "" : "s", StringUtils.join(unused, ", "));
        context().newIssue(KEY, message).tree(tree);
      }
    }
  }

  public boolean isExcluded(MethodDeclarationTree tree) {
    return (mayOverride && !CheckUtils.hasModifier(tree.modifiers(), "private"))
      || !tree.body().is(Tree.Kind.BLOCK)
      || hasInheritdocTag(tree);
  }

  public static boolean hasInheritdocTag(MethodDeclarationTree methodDec) {
    SyntaxToken firstToken = ((PHPTree) methodDec).getFirstToken();
    for (SyntaxTrivia comment : firstToken.trivias()) {
      if (StringUtils.containsIgnoreCase(comment.text(), "@inheritdoc")) {
        return true;
      }
    }
    return false;
  }

}
