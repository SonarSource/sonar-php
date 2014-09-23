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

import javax.annotation.Nullable;

@Rule(
  key = "S1780",
  priority = Priority.MINOR)
public class ClosingTagInFullPHPFileCheck extends SquidCheck<LexerlessGrammar> {

  private int inlineHTMLCounter = 0;
  private boolean isOnlyClosingTag = false;
  private AstNode lastInlineHTMLNode = null;

  @Override
  public void init() {
    subscribeTo(PHPTagsChannel.INLINE_HTML);
  }

  @Override
  public void visitNode(AstNode astNode) {
    inlineHTMLCounter++;
    isOnlyClosingTag = "?>".equals(astNode.getTokenOriginalValue().trim());
    lastInlineHTMLNode = astNode;
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    inlineHTMLCounter = 0;
    isOnlyClosingTag = false;
    lastInlineHTMLNode = null;
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    if (inlineHTMLCounter == 1 && isOnlyClosingTag) {
      getContext().createLineViolation(this, "Remove this closing tag \"?>\".", lastInlineHTMLNode);
    }
  }

}
