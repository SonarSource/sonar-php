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

import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.AbstractCommentContainsPatternCheck;
import org.sonar.squidbridge.annotations.NoSqale;

@Rule(
  key = TodoTagPresenceCheck.KEY,
  name = "\"TODO\" tags should be handled",
  priority = Priority.INFO)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.INFO)
@NoSqale
public class TodoTagPresenceCheck extends AbstractCommentContainsPatternCheck {

  public static final String KEY = "S1135";
  private static final String MESSAGE = "Complete the task associated to this \"TODO\" comment.";
  private static final String PATTERN = "TODO";

  @Override
  protected String pattern() {
    return PATTERN;
  }

  @Override
  protected String key() {
    return KEY;
  }

  @Override
  protected String message() {
    return MESSAGE;
  }

}
