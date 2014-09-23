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

import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.api.PHPTokenType;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;

@Rule(
  key = "S1788",
  priority = Priority.CRITICAL)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
public class ArgumentWithDefaultValueNotLastCheck extends SquidCheck<LexerlessGrammar> {


  @Override
  public void init() {
    subscribeTo(PHPGrammar.PARAMETER_LIST);
  }

  @Override
  public void visitNode(AstNode astNode) {
    List<AstNode> paramToReport = getParamToReport(astNode);

    if (!paramToReport.isEmpty()) {
      getContext().createLineViolation(this, "Move arguments {0} after arguments without default value", astNode, paramToString(paramToReport));
    }
  }

  /**
   * <p>Return list of parameter nodes that are not declared at the end.</p>
   * <p/>
   * Example: $p2 will be returned.
   * <pre>function f($p1, $p2 = 1, $p3, $p4 = 4) {...}</pre>
   */
  private List<AstNode> getParamToReport(AstNode paramListNode) {
    List<AstNode> paramToReport = Lists.newArrayList();
    boolean metParamWithoutDefault = false;

    for (AstNode param : Lists.reverse(paramListNode.getChildren(PHPGrammar.PARAMETER))) {
      boolean hasDefault = param.hasDirectChildren(PHPPunctuator.EQU);

      if (!hasDefault && !metParamWithoutDefault) {
        metParamWithoutDefault = true;
      } else if (hasDefault && metParamWithoutDefault) {
        paramToReport.add(param);
      }
    }

    return Lists.reverse(paramToReport);
  }

  private String paramToString(List<AstNode> params) {
    StringBuilder build = new StringBuilder();

    for (int i = 0; i < params.size(); i++) {

      if (i == (params.size() - 1)) {
        build.append("\"" + params.get(i).getFirstChild(PHPGrammar.VAR_IDENTIFIER).getTokenOriginalValue() + "\"");
      } else {
        build.append("\"" + params.get(i).getFirstChild(PHPGrammar.VAR_IDENTIFIER).getTokenOriginalValue() + "\", ");
      }
    }

    return build.toString();
  }

}
