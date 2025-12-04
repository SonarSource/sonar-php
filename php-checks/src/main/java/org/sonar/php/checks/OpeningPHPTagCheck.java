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

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1757")
public class OpeningPHPTagCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Change this opening tag to either \"<?php\" or \"<?=\".";

  private static final String LONG_TAG = "<?php";
  private static final String SHORT_ECHO_TAG = "<?=";
  private static final String SHORT_TAG = "<?";

  @Override
  public void visitScript(ScriptTree tree) {
    String openingTagWithContentBefore = tree.fileOpeningTagToken().text();

    if (!isAuthorisedTag(openingTagWithContentBefore)) {
      context().newLineIssue(this, getLineToReport(tree), MESSAGE);
    }
  }

  /**
   * Return line on which the issue should be reported.
   * <p/>
   * The node contains everything before the first opening include HTML if present
   * this allows to ensure reporting the issue on the correct line.
   */
  private static int getLineToReport(ScriptTree tree) {
    return tree.fileOpeningTagToken().endLine();
  }

  private static boolean isAuthorisedTag(String openingTagWithContentBefore) {
    return openingTagWithContentBefore.endsWith(LONG_TAG)
      || openingTagWithContentBefore.endsWith(SHORT_ECHO_TAG)
      || !openingTagWithContentBefore.endsWith(SHORT_TAG);
  }

}
