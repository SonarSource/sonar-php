/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ElseIfWithoutElseCheck.KEY)
public class ElseIfWithoutElseCheck extends PHPVisitorCheck {

  public static final String KEY = "S126";
  private static final String MESSAGE = "Add the missing \"else\" clause.";

  // Store if-else constructs which can be seen as an exit statement
  private final Set<Tree> exclusivelyExitBodies = new HashSet<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    super.visitCompilationUnit(tree);
    exclusivelyExitBodies.clear();
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    super.visitIfStatement(tree);

    List<ElseifClauseTree> elseifClauses = tree.elseifClauses();
    // Add all if body statements to work list
    List<StatementTree> conditionBodies = new ArrayList<>(tree.statements());

    if (tree.elseClause() == null && !elseifClauses.isEmpty()) {
      // Add all statements of each elseif body to work list
      elseifClauses.forEach(c -> conditionBodies.addAll(c.statements()));
      if (!hasExclusivelyExitBodies(conditionBodies)) {
        ElseifClauseTree lastElseIf = elseifClauses.get(elseifClauses.size() - 1);
        context().newIssue(this, lastElseIf.elseifToken(), MESSAGE);
      }
    }
  }

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    super.visitElseClause(tree);

    IfStatementTree parentIf = (IfStatementTree) tree.getParent();
    if (tree.statements().get(0).is(Kind.IF_STATEMENT)) {
      IfStatementTree nestedIf = (IfStatementTree) tree.statements().get(0);
      if (nestedIf.elseClause() == null && nestedIf.elseifClauses().isEmpty()) {
        List<StatementTree> bodyStatements = new ArrayList<>(parentIf.statements());
        bodyStatements.addAll(nestedIf.statements());
        if (!hasExclusivelyExitBodies(bodyStatements)) {
          context().newIssue(this, tree.elseToken(), nestedIf.ifToken(), MESSAGE);
        }
      }
    } else {
      // If-else constructs which exit in both branches can be seen as exit statement
      List<StatementTree> bodyStatements = new ArrayList<>(tree.statements());
      bodyStatements.addAll(parentIf.statements());
      if (hasExclusivelyExitBodies(bodyStatements)) {
        exclusivelyExitBodies.add(parentIf);
      }
    }
  }

  private boolean hasExclusivelyExitBodies(List<StatementTree> statements) {
    return !statements.isEmpty() && statements.stream().allMatch(s -> {
      ConditionBodyVisitor conditionBody = new ConditionBodyVisitor();
      s.accept(conditionBody);
      return conditionBody.hasExitStatement;
    });
  }

  private class ConditionBodyVisitor extends PHPVisitorCheck {

    boolean hasExitStatement = false;

    @Override
    public void visitReturnStatement(ReturnStatementTree tree) {
      hasExitStatement = true;
    }

    @Override
    public void visitBreakStatement(BreakStatementTree tree) {
      hasExitStatement = true;
    }

    @Override
    public void visitContinueStatement(ContinueStatementTree tree) {
      hasExitStatement = true;
    }

    @Override
    public void visitThrowStatement(ThrowStatementTree tree) {
      hasExitStatement = true;
    }

    @Override
    public void visitIfStatement(IfStatementTree tree) {
      if (exclusivelyExitBodies.contains(tree)) {
        hasExitStatement = true;
      }
    }
  }
}
