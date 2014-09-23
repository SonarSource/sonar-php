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
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPTokenType;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.regex.Pattern;

@Rule(
  key = "S116",
  priority = Priority.MAJOR)
public class FieldNameCheck extends SquidCheck<LexerlessGrammar> {

  public static final String DEFAULT = "^[a-z][a-zA-Z0-9]*$";
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  String format = DEFAULT;


  @Override
  public void init() {
    pattern = Pattern.compile(format);
    subscribeTo(PHPGrammar.CLASS_VARIABLE_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    for (AstNode varDec : astNode.getChildren(PHPGrammar.VARIABLE_DECLARATION)) {
      String fieldName = varDec.getFirstChild(PHPGrammar.VAR_IDENTIFIER).getTokenOriginalValue();

      if (!pattern.matcher(StringUtils.remove(fieldName, "$")).matches()) {
        getContext().createLineViolation(this, "Rename this field \"{0}\" to match the regular expression {1}.", astNode, fieldName, format);
      }
    }
  }

}
