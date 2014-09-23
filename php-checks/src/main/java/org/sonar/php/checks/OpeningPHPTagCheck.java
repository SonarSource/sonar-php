/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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

import com.sonar.sslr.api.AstNode;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.lexer.PHPTagsChannel;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1757",
  priority = Priority.MINOR)
public class OpeningPHPTagCheck extends SquidCheck<LexerlessGrammar> {

  private static final String LONG_TAG = "<?php";
  private static final String SHORT_ECHO_TAG = "<?=";

  @Override
  public void init() {
    subscribeTo(PHPTagsChannel.FILE_OPENING_TAG);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isNotAuthorisedTag(astNode)) {
      getContext().createLineViolation(this, "Change this opening tag to either \"<?php\" or \"<?=\".", getLineToReport(astNode));
    }
  }

  /**
   * Return line on which the issue should be reported.
   * <p/>
   * The node contains everything before the first opening include HTML if present
   * this allows to ensure reporting the issue on the correct line.
   */
  private int getLineToReport(AstNode node) {
    return node.getTokenValue().split("(?:\r)?\n|\r").length;
  }

  private boolean isNotAuthorisedTag(AstNode node) {
    int offset = 1 + LONG_TAG.length();
    String openingTag = node.getTokenOriginalValue();

    if (openingTag.length() > offset) {
      openingTag = openingTag.substring(offset);
    }

    return !openingTag.contains(LONG_TAG) && !openingTag.contains(SHORT_ECHO_TAG);
  }

}
