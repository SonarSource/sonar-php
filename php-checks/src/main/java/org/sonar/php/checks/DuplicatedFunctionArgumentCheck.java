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

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Iterator;
import java.util.Set;

@Rule(
  key = "S1536",
  name = "Function argument names should be unique\n",
  priority = Priority.CRITICAL,
  tags = {Tags.PITFALL})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class DuplicatedFunctionArgumentCheck extends SquidCheck<LexerlessGrammar> {

  private Set<String> parameters = Sets.newHashSet();
  private Set<String> duplicatedParams = Sets.newTreeSet();


  @Override
  public void init() {
    subscribeTo(PHPGrammar.PARAMETER_LIST);
  }

  @Override
  public void visitNode(AstNode astNode) {
    for (AstNode parameter : astNode.getChildren(PHPGrammar.PARAMETER)) {
      String paramName = parameter.getFirstChild(PHPGrammar.VAR_IDENTIFIER).getTokenOriginalValue();

      if (parameters.contains(paramName)) {
        duplicatedParams.add(paramName);
      } else {
        parameters.add(paramName);
      }
    }

    if (!duplicatedParams.isEmpty()) {
      getContext().createLineViolation(this, "Rename the duplicated function {0} \"{1}\".", astNode,
        duplicatedParams.size() == 1 ? "parameter" : "parameters",
        duplicatedParamsToString());
    }

    parameters.clear();
    duplicatedParams.clear();
  }

  private String duplicatedParamsToString() {
    StringBuilder builder = new StringBuilder();

    Iterator<String> it = duplicatedParams.iterator();
    while (it.hasNext()) {
      builder.append(it.next() + ", ");
    }

    return StringUtils.removeEnd(builder.toString().trim(), ",");
  }

}
