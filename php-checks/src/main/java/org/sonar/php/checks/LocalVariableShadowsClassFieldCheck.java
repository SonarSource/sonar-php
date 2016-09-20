/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;

@Rule(
  key = LocalVariableShadowsClassFieldCheck.KEY,
  name = "Local variables should not have the same name as class fields",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@SqaleConstantRemediation("5min")
public class LocalVariableShadowsClassFieldCheck extends PHPVisitorCheck {

  public static final String KEY = "S1117";
  private static final String MESSAGE = "Rename \"%s\" which has the same name as the field declared at line %s.";

  private ClassState classState = new ClassState();
  private Deque<FunctionTree> functions = new ArrayDeque<>();

  private static class ClassState {

    private Map<String, Integer> classFields = Maps.newHashMap();
    private Deque<Set<String>> checkedVariables = new ArrayDeque<>();
    private String className;

    public void clear() {
      classFields.clear();
      checkedVariables.clear();
    }

    public void setClassName(ClassDeclarationTree classDeclaration) {
      className = classDeclaration.name().text();
    }

    public boolean isInClass() {
      return !classFields.isEmpty();
    }

    public void declareField(SyntaxToken fieldToken) {
      classFields.put(fieldToken.text(), fieldToken.line());
    }

    public boolean hasFieldNamed(String paramName) {
      return classFields.containsKey(paramName);
    }

    public int getLineOfFieldNamed(String name) {
      return classFields.get(name);
    }

    public void setAsCheckedVariable(String varName) {
      checkedVariables.peek().add(varName);
    }

    public boolean hasAlreadyBeenChecked(String varName) {
      return checkedVariables.peek().contains(varName);
    }

    public void newFunctionScope() {
      checkedVariables.push(new HashSet<String>());
    }

    public void leaveFunctionScope() {
      checkedVariables.pop();
    }
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    classState.clear();
    functions.clear();
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    for (ClassMemberTree classMemberTree : tree.members()) {
      if (classMemberTree.is(Kind.CLASS_PROPERTY_DECLARATION)) {
        for (VariableDeclarationTree declaration : ((ClassPropertyDeclarationTree) classMemberTree).declarations()) {
          classState.declareField(declaration.identifier().token());
        }
      }
    }

    classState.setClassName(tree);
    super.visitClassDeclaration(tree);
    classState.clear();
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isExcluded(tree)) {
      classState.newFunctionScope();
      functions.push(tree);
      super.visitMethodDeclaration(tree);
      functions.pop();
      classState.leaveFunctionScope();
    }
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    if (classState.isInClass()) {
      classState.newFunctionScope();
      functions.push(tree);
      super.visitFunctionExpression(tree);
      functions.pop();
      classState.leaveFunctionScope();
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (classState.isInClass()) {
      checkLocalVariable(tree.variable());
      super.visitAssignmentExpression(tree);
    }
  }

  private void checkLocalVariable(ExpressionTree assignedExpression) {
    String varName = assignedExpression.toString();

    if (isLocalVar(varName) && classState.hasFieldNamed(varName) && !classState.hasAlreadyBeenChecked(varName)) {
      reportIssue(assignedExpression, varName);
    }
  }

  private boolean isLocalVar(String varName) {
    if (functions.isEmpty()) {
      return false;
    }
    Scope scope = context().symbolTable().getScopeFor(functions.peek());
    return scope.getSymbol(varName, Symbol.Kind.VARIABLE) != null;
  }

  private boolean isExcluded(MethodDeclarationTree methodDec) {
    String methodName = methodDec.name().text();
    return CheckUtils.hasModifier(methodDec.modifiers(), PHPKeyword.STATIC.getValue())
      || isConstructor(methodName) || isSetter(methodName);
  }

  private static boolean isSetter(String methodName) {
    return methodName.startsWith("set");
  }

  private boolean isConstructor(String methodName) {
    return classState.className.equalsIgnoreCase(methodName) || "__construct".equalsIgnoreCase(methodName);
  }

  private void reportIssue(Tree tree, String varName) {
    String message = String.format(MESSAGE, varName, classState.getLineOfFieldNamed(varName));
    context().newIssue(this, message).tree(tree);

    classState.setAsCheckedVariable(varName);
  }
}
