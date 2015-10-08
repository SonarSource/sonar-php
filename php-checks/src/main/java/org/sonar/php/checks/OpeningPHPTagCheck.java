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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = OpeningPHPTagCheck.KEY,
  name = "\"<?php\" and \"<?=\" tags should be used",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR1})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class OpeningPHPTagCheck extends PHPVisitorCheck {

  public static final String KEY = "S1757";
  private static final String MESSAGE = "Change this opening tag to either \"<?php\" or \"<?=\".";

  private static final String LONG_TAG = "<?php";
  private static final String SHORT_ECHO_TAG = "<?=";
  private static final String SHORT_TAG = "<?";


  @Override
  public void visitScript(ScriptTree tree) {
    String openingTagWithContentBefore = tree.fileOpeningTagToken().text();

    if (!isAuthorisedTag(openingTagWithContentBefore)) {
      context().newIssue(KEY, MESSAGE).line(getLineToReport(openingTagWithContentBefore));
    }
  }

  /**
   * Return line on which the issue should be reported.
   * <p/>
   * The node contains everything before the first opening include HTML if present
   * this allows to ensure reporting the issue on the correct line.
   */
  private int getLineToReport(String openingTag) {
    return openingTag.split("(?:\r)?\n|\r").length;
  }

  private boolean isAuthorisedTag(String openingTagWithContentBefore) {
    return openingTagWithContentBefore.endsWith(LONG_TAG)
      || openingTagWithContentBefore.endsWith(SHORT_ECHO_TAG)
      || !openingTagWithContentBefore.endsWith(SHORT_TAG);
  }

}
