/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.isPublic;
import static org.sonar.php.checks.utils.CheckUtils.isStatic;
import static org.sonar.php.checks.utils.SyntacticEquivalence.areSyntacticallyEquivalent;

@Rule(key = "S4144")
public class DuplicatedMethodCheck extends PHPVisitorCheck {

  private static final String ISSUE_MSG = "Update this method so that its implementation is not identical to \"%s\" on line %d.";
  private static final Function<FunctionTree, NameIdentifierTree> METHOD_TO_NAME = f -> ((MethodDeclarationTree) f)
    .name();
  private static final Function<FunctionTree, NameIdentifierTree> FUNCTION_TO_NAME = f -> ((FunctionDeclarationTree) f)
    .name();
  private static final int MINIMUM_NUMBER_OF_STATEMENTS = 2;
  private static final Set<String> EXCLUDED_MAGIC_CONSTANTS = Set.of("__FUNCTION__", "__LINE__", "__METHOD__");

  private final Deque<List<MethodDeclarationTree>> methods = new LinkedList<>();
  private final Deque<AccessorUtils> accessorUtils = new LinkedList<>();
  private final List<FunctionDeclarationTree> functions = new ArrayList<>();
  private final Deque<Boolean> scopeContainMagicConstantExclusion = new LinkedList<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    functions.clear();
    methods.clear();
    accessorUtils.clear();
    super.visitCompilationUnit(tree);
    checkDuplications(functions, FUNCTION_TO_NAME);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    scopeContainMagicConstantExclusion.addFirst(false);
    super.visitFunctionDeclaration(tree);
    // Ignore functions with exceptions + ensure that the function has the minimum
    // number of statements
    if (Boolean.FALSE.equals(scopeContainMagicConstantExclusion.pop())
      && tree.body().statements().size() >= MINIMUM_NUMBER_OF_STATEMENTS) {
      functions.add(tree);
    }
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    scopeContainMagicConstantExclusion.addFirst(false);
    super.visitMethodDeclaration(tree);
    // Ignore methods with exceptions, as well as abstract and empty methods
    if (Boolean.FALSE.equals(scopeContainMagicConstantExclusion.pop()) && isDuplicateCandidate(tree)) {
      methods.peek().add(tree);
    }
  }

  private boolean isDuplicateCandidate(MethodDeclarationTree tree) {
    return tree.body() instanceof BlockTree body &&
      (body.statements().size() >= MINIMUM_NUMBER_OF_STATEMENTS || accessorUtils.peek().isAccessor(tree));
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    methods.push(new ArrayList<>());
    accessorUtils.push(new AccessorUtils(tree));
    super.visitClassDeclaration(tree);
    accessorUtils.pop();
    checkDuplications(methods.pop(), METHOD_TO_NAME);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    methods.push(new ArrayList<>());
    accessorUtils.push(new AccessorUtils(tree));
    super.visitAnonymousClass(tree);
    accessorUtils.pop();
    checkDuplications(methods.pop(), METHOD_TO_NAME);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (!scopeContainMagicConstantExclusion.isEmpty() && tree.is(Tree.Kind.MAGIC_CONSTANT)
      && EXCLUDED_MAGIC_CONSTANTS.contains(tree.value())) {
      scopeContainMagicConstantExclusion.pop();
      scopeContainMagicConstantExclusion.addFirst(true);
    }
    super.visitLiteral(tree);
  }

  private void checkDuplications(List<? extends FunctionTree> functionDeclarations,
    Function<FunctionTree, NameIdentifierTree> toName) {
    Set<? super FunctionTree> reported = new HashSet<>();
    for (int i = 0; i < functionDeclarations.size(); i++) {
      FunctionTree func = functionDeclarations.get(i);
      SyntaxToken methodIdentifier = toName.apply(func).token();
      List<StatementTree> methodBody = ((BlockTree) func.body()).statements();
      functionDeclarations.stream()
        .skip(i + 1L)
        // avoid reporting multiple times
        .filter(m -> !reported.contains(m))
        // avoid methods with different parameters and/or return type
        .filter(m -> haveSameParametersAndReturnType(func, m))
        // only consider method syntactically equivalent
        .filter(
          m -> areSyntacticallyEquivalent(methodBody.iterator(), ((BlockTree) m.body()).statements().iterator()))
        .forEach(m -> {
          context()
            .newIssue(this,
              toName.apply(m),
              String.format(ISSUE_MSG, methodIdentifier.text(), methodIdentifier.line()))
            .secondary(methodIdentifier, "original implementation");
          reported.add(m);
        });
    }
  }

  private static boolean haveSameParametersAndReturnType(FunctionTree f1, FunctionTree f2) {
    return f1.parameters().parameters().size() == f2.parameters().parameters().size() &&
      areSyntacticallyEquivalent(declaredTypeTreeOrNull(f1), declaredTypeTreeOrNull(f2));
  }

  private static DeclaredTypeTree declaredTypeTreeOrNull(FunctionTree functionTree) {
    return Optional.ofNullable(functionTree.returnTypeClause()).map(ReturnTypeClauseTree::declaredType).orElse(null);
  }

  static final class AccessorUtils {

    private final Set<String> nonPublicInstanceProperties;

    AccessorUtils(ClassTree tree) {
      this.nonPublicInstanceProperties = nonPublicInstanceProperties(tree);
    }

    boolean isAccessor(MethodDeclarationTree tree) {
      if (!(tree.body() instanceof BlockTree body)) {
        return false;
      }
      List<StatementTree> statements = body.statements();
      if (statements.size() != 1 || !isPublic(tree) || isStatic(tree)) {
        return false;
      }
      return isGetter(tree, statements.get(0)) || isSetter(tree, statements.get(0));
    }

    private static Set<String> nonPublicInstanceProperties(ClassTree tree) {
      UnaryOperator<String> withoutDollar = name -> name.substring(1);
      Set<String> properties = new HashSet<>();
      for (ClassMemberTree member : tree.members()) {
        if (member instanceof ClassPropertyDeclarationTree property && !isPublic(property) && !isStatic(property)) {
          property.declarations()
            .forEach(declaration -> properties.add(withoutDollar.apply(declaration.identifier().text())));
        } else if (member instanceof MethodDeclarationTree method && "__construct".equalsIgnoreCase(method.name().text())) {
          method.parameters().parameters().stream()
            .filter(ParameterTree::isPropertyPromotion)
            .filter(parameter -> parameter.visibility() != null &&
              ("private".equalsIgnoreCase(parameter.visibility().text())
                || "protected".equalsIgnoreCase(parameter.visibility().text())))
            .forEach(parameter -> properties.add(withoutDollar.apply(parameter.variableIdentifier().text())));
        }
      }
      return properties;
    }

    private boolean isGetter(MethodDeclarationTree tree, StatementTree statement) {
      String methodName = tree.name().text();
      return (methodName.startsWith("get") || (methodName.startsWith("is") && hasCompatibleReturnType(tree, "bool")))
        && tree.parameters().parameters().isEmpty() && statement instanceof ReturnStatementTree returnStatement &&
        isDeclaredNonPublicInstanceProperty(returnStatement.expression());
    }

    private boolean isSetter(MethodDeclarationTree tree, StatementTree statement) {
      List<ParameterTree> parameters = tree.parameters().parameters();
      if (!tree.name().text().startsWith("set") || parameters.size() != 1 || !hasCompatibleReturnType(tree, "void") ||
        !(statement instanceof ExpressionStatementTree expressionStatement)) {
        return false;
      }
      ExpressionTree expression = expressionStatement.expression();
      if (!(expression instanceof AssignmentExpressionTree assignment) || !assignment.is(Tree.Kind.ASSIGNMENT)) {
        return false;
      }
      return isDeclaredNonPublicInstanceProperty(assignment.variable()) &&
        assignment.value() instanceof VariableIdentifierTree assignedValue &&
        assignedValue.text().equals(parameters.get(0).variableIdentifier().text());
    }

    private boolean isDeclaredNonPublicInstanceProperty(ExpressionTree expression) {
      if (!(expression instanceof MemberAccessTree memberAccess) || !memberAccess.is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
        return false;
      }
      boolean isThis = (memberAccess.object() instanceof VariableIdentifierTree object)
        && "$this".equals(object.text());
      return isThis && memberAccess.member() instanceof NameIdentifierTree memberName
        && nonPublicInstanceProperties.contains(memberName.text());
    }

    private static boolean hasCompatibleReturnType(MethodDeclarationTree tree, String expectedType) {
      ReturnTypeClauseTree returnTypeClause = tree.returnTypeClause();
      return returnTypeClause == null ||
        (returnTypeClause.declaredType() instanceof TypeTree type && expectedType.equalsIgnoreCase(type.typeName().toString()));
    }
  }
}
