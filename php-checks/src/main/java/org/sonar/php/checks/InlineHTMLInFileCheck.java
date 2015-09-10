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

import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPTokenType;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

@Rule(
  key = "S1997",
  name = "Files should not contain inline HTML",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("10min")
public class InlineHTMLInFileCheck extends SquidCheck<LexerlessGrammar> {

  private boolean fileHasIssue = false;

  @Override
  public void init() {
    subscribeTo(PHPTokenType.INLINE_HTML);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    fileHasIssue = false;
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!"?>".equals(astNode.getTokenOriginalValue().trim()) && !fileHasIssue && !isExcludedFile()) {
      getContext().createFileViolation(this, "Remove the inline HTML in this file.");
      fileHasIssue = true;
    }
  }

  private boolean isExcludedFile() {
    return getContext().getFile().getName().endsWith(".phtml");
  }

}
