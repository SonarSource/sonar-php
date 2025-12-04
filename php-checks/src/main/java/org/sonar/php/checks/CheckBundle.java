/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.List;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public abstract class CheckBundle extends PHPVisitorCheck {

  private List<CheckBundlePart> checks;

  @Override
  public void init() {
    checks = checks();
    checks.forEach(check -> {
      check.init();
      check.setBundle(this);
    });
  }

  @Override
  public List<PhpIssue> analyze(CheckContext context) {
    checks.forEach(check -> check.analyze(context));
    return context.getIssues();
  }

  protected abstract List<CheckBundlePart> checks();
}
