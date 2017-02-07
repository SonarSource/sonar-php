/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = HardCodedCredentialsCheck.KEY)
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
    checkVariable(declaration.identifier(), identifier, declaration.initValue());
    super.visitVariableDeclaration(declaration);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree assignment) {
    SyntaxToken lastToken = ((PHPTree) assignment.variable()).getLastToken();
    String variableName = lastToken.text();
    checkVariable(lastToken, variableName, assignment.value());
    super.visitAssignmentExpression(assignment);
  }

  private void checkVariable(Tree tree, String identifier, @Nullable Tree value) {
    if (value != null && value.is(Kind.REGULAR_STRING_LITERAL) && PASSWORD_VARIABLE_PATTERN.matcher(identifier).find()) {
      addIssue(tree);
    }
  }

  private void addIssue(Tree tree) {
    context().newIssue(this, tree, MESSAGE);
  }

}
