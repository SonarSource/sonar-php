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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = AtLeastThreeCasesInSwitchCheck.KEY,
  name = "\"switch\" statements should have at least 3 \"case\" clauses",
  priority = Priority.MINOR,
  tags = {Tags.MISRA})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("5min")
public class AtLeastThreeCasesInSwitchCheck extends PHPVisitorCheck {

  public static final String KEY = "S1301";

  @Override
  public void visitSwitchStatement(SwitchStatementTree switchTree) {
    if (switchTree.cases().size() < 3) {
      context()
        .newIssue(KEY, "Replace this \"switch\" statement with \"if\" statements to increase readability.")
        .tree(switchTree);
    }

    super.visitSwitchStatement(switchTree);
  }
}
