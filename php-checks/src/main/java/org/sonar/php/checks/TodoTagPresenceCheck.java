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

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.AbstractCommentContainsPatternCheck;

@Rule(key = TodoTagPresenceCheck.KEY)
public class TodoTagPresenceCheck extends AbstractCommentContainsPatternCheck {

  public static final String KEY = "S1135";
  private static final String MESSAGE = "Complete the task associated to this \"TODO\" comment.";
  private static final String PATTERN = "TODO";

  @Override
  protected String pattern() {
    return PATTERN;
  }

  @Override
  protected String message() {
    return MESSAGE;
  }

}
