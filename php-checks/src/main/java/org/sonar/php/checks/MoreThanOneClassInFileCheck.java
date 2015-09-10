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
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleLinearRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

@Rule(
  key = "S1996",
  name = "Files should contain only one class or interface each",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleLinearRemediation(coeff = "10min", effortToFixDescription = "classes + interfaces -1")
public class MoreThanOneClassInFileCheck extends SquidCheck<LexerlessGrammar> {

  private int nbClass = 0;
  private int nbInterface = 0;

  @Override
  public void init() {
    subscribeTo(PHPGrammar.CLASS_DECLARATION, PHPGrammar.INTERFACE_DECLARATION);

  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    nbClass = 0;
    nbInterface = 0;
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      nbClass++;
    } else {
      nbInterface++;
    }
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    if ((nbClass + nbInterface) > 1) {

      CheckMessage msg = new CheckMessage((Object) this, "There are {0}{1}{2}in this file; move all but one of them to other files.",
        nbClass > 0 ? (nbClass + " independent classes ") : "",
        nbClass > 0 && nbInterface > 0 ? "and " : "",
        nbInterface > 0 ? (nbInterface + " independent interfaces ") : "");
      int cost = nbClass + nbInterface - 1;
      msg.setCost(cost);
      getContext().log(msg);
    }
  }
}
