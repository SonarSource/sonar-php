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

import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;

@Rule(
  key = "S1820",
  name = "Classes should not have too many fields",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("1h")
public class TooManyFieldsInClassCheck extends SquidCheck<LexerlessGrammar> {

  public static final int DEFAULT_MAX = 20;
  public static final boolean DEFAULT_COUNT_NON_PUBLIC = true;

  @RuleProperty(
    key = "maximumFieldThreshold",
    defaultValue = "" + DEFAULT_MAX)
  int maximumFieldThreshold = DEFAULT_MAX;

  @RuleProperty(
    key = "countNonpublicFields",
    type = "BOOLEAN",
    defaultValue = "" + DEFAULT_COUNT_NON_PUBLIC)
  boolean countNonpublicFields = DEFAULT_COUNT_NON_PUBLIC;


  @Override
  public void init() {
    subscribeTo(PHPGrammar.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    int nbFields = getNumberOfFields(astNode);

    if (nbFields > maximumFieldThreshold) {
      String msg = countNonpublicFields ? String.valueOf(maximumFieldThreshold) : (maximumFieldThreshold + " public");
      getContext().createLineViolation(this, "Refactor this class so it has no more than {0} fields, rather than the {1} it currently has.", astNode,
        msg, nbFields);
    }
  }

  private int getNumberOfFields(AstNode classDef) {
    List<AstNode> fields = getClassFields(classDef);
    int nbFields = fields.size();

    if (!countNonpublicFields) {
      nbFields -= getNumberOfNonPublicFields(fields);
    }
    return nbFields;
  }

  private static List<AstNode> getClassFields(AstNode classDef) {
    List<AstNode> fields = Lists.newArrayList();

    for (AstNode classStmt : classDef.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode statement = classStmt.getFirstChild();

      if (statement.is(PHPGrammar.CLASS_CONSTANT_DECLARATION, PHPGrammar.CLASS_VARIABLE_DECLARATION)) {
        fields.add(statement);
      }
    }
    return fields;
  }

  private static int getNumberOfNonPublicFields(List<AstNode> fields) {
    int nbNonPublicFields = 0;

    for (AstNode field : fields) {

      // class constants are public in PHP
      if (field.is(PHPGrammar.CLASS_VARIABLE_DECLARATION) && isNonPublic(field.getFirstChild(PHPGrammar.VARIABLE_MODIFIERS))) {
        nbNonPublicFields++;
      }
    }
    return nbNonPublicFields;
  }

  private static boolean isNonPublic(AstNode modifiers) {
    for (AstNode modifier : modifiers.getChildren()) {
      if (modifier.isNot(PHPKeyword.VAR) && modifier.getFirstChild().is(PHPKeyword.PROTECTED, PHPKeyword.PRIVATE)) {
        return true;
      }
    }
    return false;
  }

}
