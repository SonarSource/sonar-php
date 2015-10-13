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

import com.google.common.collect.ImmutableList;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;
import java.util.regex.Pattern;

@Rule(
  key = LowerCaseColorCheck.KEY,
  name = "Colors should be defined in upper case",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class LowerCaseColorCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S2038";
  private static final String MESSAGE = "Replace \"%s\" with \"%s\".";

  private static final Pattern COLOR_REGEXP = Pattern.compile("#[A-Fa-f0-9]{3,6}");
  private static final Pattern COLOR_REGEXP_UPPER_CASE = Pattern.compile("#[A-F0-9]{3,6}");

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.REGULAR_STRING_LITERAL);
  }

  @Override
  public void visitNode(Tree tree) {
    String stringContent = getStringContent((LiteralTree) tree);

    if (isLowerCaseColor(stringContent)) {
      String message = String.format(MESSAGE, stringContent, stringContent.toUpperCase());
      context().newIssue(KEY, message).tree(tree);
    }
  }

  private static boolean isLowerCaseColor(String str) {
    return COLOR_REGEXP.matcher(str).matches() && !COLOR_REGEXP_UPPER_CASE.matcher(str).matches();
  }

  private static String getStringContent(LiteralTree stringLiteral) {
    String stringContent = stringLiteral.value();
    return stringContent.substring(1, stringContent.length() - 1);
  }
}
