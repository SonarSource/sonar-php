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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.ArrayList;
import java.util.List;

@Rule(
  key = ArgumentWithDefaultValueNotLastCheck.KEY,
  name = "Method arguments with default value should be last",
  priority = Priority.CRITICAL,
  tags = {Tags.BUG, Tags.PSR2})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INSTRUCTION_RELIABILITY)
@SqaleConstantRemediation("20min")
public class ArgumentWithDefaultValueNotLastCheck extends PHPVisitorCheck {

  public static final String KEY = "S1788";

  private static final String MESSAGE = "Move arguments %s after arguments without default value";

  @Override
  public void visitParameterList(ParameterListTree parameterList) {
    List<ParameterTree> parametersToMove = getParametersToMove(parameterList);
    if (!parametersToMove.isEmpty()) {
      context().newIssue(KEY, String.format(MESSAGE, getNameListString(parametersToMove))).tree(parameterList);
    }
    super.visitParameterList(parameterList);
  }

  /**
   * <p>Return list of parameter nodes that are not declared at the end.</p>
   * <p/>
   * Example: $p2 will be returned.
   * <pre>function f($p1, $p2 = 1, $p3, $p4 = 4) {...}</pre>
   */
  private List<ParameterTree> getParametersToMove(ParameterListTree parameterList) {
    List<ParameterTree> parametersToMove = Lists.newArrayList();
    boolean metParamWithoutDefault = false;

    for (ParameterTree param : Lists.reverse(parameterList.parameters())) {
      boolean hasDefault = param.initValue() != null;

      if (!hasDefault && !metParamWithoutDefault) {
        metParamWithoutDefault = true;
      } else if (hasDefault && metParamWithoutDefault) {
        parametersToMove.add(param);
      }
    }

    return Lists.reverse(parametersToMove);
  }

  private String getNameListString(List<ParameterTree> params) {
    List<String> parameterNames = new ArrayList<>();
    for (ParameterTree parameter : params) {
      parameterNames.add("\"" + parameter.variableIdentifier().variableExpression().text() + "\"");
    }
    return Joiner.on(", ").join(parameterNames);
  }

}
