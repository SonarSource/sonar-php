/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.plugins.php;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.PHPAnalyzer;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.symbols.ProjectSymbolData;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

public class WpHookCollector extends FunctionUsageCheck {

  private static final Logger LOG = Loggers.get(WpHookCollector.class);

  public final Map<String, List<String>> wpActions = new HashMap<>();
  private final ProjectSymbolData projectSymbolData;

  public WpHookCollector(ProjectSymbolData projectSymbolData) {
    this.projectSymbolData = projectSymbolData;
  }

  @Override
  protected Set<String> functionNames() {
    return Collections.singleton("add_action");
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    if (tree.callArguments().size() < 2) {
      return;
    }

    ExpressionTree hookNameArg = tree.callArguments().get(0).value();
    ExpressionTree callbackArg = tree.callArguments().get(1).value();
    if (!hookNameArg.is(Tree.Kind.REGULAR_STRING_LITERAL) || !callbackArg.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      // TODO: only string second arguments are handled for testing
      return;
    }

    wpActions.computeIfAbsent(CheckUtils.trimQuotes((LiteralTree) hookNameArg),
      s -> new ArrayList<>()).add(CheckUtils.trimQuotes((LiteralTree) callbackArg));
  }
}
