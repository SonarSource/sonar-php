/*
 * Copyright (C) 2009-2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.samples.php;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = "visitor",
  priority = Priority.MINOR,
  name = "PHP visitor check",
  description = "desc")
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.SECURITY_FEATURES)
@SqaleConstantRemediation("5min")
public class CustomPHPVisitorCheck extends PHPVisitorCheck {

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    context().newIssue(this, "Function expression.").tree(tree);
    super.visitFunctionExpression(tree);
  }

}
