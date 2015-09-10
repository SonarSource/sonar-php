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
package org.sonar.php.checks.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.ast.AstSelect;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;
import java.util.Set;

public abstract class AbstractDuplicateBranchCheck extends SquidCheck<LexerlessGrammar> {

  private final Set<AstNode> checkedIfStatements = Sets.newHashSet();

  protected abstract AstNodeType ifBranchNodeType();

  protected abstract AstNodeType caseClauseChildType();

  protected abstract void addIssue(String type, AstNode duplicate, AstNode duplicated);

  @Override
  public void leaveFile(AstNode astNode) {
    checkedIfStatements.clear();
  }

  @Override
  public void init() {
    subscribeTo(PHPGrammar.IF_STATEMENT, PHPGrammar.CASE_LIST);
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(PHPGrammar.IF_STATEMENT)) {
      visitIfStatementNode(node);
    } else {
      visitCaseListNode(node);
    }
  }

  private void visitIfStatementNode(AstNode node) {
    if (checkedIfStatements.contains(node)) {
      return;
    }
    List<AstNode> branchNodes = Lists.newArrayList();
    AstNode ifNode = node;
    while (ifNode != null) {
      checkedIfStatements.add(ifNode);
      branchNodes.add(ifNode.getFirstChild(ifBranchNodeType()));
      AstSelect elseIfNodes = ifNode.select()
        .children(PHPGrammar.ELSEIF_LIST)
        .children(PHPGrammar.ELSEIF_CLAUSE)
        .children(ifBranchNodeType());
      for (AstNode condition : elseIfNodes) {
        branchNodes.add(condition);
      }
      AstSelect elseNodes = ifNode.select()
        .children(PHPGrammar.ELSE_CLAUSE);
      for (AstNode elseNode : elseNodes.children(ifBranchNodeType())) {
        branchNodes.add(elseNode);
      }
      AstSelect elseIfs = elseNodes
        .children(PHPGrammar.STATEMENT)
        .children(PHPGrammar.IF_STATEMENT);
      ifNode = elseIfs.isEmpty() ? null : elseIfs.get(0);
    }
    checkBranches(branchNodes, "branch");
  }

  private void visitCaseListNode(AstNode node) {
    AstSelect conditions = node.select()
      .children(PHPGrammar.CASE_CLAUSE)
      .children(caseClauseChildType());
    checkBranches(ImmutableList.copyOf(conditions), "case");
  }

  private void checkBranches(List<AstNode> conditions, String type) {
    int nbConditions = conditions.size();
    for (int i = 1; i < nbConditions; i++) {
      for (int j = 0; j < i; j++) {
        if (CheckUtils.areSyntacticallyEquivalent(conditions.get(i), conditions.get(j))) {
          AstNode duplicate = conditions.get(i);
          AstNode duplicated = conditions.get(j);
          addIssue(type, duplicate, duplicated);
          break;
        }
      }
    }
  }

}
