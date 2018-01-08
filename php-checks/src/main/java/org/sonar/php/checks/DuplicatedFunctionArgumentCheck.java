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

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = DuplicatedFunctionArgumentCheck.KEY)
public class DuplicatedFunctionArgumentCheck extends PHPVisitorCheck {

  public static final String KEY = "S1536";

  private static final String MESSAGE = "Rename the duplicated function %s \"%s\".";

  @Override
  public void visitParameterList(ParameterListTree parameterList) {
    Set<String> parameterNames = Sets.newHashSet();
    Set<String> duplicatedParamNames = Sets.newTreeSet();

    for (ParameterTree parameter : parameterList.parameters()) {
      String name = parameter.variableIdentifier().variableExpression().text();
      boolean isNewName = parameterNames.add(name);
      if (!isNewName) {
        duplicatedParamNames.add(name);
      }
    }

    if (!duplicatedParamNames.isEmpty()) {
      String listString = Joiner.on(", ").join(duplicatedParamNames);
      context().newIssue(this, parameterList, String.format(MESSAGE, duplicatedParamNames.size() == 1 ? "parameter" : "parameters", listString));
    }

    super.visitParameterList(parameterList);
  }

}
