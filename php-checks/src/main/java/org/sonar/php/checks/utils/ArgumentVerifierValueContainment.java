/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.checks.utils;

import java.util.Set;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;

public class ArgumentVerifierValueContainment extends ArgumentMatcherValueContainment implements IssueRaiser {

  private boolean raiseIssueOnMatch = true;

  @Deprecated
  public ArgumentVerifierValueContainment(int position, Set<String> values) {
    super(position, values);
  }

  public ArgumentVerifierValueContainment(int position, String name, Set<String> values) {
    super(position, name, values);
  }

  @Deprecated
  public ArgumentVerifierValueContainment(int position, String value) {
    this(position, SetUtils.immutableSetOf(value));
  }

  public ArgumentVerifierValueContainment(int position, String name, String value) {
    this(position, name, SetUtils.immutableSetOf(value));
  }

  public ArgumentVerifierValueContainment(int position, String value, boolean raiseIssueOnMatch) {
    this(position, SetUtils.immutableSetOf(value));
    this.raiseIssueOnMatch = raiseIssueOnMatch;
  }

  public ArgumentVerifierValueContainment(int position, String name, String value, boolean raiseIssueOnMatch) {
    this(position, name, SetUtils.immutableSetOf(value));
    this.raiseIssueOnMatch = raiseIssueOnMatch;
  }

  public ArgumentVerifierValueContainment(int position, Set<String> values, boolean raiseIssueOnMatch) {
    super(position, values);
    this.raiseIssueOnMatch = raiseIssueOnMatch;
  }

  public ArgumentVerifierValueContainment(int position, String name, Set<String> values, boolean raiseIssueOnMatch) {
    super(position, name, values);
    this.raiseIssueOnMatch = raiseIssueOnMatch;
  }

  @Override
  public boolean shouldRaiseIssue(boolean matchingSuccessful, ExpressionTree argumentValue) {
    return nameOf(argumentValue).isPresent() && matchingSuccessful == raiseIssueOnMatch;
  }

  public boolean isRaiseIssueOnMatch() {
    return raiseIssueOnMatch;
  }
}
