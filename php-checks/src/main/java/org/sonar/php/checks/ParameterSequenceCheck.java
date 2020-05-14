/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.HashSet;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2234")
public class ParameterSequenceCheck extends PHPVisitorCheck {

  public static final String MESSAGE = "Parameters to \"%s\" have the same names but not the same order as the method arguments.";

  private final List<FunctionCallTree> functionCalls = new ArrayList<>();
  private final Map<String, FunctionDeclarationTree> functionDeclarations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  private final Map<String, Map<String, MethodDeclarationTree>> methodDeclarations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    functionCalls.clear();
    functionDeclarations.clear();

    super.visitCompilationUnit(tree);

    checkFunctionCalls();
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (!tree.arguments().isEmpty()) {
      functionCalls.add(tree);
    }

    super.visitFunctionCall(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!tree.arguments().isEmpty()) {
      functionCalls.add(tree);
    }

    super.visitMethodDeclaration(tree);
  }

  private void addMethod(MethodDeclarationTree tree) {
    ClassDeclarationTree classDeclaration = U
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    if (!tree.parameters().parameters().isEmpty()) {
      functionDeclarations.put(CheckUtils.getFunctionName(tree), tree);
    }

    super.visitFunctionDeclaration(tree);
  }

  private void checkFunctionCalls() {
    for (FunctionCallTree call : functionCalls) {
      if (call.callee().is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
        continue;
      }
      String functionName = CheckUtils.getFunctionName(call);
      if (functionName != null && functionDeclarations.containsKey(functionName)) {
        checkParameterSequence(functionName, call, functionDeclarations.get(functionName));
      }
    }
  }

  private void checkParameterSequence(String functionName, FunctionCallTree call, FunctionDeclarationTree declaration)
  {
    List<String> parameters = declaration.parameters().parameters().stream()
      .map(e -> e.variableIdentifier().text())
      .collect(Collectors.toList());

    List<String> arguments = call.arguments().stream()
      .filter(e -> e.is(Tree.Kind.VARIABLE_IDENTIFIER))
      .map(e -> ((VariableIdentifierTree) e).text())
      .collect(Collectors.toList());

    if (arguments.size() == parameters.size() && !arguments.equals(parameters) && new HashSet<>(parameters).equals(new HashSet<>(arguments))) {
      context().newIssue(this, call, String.format(MESSAGE, functionName));
    }
  }
}
