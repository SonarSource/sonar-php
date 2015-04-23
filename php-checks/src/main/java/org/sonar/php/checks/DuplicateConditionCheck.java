/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.ast.AstSelect;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;
import java.util.Set;

@Rule(
  key = "S1862",
  name = "Related \"if/else if\" statements and \"cases\" in a \"switch\" should not have the same condition",
  tags = {Tags.BUG, Tags.CERT, Tags.PITFALL, Tags.UNUSED},
  priority = Priority.CRITICAL)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("10min")
public class DuplicateConditionCheck extends SquidCheck<LexerlessGrammar> {

  private final Set<AstNode> checkedIfStatements = Sets.newHashSet();

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
    AstNode ifNode = node;
    List<AstNode> conditions = Lists.newArrayList();
    while (ifNode != null) {
      checkedIfStatements.add(ifNode);
      conditions.add(ifNode.getFirstChild(PHPGrammar.PARENTHESIS_EXPRESSION));
      AstSelect elseIfConditions = ifNode.select()
        .children(PHPGrammar.ELSEIF_LIST)
        .children(PHPGrammar.ELSEIF_CLAUSE)
        .children(PHPGrammar.PARENTHESIS_EXPRESSION);
      for (AstNode condition : elseIfConditions) {
        conditions.add(condition);
      }
      AstSelect elseIfs = ifNode.select()
        .children(PHPGrammar.ELSE_CLAUSE)
        .children(PHPGrammar.STATEMENT)
        .children(PHPGrammar.IF_STATEMENT);
      ifNode = elseIfs.isEmpty() ? null : elseIfs.get(0);
    }
    checkConditions(conditions, "branch");
  }

  private void visitCaseListNode(AstNode node) {
    AstSelect conditions = node.select()
      .children(PHPGrammar.CASE_CLAUSE)
      .children(PHPGrammar.EXPRESSION);
    checkConditions(ImmutableList.copyOf(conditions), "case");
  }

  private void checkConditions(List<AstNode> conditions, String type) {
    int nbConditions = conditions.size();
    for (int i = 1; i < nbConditions; i++) {
      for (int j = 0; j < i; j++) {
        if (CheckUtils.areSyntacticallyEquivalent(conditions.get(i), conditions.get(j))) {
          String message = "This {0} duplicates the one on line {1}.";
          getContext().createLineViolation(this, message, conditions.get(i), type, conditions.get(j).getTokenLine());
          break;
        }
      }
    }
  }
}
