package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ConstantConditionCheck.KEY)
public class ConstantConditionCheck extends PHPVisitorCheck {

  public static final String KEY = "S5797";
  private static final String MESSAGE = "Replace this expression; used as a condition it will always be constant.";

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    ExpressionTree conditionExpression = tree.condition().expression();
    if(conditionExpression.is(Tree.Kind.BOOLEAN_LITERAL) || conditionExpression.is(Tree.Kind.NUMERIC_LITERAL)) {
      newIssue(conditionExpression, MESSAGE);
    }
    super.visitIfStatement(tree);
  }
}
