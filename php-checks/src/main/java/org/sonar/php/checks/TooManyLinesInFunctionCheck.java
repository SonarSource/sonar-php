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

import com.google.common.collect.ImmutableList;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.FunctionUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = TooManyLinesInFunctionCheck.KEY,
  name = "Functions should not have too many lines",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("20min")
public class TooManyLinesInFunctionCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S138";

  private static final String MESSAGE = "This function %s has %s lines, which is greater than the %s lines authorized. Split it into smaller functions.";

  private static final int DEFAULT = 150;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(
      Kind.METHOD_DECLARATION,
      Kind.FUNCTION_DECLARATION,
      Kind.FUNCTION_EXPRESSION);
  }

  @Override
  public void visitNode(Tree tree) {
    FunctionTree declaration = (FunctionTree) tree;
    int nbLines = getNumberOfLines(declaration);

    if (nbLines > max) {
      context()
        .newIssue(KEY, String.format(MESSAGE, FunctionUtils.getFunctionName(declaration), nbLines, max))
        .tree(declaration);
    }
  }

  public static int getNumberOfLines(FunctionTree declaration) {
    Tree body = declaration.body();
    if (!body.is(Kind.BLOCK)) {
      return 0;
    }

    BlockTree block = (BlockTree) body;
    int firstLine = block.openCurlyBraceToken().line();
    int lastLine = block.closeCurlyBraceToken().line();
    return lastLine - firstLine + 1;
  }

}
