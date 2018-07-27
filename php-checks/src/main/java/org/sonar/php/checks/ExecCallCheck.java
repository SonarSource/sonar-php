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

import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

@Rule(key = ExecCallCheck.KEY)
public class ExecCallCheck extends FunctionUsageCheck {

  public static final String KEY = "S4721";
  private static final String MESSAGE = "Make sure that executing this OS command is safe here.";

  @Override
  protected ImmutableSet<String> functionNames() {
    return ImmutableSet.of("exec", "passthru", "proc_open", "popen", "shell_exec", "system", "pcntl_exec");
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    context().newIssue(this, tree.callee(), MESSAGE);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    // http://php.net/manual/en/language.operators.execution.php
    if (tree.value().startsWith("`")) {
      context().newIssue(this, tree, MESSAGE);
    }
    super.visitLiteral(tree);
  }
}
