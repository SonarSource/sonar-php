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
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.utils.collections.ListUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ArgumentWithDefaultValueNotLastCheck.KEY)
public class ArgumentWithDefaultValueNotLastCheck extends PHPVisitorCheck {

  public static final String KEY = "S1788";

  private static final String MESSAGE = "Move arguments %s after arguments without default value";

  @Override
  public void visitParameterList(ParameterListTree parameterList) {
    List<ParameterTree> parametersToMove = getParametersToMove(parameterList);
    if (!parametersToMove.isEmpty() && !isVariableLengthParameterList(parameterList)) {
      context().newIssue(this, parameterList, String.format(MESSAGE, getNameListString(parametersToMove)));
    }
    super.visitParameterList(parameterList);
  }

  private static boolean isVariableLengthParameterList(ParameterListTree parameterList) {
    SeparatedList<ParameterTree> parameters = parameterList.parameters();
    if (!parameters.isEmpty()) {
      ParameterTree lastParameter = parameters.get(parameters.size() - 1);
      return lastParameter.ellipsisToken() != null;
    }
    return false;
  }

  /**
   * <p>Return list of parameter nodes that are not declared at the end.</p>
   * <p/>
   * Example: $p2 will be returned.
   * <pre>function f($p1, $p2 = 1, $p3, $p4 = 4) {...}</pre>
   */
  private static List<ParameterTree> getParametersToMove(ParameterListTree parameterList) {
    List<ParameterTree> parametersToMove = new ArrayList<>();
    boolean metParamWithoutDefault = false;

    for (ParameterTree param : ListUtils.reverse(parameterList.parameters())) {
      boolean hasDefault = param.initValue() != null;

      if (!hasDefault && !metParamWithoutDefault) {
        metParamWithoutDefault = true;
      } else if (hasDefault && metParamWithoutDefault) {
        parametersToMove.add(param);
      }
    }

    return ListUtils.reverse(parametersToMove);
  }

  private static String getNameListString(List<ParameterTree> params) {
    List<String> parameterNames = new ArrayList<>();
    for (ParameterTree parameter : params) {
      parameterNames.add("\"" + parameter.variableIdentifier().variableExpression().text() + "\"");
    }
    return String.join(", ", parameterNames);
  }

}
