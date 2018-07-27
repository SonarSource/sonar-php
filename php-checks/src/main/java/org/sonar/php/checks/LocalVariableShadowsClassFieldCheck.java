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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
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
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = LocalVariableShadowsClassFieldCheck.KEY)
public class LocalVariableShadowsClassFieldCheck extends PHPVisitorCheck {

  public static final String KEY = "S1117";
  private static final String MESSAGE = "Rename \"%s\" which has the same name as the field declared at line %s.";

  private Deque<ClassState> classStates = new ArrayDeque<>();
  private Deque<FunctionTree> functions = new ArrayDeque<>();

  private static class ClassState {

    private Map<String, SyntaxToken> classFields = new HashMap<>();
    private Deque<Set<String>> checkedVariables = new ArrayDeque<>();
    private String className = null;

    public void setClassName(@Nullable String name) {
      className = name;
    }

    public void declareField(SyntaxToken fieldToken) {
      classFields.put(fieldToken.text(), fieldToken);
    }

    public boolean hasFieldNamed(String paramName) {
      return classFields.containsKey(paramName);
    }

    public SyntaxToken getFieldNamed(String name) {
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
    classStates.clear();
    functions.clear();
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    ClassState classState = new ClassState();
    classState.setClassName(tree.name().text());
    classStates.push(classState);
    collectClassData(tree);
    super.visitClassDeclaration(tree);
    classStates.pop();
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    scan(tree.arguments());
    classStates.push(new ClassState());
    collectClassData(tree);

    NamespaceNameTree superClass = tree.superClass();
    if (superClass != null) {
      scan(superClass);
    }
    scan(tree.superInterfaces());
    scan(tree.members());

    classStates.pop();
  }

  private void collectClassData(ClassTree tree) {
    for (ClassMemberTree classMemberTree : tree.members()) {
      if (classMemberTree.is(Kind.CLASS_PROPERTY_DECLARATION)) {
        for (VariableDeclarationTree declaration : ((ClassPropertyDeclarationTree) classMemberTree).declarations()) {
          classStates.peek().declareField(declaration.identifier().token());
        }
      }
    }
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isExcluded(tree)) {
      classStates.peek().newFunctionScope();
      functions.push(tree);
      super.visitMethodDeclaration(tree);
      functions.pop();
      classStates.peek().leaveFunctionScope();
    }
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    if (!classStates.isEmpty()) {
      classStates.peek().newFunctionScope();
      functions.push(tree);
      super.visitFunctionExpression(tree);
      functions.pop();
      classStates.peek().leaveFunctionScope();
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (!classStates.isEmpty()) {
      checkLocalVariable(tree.variable());
    }

    super.visitAssignmentExpression(tree);
  }

  private void checkLocalVariable(ExpressionTree assignedExpression) {
    String varName = assignedExpression.toString();

    if (isLocalVar(varName) && classStates.peek().hasFieldNamed(varName) && !classStates.peek().hasAlreadyBeenChecked(varName)) {
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
    return (classStates.peek().className != null && classStates.peek().className.equalsIgnoreCase(methodName)) || "__construct".equalsIgnoreCase(methodName);
  }

  private void reportIssue(Tree tree, String varName) {
    SyntaxToken field = classStates.peek().getFieldNamed(varName);
    String message = String.format(MESSAGE, varName, field.line());
    context().newIssue(this, tree, message).secondary(field, null);

    classStates.peek().setAsCheckedVariable(varName);
  }
}
