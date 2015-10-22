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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.regex.Pattern;

@Rule(
  key = HardCodedCredentialsCheck.KEY,
  name = "Credentials should not be hard-coded",
  priority = Priority.CRITICAL,
  tags = {Tags.CWE, Tags.OWASP_A2, Tags.SANS_TOP25_POROUS, Tags.SECURITY})
@ActivatedByDefault
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.SECURITY_FEATURES)
@SqaleConstantRemediation("30min")
public class HardCodedCredentialsCheck extends PHPVisitorCheck {

  public static final String KEY = "S2068";

  private static final String MESSAGE = "Remove this hard-coded password.";

  private static final Pattern PASSWORD_LITERAL_PATTERN = Pattern.compile("password=..", Pattern.CASE_INSENSITIVE);
  private static final Pattern PASSWORD_VARIABLE_PATTERN = Pattern.compile("password", Pattern.CASE_INSENSITIVE);

  @Override
  public void visitLiteral(LiteralTree literal) {
    if (literal.is(Kind.REGULAR_STRING_LITERAL) && PASSWORD_LITERAL_PATTERN.matcher(literal.token().text()).find()) {
      addIssue(literal);
    }
    super.visitLiteral(literal);
  }

  @Override
  public void visitVariableDeclaration(VariableDeclarationTree declaration) {
    String identifier = declaration.identifier().text();
    checkVariable(declaration, identifier, declaration.initValue());
    super.visitVariableDeclaration(declaration);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree assignment) {
    String variableName = ((PHPTree) assignment.variable()).getLastToken().text();
    checkVariable(assignment, variableName, assignment.value());
    super.visitAssignmentExpression(assignment);
  }

  private void checkVariable(Tree tree, String identifier, Tree value) {
    if (value != null && value.is(Kind.REGULAR_STRING_LITERAL) && PASSWORD_VARIABLE_PATTERN.matcher(identifier).find()) {
      addIssue(tree);
    }
  }

  private void addIssue(Tree tree) {
    context().newIssue(KEY, MESSAGE).tree(tree);
  }

}
