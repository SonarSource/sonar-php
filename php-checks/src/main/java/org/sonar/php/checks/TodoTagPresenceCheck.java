/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
