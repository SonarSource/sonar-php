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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.SyntacticEquivalence.areSyntacticallyEquivalent;

@Rule(key = "S4144")
public class DuplicatedMethodCheck extends PHPVisitorCheck {

  private static final String ISSUE_MSG = "Update this method so that its implementation is not identical to \"%s\" on line %d.";
  private static final Function<FunctionTree, NameIdentifierTree> METHOD_TO_NAME = f -> ((MethodDeclarationTree) f).name();
  private static final Function<FunctionTree, NameIdentifierTree> FUNCTION_TO_NAME = f -> ((FunctionDeclarationTree) f).name();
  private final Deque<List<MethodDeclarationTree>> methods = new LinkedList<>();
  private List<FunctionDeclarationTree> functions = new ArrayList<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    functions.clear();
    methods.clear();
    super.visitCompilationUnit(tree);
    checkDuplications(functions, FUNCTION_TO_NAME);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    // Ignore empty functions
    if (!tree.body().statements().isEmpty()) {
      functions.add(tree);
    }
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    // Ignore abstract and empty methods
    if (isDuplicateCandidate(tree)) {
      methods.peek().add(tree);
    }
    super.visitMethodDeclaration(tree);
  }

  private static boolean isDuplicateCandidate(MethodDeclarationTree tree) {
    return tree.body().is(Tree.Kind.BLOCK) && (((BlockTree) tree.body()).statements().size() >=2 || isAccessor(tree));
  }

  private static boolean isAccessor(MethodDeclarationTree tree) {
    String methodName = tree.name().text();
    return ((BlockTree) tree.body()).statements().size() == 1
      && (methodName.startsWith("set") || methodName.startsWith("get") || methodName.startsWith("is"));
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    methods.push(new ArrayList<>());
    super.visitClassDeclaration(tree);
    checkDuplications(methods.pop(), METHOD_TO_NAME);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    methods.push(new ArrayList<>());
    super.visitAnonymousClass(tree);
    checkDuplications(methods.pop(), METHOD_TO_NAME);
  }

  private void checkDuplications(List<? extends FunctionTree> functionDeclarations, Function<FunctionTree, NameIdentifierTree> toName) {
    Set<? super FunctionTree> reported = new HashSet<>();
    for (int i = 0; i < functionDeclarations.size(); i++) {
      FunctionTree func = functionDeclarations.get(i);
      SyntaxToken methodIdentifier = toName.apply(func).token();
      List<StatementTree> methodBody = ((BlockTree) func.body()).statements();
      functionDeclarations.stream()
        .skip(i + 1L)
        // avoid reporting multiple times
        .filter(m -> !reported.contains(m))
        // only consider method syntactically equivalent
        .filter(m -> areSyntacticallyEquivalent(methodBody.iterator(), ((BlockTree) m.body()).statements().iterator()))
        .forEach(m -> {
          context().newIssue(this,
            toName.apply(m),
            String.format(ISSUE_MSG, methodIdentifier.text(), methodIdentifier.line())).secondary(methodIdentifier, "original implementation");
          reported.add(m);
        });
    }
  }
}
