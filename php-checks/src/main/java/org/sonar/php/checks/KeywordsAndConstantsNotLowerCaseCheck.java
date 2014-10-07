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
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.regex.Pattern;

@Rule(
  key = "S1781",
  priority = Priority.MINOR)
public class KeywordsAndConstantsNotLowerCaseCheck extends SquidCheck<LexerlessGrammar> {

  private static final Pattern PATTERN = Pattern.compile("[a-z_]+");

  @Override
  public void init() {
    subscribeTo(PHPKeyword.values());
    subscribeTo(PHPGrammar.COMMON_SCALAR);
  }

  @Override
  public void visitNode(AstNode astNode) {
    String tokenValue = astNode.getTokenOriginalValue();

    if (!PATTERN.matcher(tokenValue).matches()) {

      if (isTrueFalseOrNull(astNode)) {
        reportIssue(astNode, "constant", tokenValue);

      } else if (astNode.is(PHPKeyword.values())) {
        reportIssue(astNode, "keyword", tokenValue);
      }
    }
  }

  private boolean isTrueFalseOrNull(AstNode node) {
    AstNode scalar = node.getFirstChild();

    return node.is(PHPGrammar.COMMON_SCALAR)
      && ("null".equalsIgnoreCase(scalar.getTokenOriginalValue()) || scalar.is(PHPGrammar.BOOLEAN_LITERAL));
  }

  private final void reportIssue(AstNode node, String kind, String value) {
    getContext().createLineViolation(this, "Write this \"" + value + "\" " + kind + " in lower case.", node);
  }

}
