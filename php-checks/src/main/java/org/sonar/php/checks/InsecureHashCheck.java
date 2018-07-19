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
import java.util.Locale;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import static org.sonar.php.checks.utils.CheckUtils.getFunctionName;

@Rule(key = InsecureHashCheck.KEY)
public class InsecureHashCheck extends FunctionUsageCheck {

  public static final String KEY = "S2070";
  private static final String MESSAGE = "Use a stronger hashing algorithm than %s.";

  @Override
  protected ImmutableSet<String> functionNames() {
    return ImmutableSet.of("md5", "sha1");
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    String functionName = getFunctionName(tree);
    context().newIssue(this, tree.callee(), String.format(MESSAGE, functionName.toUpperCase(Locale.ENGLISH)));
  }

}
