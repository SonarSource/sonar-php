package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Rule(key = "S4143")
public class OverwrittenArrayElementCheck extends PHPSubscriptionCheck {

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return ImmutableList.of(Tree.Kind.BLOCK, Tree.Kind.SCRIPT);
  }

  @Override
  public void visitNode(Tree tree) {
    List<StatementTree> statementTrees;
    if (tree.is(Tree.Kind.BLOCK)) {
      statementTrees = ((BlockTree) tree).statements();
    } else {
      statementTrees = ((ScriptTree) tree).statements();
    }

    Map<String, Tree> writtenAndUnread = new HashMap<>();
    for (StatementTree statementTree : statementTrees) {
      if (!isAssignmentStatement(statementTree)) {
        writtenAndUnread.clear();
        continue;
      }

      ExpressionTree assignVariableTree = ((AssignmentExpressionTree) ((ExpressionStatementTree) statementTree).expression()).variable();

      if (!assignVariableTree.is(Tree.Kind.ARRAY_ACCESS) ||
        !((ArrayAccessTree) assignVariableTree).object().is(Tree.Kind.VARIABLE_IDENTIFIER) ||
        !((ArrayAccessTree) assignVariableTree).offset().is(Tree.Kind.NUMERIC_LITERAL, Tree.Kind.REGULAR_STRING_LITERAL)) {
        continue;
      }

      ArrayAccessTree arrayAccessTree = (ArrayAccessTree) assignVariableTree;
      String key = ((VariableIdentifierTree)arrayAccessTree.object()).text() + ((LiteralTree)arrayAccessTree.offset()).value();

      if (writtenAndUnread.containsKey(key)) {
        context().newIssue(this, statementTree, "Verify this is the key that was intended; it was already set before");
      }

      writtenAndUnread.put(key, statementTree);
    }

    super.visitNode(tree);
  }

  private static boolean isAssignmentStatement(StatementTree tree) {
    if (!tree.is(Tree.Kind.EXPRESSION_STATEMENT)) {
      return false;
    }

    return ((ExpressionStatementTree) tree).expression().is(Tree.Kind.ASSIGNMENT); // TODO: Check different kinds?
  }
}
